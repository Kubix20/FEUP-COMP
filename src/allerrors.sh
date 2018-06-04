#!/bin/bash
jjtree Yal.jjt
javacc tree/Yal.jj
javac generator/*.java
javac semantic/*.java
javac tree/*.java
java tree.Yal ErrCode/array2_err.yal
java tree.Yal ErrCode/array4_err.yal
java tree.Yal ErrCode/aval1_err.yal
java tree.Yal ErrCode/aval2_err.yal
java tree.Yal ErrCode/aval3_err.yal
java tree.Yal ErrCode/aval4_err.yal
java tree.Yal ErrCode/aval5_err.yal
java tree.Yal ErrCode/aval6_err.yal
java tree.Yal ErrCode/aval7_err.yal
java tree.Yal ErrCode/err1.yal
java tree.Yal ErrCode/error-resize-array.yal
java tree.Yal ErrCode/error-shared-scope.yal
java tree.Yal ErrCode/error-uninitialized-output.yal