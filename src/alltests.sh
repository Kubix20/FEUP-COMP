#!/bin/bash
jjtree Yal.jjt
javacc Yal.jj
javac *.java
java Yal array2_err.yal
java Yal array4_err.yal
java Yal aval1_err.yal
java Yal aval2_err.yal
java Yal aval3_err.yal
java Yal aval4_err.yal
java Yal aval5_err.yal
java Yal aval6_err.yal
java Yal aval7_err.yal
java Yal err1.yal