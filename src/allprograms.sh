!/bin/bash
jjtree Yal.jjt
javacc tree/Yal.jj
javac generator/*.java
javac semantic/*.java
javac tree/*.java
java tree.Yal Programs/array1.yal
java tree.Yal Programs/array2.yal
java tree.Yal Programs/aval1.yal
java tree.Yal Programs/aval2.yal
java tree.Yal Programs/aval3.yal
java tree.Yal Programs/aval4.yal
java tree.Yal Programs/aval5.yal
java tree.Yal Programs/aval6.yal
java tree.Yal Programs/aval7.yal
java tree.Yal Programs/aval8.yal
java tree.Yal Programs/call-main.yal
java tree.Yal Programs/constant-ranges.yal
java tree.Yal Programs/erro_prof.yal
java tree.Yal Programs/library1.yal
java tree.Yal Programs/max.yal
java tree.Yal Programs/max1.yal
java tree.Yal Programs/nested-branch.yal
java tree.Yal Programs/maxmin.yal
java tree.Yal Programs/programa1.yal
java tree.Yal Programs/programa2.yal
java tree.Yal Programs/programa3.yal
java tree.Yal Programs/register-test.yal
java tree.Yal Programs/return.yal
java tree.Yal Programs/sqrt.yal
java tree.Yal Programs/stack-size.yal