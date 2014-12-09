package luca.corigliano.cheapband;

import java.io.File;
import java.io.IOException;
import java.lang.String;

public class Utils {
    public static String PathCombine(String path1, String path2)
    {
        try {
            return new File(path1, path2).getCanonicalPath();
        } catch (IOException ignored) {
            return new File(path1, path2).getPath();
        }

    }
    public static String GetAbsolutePath(String relativePath)
    {
        return new File(relativePath).getAbsolutePath();
    }
    public static boolean PathExists(String path)
    {
        return new File(path).exists();
    }
    public static boolean PathCreate(String path)
    {
       return new File(path).mkdir();
    }

    public static enum OS
    {
        WINDOWS,  MACOS,  LINUX,  UNKNOWN,
    }

    public static OS GetCurrentOS(String osName)
    {
        osName = osName.toLowerCase();
        if (osName.contains("win")) {
            return OS.WINDOWS;
        }
        if (osName.contains("mac")) {
            return OS.MACOS;
        }
        if (osName.contains("linux") || osName.contains("unix")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }

}
