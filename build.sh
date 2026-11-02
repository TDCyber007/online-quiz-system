#!/bin/sh
# Builds the Online Quiz System into the 'out' folder.
set -e
cd "$(dirname "$0")"

echo
echo "=============================================="
echo "  Online Quiz System - build"
echo "=============================================="
echo

mkdir -p out

# One entry per package. Add a new line here when a new package is created.
javac -encoding UTF-8 -Xlint:all -d out \
    src/com/quizsystem/*.java \
    src/com/quizsystem/model/*.java \
    src/com/quizsystem/store/*.java \
    src/com/quizsystem/service/*.java \
    src/com/quizsystem/ui/*.java \
    src/com/quizsystem/util/*.java \
    src/com/quizsystem/test/*.java

echo
echo "[OK] Build finished. The classes are in the 'out' folder."
echo
