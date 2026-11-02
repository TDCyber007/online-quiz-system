#!/bin/sh
# Starts the Online Quiz System.
set -e
cd "$(dirname "$0")"

if [ ! -f out/com/quizsystem/Main.class ]; then
    echo "The project is not compiled yet, building it now..."
    ./build.sh
fi

echo "Starting the Online Quiz System..."
echo
java -cp out com.quizsystem.Main
