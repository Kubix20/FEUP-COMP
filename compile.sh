#!/bin/bash
jjtree -OUTPUT_DIRECTORY=src/tree -JJTREE_OUTPUT_DIRECTORY=src/tree src/Yal.jjt
javacc -OUTPUT_DIRECTORY=src/tree src/tree/Yal.jj
javac src/generator/*.java src/semantic/*.java src/tree/*.java