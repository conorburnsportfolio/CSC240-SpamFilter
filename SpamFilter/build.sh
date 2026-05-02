#!/usr/bin/env bash
# Build and test script for the CSC 240 Spam Filter project.
#
# Usage:
#   ./build.sh             # compile main + tests, run tests
#   ./build.sh run         # compile main, run on data/spam_or_not_spam.csv
#   ./build.sh clean       # remove build artefacts
#
# Requires:
#   java 11+
#   junit-platform-console-standalone-1.10.x.jar in ./lib/
#   (download from https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar)

set -euo pipefail
cd "$(dirname "$0")"

SRC_MAIN=src/main/java
SRC_TEST=src/test/java
OUT_MAIN=out/main
OUT_TEST=out/test
LIB=lib

cmd=${1:-test}

case "$cmd" in
  clean)
    rm -rf out output
    echo "cleaned."
    ;;
  compile)
    mkdir -p "$OUT_MAIN"
    javac -d "$OUT_MAIN" $(find "$SRC_MAIN" -name '*.java')
    echo "compiled main -> $OUT_MAIN"
    ;;
  run)
    "$0" compile
    mkdir -p output
    input=${2:-/Users/conor/Documents/Claude/Projects/CSC240\ Text\ Processing\ Project/SpamFilter/data/spam_or_not_spam.csv}
    java -cp "$OUT_MAIN" Main "$input" output
    ;;
  test)
    "$0" compile
    mkdir -p "$OUT_TEST"
    jar=$(ls "$LIB"/junit-platform-console-standalone-*.jar 2>/dev/null | head -n 1 || true)
    if [[ -z "$jar" ]]; then
      echo "ERROR: no JUnit console jar found in $LIB/" >&2
      echo "  download junit-platform-console-standalone-1.10.2.jar into ./lib/" >&2
      exit 1
    fi
    javac -cp "$OUT_MAIN:$jar" -d "$OUT_TEST" $(find "$SRC_TEST" -name '*.java')
    java -jar "$jar" --class-path "$OUT_MAIN:$OUT_TEST" --scan-class-path
    ;;
  *)
    echo "usage: $0 [compile|run [csv]|test|clean]" >&2
    exit 2
    ;;
esac
