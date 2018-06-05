#!/bin/bash
jjtree -OUTPUT_DIRECTORY=../src/tree -JJTREE_OUTPUT_DIRECTORY=../src/tree ../src/Yal.jjt
javacc -OUTPUT_DIRECTORY=../src/tree ../src/tree/Yal.jj
javac ../src/generator/*.java ../src/semantic/*.java ../src/tree/*.java
java -classpath ../src tree.Yal ErrCode/array2_err.yal
java -classpath ../src tree.Yal ErrCode/array4_err.yal
java -classpath ../src tree.Yal ErrCode/aval1_err.yal
java -classpath ../src tree.Yal ErrCode/aval2_err.yal
java -classpath ../src tree.Yal ErrCode/aval3_err.yal
java -classpath ../src tree.Yal ErrCode/aval4_err.yal
java -classpath ../src tree.Yal ErrCode/aval5_err.yal
java -classpath ../src tree.Yal ErrCode/aval6_err.yal
java -classpath ../src tree.Yal ErrCode/aval7_err.yal
java -classpath ../src tree.Yal ErrCode/err1.yal
java -classpath ../src tree.Yal ErrCode/error-resize-array.yal
java -classpath ../src tree.Yal ErrCode/error-shared-scope.yal
java -classpath ../src tree.Yal ErrCode/error-uninitialized-output.yal