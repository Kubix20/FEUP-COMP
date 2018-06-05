#!/bin/sh
if [ "$#" -ne 1 ]; then
  echo "Usage: run.sh <file_path>"
  exit 1
fi

java -classpath src tree.Yal "$1"