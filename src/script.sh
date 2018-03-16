#!/bin/bash
jjtree Yal.jjt
javacc Yal.jj
javac *.java
java Yal aval5_err.yal