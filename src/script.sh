#!/bin/bash
jjtree Yal.jjt
javacc tree/Yal.jj
javac generator/*.java
javac semantic/*.java
javac tree/*.java
java tree.Yal mymodule3.yal