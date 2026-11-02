#!/bin/sh
# Runs the test suite.
set -e
cd "$(dirname "$0")"

if [ ! -f out/com/quizsystem/test/SelfTest.class ]; then
    echo "The project is not compiled yet, building it now..."
    ./build.sh
fi

echo "Running the test suite..."
echo
java -cp out com.quizsystem.test.SelfTest
