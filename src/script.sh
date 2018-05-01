#!/bin/bash
jjtree Yal.jjt
javacc Yal.jj
javac *.java
java Yal mymodule.yal