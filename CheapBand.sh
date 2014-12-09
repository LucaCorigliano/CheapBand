#!/bin/sh
JAVA_VERSION="$(java -version 2>&1 > /dev/null)"

case "$JAVA_VERSION" in
	*java version*) java -jar CheapBoard.jar;;
	*) "CheapBoard requires Java."; pause;;
esac
