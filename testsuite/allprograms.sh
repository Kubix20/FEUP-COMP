#!/bin/bash
jjtree -OUTPUT_DIRECTORY=../src/tree -JJTREE_OUTPUT_DIRECTORY=../src/tree ../src/Yal.jjt
javacc -OUTPUT_DIRECTORY=../src/tree ../src/tree/Yal.jj
javac ../src/generator/*.java ../src/semantic/*.java ../src/tree/*.java
java -classpath ../src tree.Yal Programs/array1.yal
java -classpath ../src tree.Yal Programs/array2.yal
java -classpath ../src tree.Yal Programs/aval1.yal
java -classpath ../src tree.Yal Programs/aval2.yal
java -classpath ../src tree.Yal Programs/aval3.yal
java -classpath ../src tree.Yal Programs/aval4.yal
java -classpath ../src tree.Yal Programs/aval5.yal
java -classpath ../src tree.Yal Programs/aval6.yal
java -classpath ../src tree.Yal Programs/aval7.yal
java -classpath ../src tree.Yal Programs/aval8.yal
java -classpath ../src tree.Yal Programs/call-main.yal
java -classpath ../src tree.Yal Programs/constant-ranges.yal
java -classpath ../src tree.Yal Programs/erro_prof.yal
java -classpath ../src tree.Yal Programs/library1.yal
java -classpath ../src tree.Yal Programs/max.yal
java -classpath ../src tree.Yal Programs/max1.yal
java -classpath ../src tree.Yal Programs/nested-branch.yal
java -classpath ../src tree.Yal Programs/maxmin.yal
java -classpath ../src tree.Yal Programs/programa1.yal
java -classpath ../src tree.Yal Programs/programa2.yal
java -classpath ../src tree.Yal Programs/programa3.yal
java -classpath ../src tree.Yal Programs/register-test.yal
java -classpath ../src tree.Yal Programs/return.yal
java -classpath ../src tree.Yal Programs/sqrt.yal
java -classpath ../src tree.Yal Programs/stack-size.yal