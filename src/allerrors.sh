#!/bin/bash
jjtree Yal.jjt
javacc Yal.jj
javac *.java
java Yal ErrCode/array2_err.yal
java Yal ErrCode/array4_err.yal
java Yal ErrCode/aval1_err.yal
java Yal ErrCode/aval2_err.yal
java Yal ErrCode/aval3_err.yal
java Yal ErrCode/aval4_err.yal
java Yal ErrCode/aval5_err.yal
java Yal ErrCode/aval6_err.yal
java Yal ErrCode/aval7_err.yal
java Yal ErrCode/err1.yal
java Yal ErrCode/error-resize-array.yal
java Yal ErrCode/error-shared-scope.yal
java Yal ErrCode/error-uninitialized-output.yal