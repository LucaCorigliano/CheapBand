package luca.corigliano.cheapband;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Created by Luca on 12/8/2014.
 */
public class Bootstrap {

    //region UI Code
    private JTextArea textArea;
    private JPanel containerPanel;
    private JScrollPane scrollPane;

    private static Bootstrap InitializeUI()
    {
        Bootstrap b = new Bootstrap();

        JFrame frame = new JFrame("CheapBand Bootstrap");
        frame.setContentPane(b.containerPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(854, 480);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        return b;
    }
    //endregion

    // Entry point
    public static void main(String[] args) {
        Bootstrap bootstrap = InitializeUI();
        bootstrap.args = args;
        bootstrap.RunBootstrap();
    }

    // Constants
    private final static String MINECRAFT_REMOTE_URL = "https://s3.amazonaws.com/Minecraft.Download/launcher/Minecraft.jar";
    private final static String OSX_WORKAROUND_COMMANDS = "mkdir Library; cd Library; mkdir \"Application Library\"; cd \"Application Library\"; ln -s ../../ minecraft";
    //
    private  String _minecraftLocalPath = Utils.GetAbsolutePath("Minecraft");
    private  String _minecraftLocalDataPath = Utils.PathCombine(_minecraftLocalPath, "AppData");
    private  String _minecraftExePath = Utils.PathCombine(_minecraftLocalPath, "Minecraft.jar");

    public String[] args;


    public Bootstrap() {
        this.textArea.setLineWrap(true);
        this.textArea.setEditable(false);
        this.textArea.setFont(new Font("Monospaced", 0, 12));
        ((DefaultCaret) this.textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        this.scrollPane.setBorder(null);
        this.scrollPane.setVerticalScrollBarPolicy(22);
    }


    public void RunBootstrap() {
        // Introduction
        println("CheapBand by Corigliano Luca - 2014");
        println("Uses launch4j, swing. Entirely Open Source.");
        println("Browse and Download CheapBand source at: ");
        println("Minecraft is owned by Mojang. This software has no affiliation with them.\r\n");

        println("Bootstrap:");
        // Get Current OS
        String osName = System.getProperty("os.name");
        println("Current Operating System is " + osName + ".");
        Utils.OS currentOS = Utils.GetCurrentOS(osName);

        String basePath;
        if(args.length > 0) {
            basePath = args[0];
            println(String.format("Using %s as base path", basePath));
        } else {
            basePath = "./Minecraft";
            println("No arguments supplied, using ./Minecraft.");
        }
        CalculateFolders(basePath);

        // Check for Minecraft folder
        print("Does Minecraft folder exist?");
        if (Utils.PathExists(_minecraftLocalPath))
            println(" Yes.");
        else {
            Utils.PathCreate(_minecraftLocalPath);
            println(" No, created.");
        }

        // Check for Minecraft.jar
        print("Does Minecraft.jar exist?");
        if (Utils.PathExists(_minecraftExePath))
            println(" Yes.");
        else {
            println(" No, downloading.");
            if(!DownloadFile(MINECRAFT_REMOTE_URL, _minecraftExePath))
            {
                println("Could not download Minecraft.jar.");
                return;
            }

        }

        // Check for Minecraft AppData
        print("Does Minecraft AppData folder exist?");
        if (Utils.PathExists(_minecraftLocalDataPath))
            println(" Yes.");
        else {
            Utils.PathCreate(_minecraftLocalDataPath);
            println(" No, created.");
        }

        // OS X Workaround
        if (currentOS == Utils.OS.MACOS) {
            if (!new File(_minecraftLocalDataPath, "/Library/Application Support/minecraft").exists()) {
                print("Applying OS X Library workaround...");
                //
                ProcessBuilder pb = new ProcessBuilder("bash", OSX_WORKAROUND_COMMANDS);
                pb.directory(new File(_minecraftLocalDataPath));
                try {
                    pb.start();
                    println(" Done.");
                } catch (IOException e) {
                    println(" Error.");
                    return;
                }
            }
        }


        // Starting minecraft
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", _minecraftExePath);

        // Editing environment as needed
        Map<String, String> env = pb.environment();

        switch (currentOS) {
            case WINDOWS:
                env.put("APPDATA", _minecraftLocalDataPath);
                println("%APPDATA% locally set to " + _minecraftLocalDataPath + ".");
                break;
            case LINUX:
            case MACOS:
            case UNKNOWN:
                env.put("HOME", _minecraftLocalDataPath);
                println("$HOME locally set to " + _minecraftLocalDataPath + ".");
        }

        // Set working directory
        pb.directory(new File(_minecraftLocalPath));

        print("\r\nStarting Minecraft.jar...");

        try {
             pb.start();
             println(" Success.");
        } catch (IOException e) {
            e.printStackTrace();
            println(" Error.");
            return;
        }

        System.exit(0);

    }

    private void CalculateFolders(String basePath)
    {
        _minecraftLocalPath = Utils.GetAbsolutePath(basePath);
        _minecraftLocalDataPath = Utils.PathCombine(_minecraftLocalPath, "AppData");
        _minecraftExePath = Utils.PathCombine(_minecraftLocalPath, "Minecraft.jar");
    }

    private final boolean DownloadFile(String remoteUrl, String localPath) {
        BufferedInputStream webStream = null;
        URLConnection webConnection = null;
        FileOutputStream outStream = null;
        File outFile;
        int fileSize = 0;
        int downloadedSize = 0;
        try {
            // Connect to the URL
            webConnection = new URL(remoteUrl).openConnection();
            // Get file size
            fileSize = Integer.parseInt(webConnection.getHeaderField("Content-Length"));
            // Start the actual download
            webStream = new BufferedInputStream(webConnection.getInputStream());

            outFile = new File(localPath);
            outStream = new FileOutputStream(outFile);

            final byte data[] = new byte[1024];
            int count;
            while ((count = webStream.read(data, 0, 1024)) != -1) {
                downloadedSize += count;
                PrintProgressBar(remoteUrl, downloadedSize, fileSize);
                outStream.write(data, 0, count);
            }
            return true;
        } catch (MalformedURLException e) { // Invalid URL
            println("Could not download, URL seems invalid.");
            e.printStackTrace();
            return false;
        } catch (FileNotFoundException e) { // Unable to create the output file
            println("Could not download, I cannot create the output file.");
            e.printStackTrace();
            return false;
        } catch (IOException e) { // Generic download exception
            println("Could not download, generic exception during download.");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (webStream != null)
                    webStream.close();
                if (outStream != null)
                    outStream.close();
            } catch (IOException e) {
                // It should be safe to suppress this exception...
            }
        }
    }

    public void PrintProgressBar(String url, int downloadedSize, int fileSize) {
        StringBuilder bar = new StringBuilder(String.format("%s [", url));
        int percentage = downloadedSize / fileSize * 100;
        for (int i = 0; i < 25; i++) {
            if (i < (percentage / 4)) {
                bar.append("=");
            } else if (i == (percentage / 4)) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }

        bar.append(String.format("] %d/%dKB (%d%%)", downloadedSize / 1024, fileSize / 1024, percentage));
        System.out.print("\r" + bar.toString());
        printU("\r" + bar.toString());
    }

    public void println(String string) {
        print(string + "\n");
    }

    // Ultra f***ing dirty workaround
    private String _lastLine = "";
    public void printU(String string) {

        if(!_lastLine.isEmpty())
            textArea.setText(textArea.getText().replace(_lastLine, string));
        else
            println(string);
        _lastLine = string;
    }
    public void print(String string) {
        System.out.print(string);
        Document document = this.textArea.getDocument();
        try {
            document.insertString(document.getLength(), string, null);
        } catch (BadLocationException ignored) {
        }
    }
}
