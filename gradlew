#!/bin/sh

#
# Gradle wrapper script for Neon City RPG
#

set -e

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"

# Determine the project base directory
PRG="$0"
while [ -h "$PRG" ]; do
    ls=$(ls -ld "$PRG")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=$(dirname "$PRG")"/$link"
    fi
done
SAVED="$(pwd)"
cd "$(dirname "$PRG")/" >/dev/null
APP_HOME="$(pwd -P)"
cd "$SAVED" >/dev/null

# Add default JVM options
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Gradle version to use
GRADLE_VERSION="8.2"
GRADLE_DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
GRADLE_HOME="$GRADLE_DIST_DIR/gradle-${GRADLE_VERSION}"

# Download and extract Gradle if not present
if [ ! -d "$GRADLE_HOME" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    mkdir -p "$GRADLE_DIST_DIR"

    # Try curl first, then wget
    if command -v curl > /dev/null 2>&1; then
        curl -L -o "$GRADLE_DIST_DIR/gradle.zip" "$GRADLE_DIST_URL"
    elif command -v wget > /dev/null 2>&1; then
        wget -O "$GRADLE_DIST_DIR/gradle.zip" "$GRADLE_DIST_URL"
    else
        echo "Error: Neither curl nor wget found. Please install one of them."
        exit 1
    fi

    echo "Extracting Gradle..."
    unzip -q "$GRADLE_DIST_DIR/gradle.zip" -d "$GRADLE_DIST_DIR"
    rm "$GRADLE_DIST_DIR/gradle.zip"
    echo "Gradle ${GRADLE_VERSION} installed."
fi

# Determine Java command
if [ -n "$JAVA_HOME" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ]; then
        echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
        exit 1
    fi
else
    JAVACMD="java"
    command -v java >/dev/null 2>&1 || {
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found."
        exit 1
    }
fi

# Execute Gradle
exec "$GRADLE_HOME/bin/gradle" "$@"
