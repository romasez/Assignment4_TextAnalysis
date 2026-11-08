#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/assignment4/*.java
java -cp out assignment4.Main "${1:-test}"
