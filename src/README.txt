PROJECT TITLE: Compiler of the yal 0.4 language to Java Bytecodes

GROUP: 42
NAME1: Bruno Manuel Nascimento Costa Galvinas Piedade, NR1: 201505668, GRADE1: 17, CONTRIBUTION1: 33 1/3 %
NAME2: Diogo Luis Rey Torres, NR2: 201506428, GRADE2: 17, CONTRIBUTION2: 33 1/3 %
NAME3: João Paulo Madureira Damas, NR3: 201504088, GRADE3: 17, CONTRIBUTION3: 33 1/3%

GLOBAL Grade of the project: 17

SUMMARY: Our tool takes a program in yal 0.4 syntax and performs extensive syntactic and semantic analysis on the code given.
If no problems are found, the program is converted into a jasmin (.j) file, with the respective syntax, ready to be compiled with said tool into Java Bytecodes.
Therefore, the tool can be summarized in these steps:
- Syntactic analysis (with error output, aborting if there are errors)
- Semantic analysis (with warning and error output, aborting if there are errors)
- Code generation (with optimizations in regards to the instructions used, such as constant loading, use of i<op>_n instead of i<op> n when possible, use of iinc, etc)

EXECUTE: java -jar yal2jvm.jar <input_yal_file:string> [OPTIONAL debug:1 for debugging messages, anything else no messages]

DEALING WITH SYNTACTIC ERRORS: Syntactic analysis was done with the goal to collect as many errors as possible. Therefore, a recovery mechanism was implemented so as to avoid the parser exiting abruptly. This mechanism involves, when encountering an error, trying to skip to a specific token that should isolate the error and allow  the parser to proceed normally, like the error was never there. Sometimes, in order to be less specific and try to guarantee even further that the error can be correctly isolated, the parser skips to not necessarily one, but one in a possible set of tokens. This recovery mechanism is implemented via Java methods, namely error_skipto, error_skipto_lookahead and error_multskipto (the last one using multiple possibilities for recovery tokens). It is used extensively throughout the grammar, but main uses can be found in the Stmt(), Assign(), If() and Call() productions. This way, syntactic analysis does not exit after the first error it encounters.

SEMANTIC ANALYSIS: (Refer the semantic rules implemented by your tool.)

INTERMEDIATE REPRESENTATIONS (IRs): The AST, result of the syntactic analysis, is printed when this stage is complete.
The AST was used directly with the Symbol Table to generate the code. As register-level optimizations were not made, there is no generation of a LLIR.

CODE GENERATION: (when applicable, describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)

OVERVIEW: (refer the approach used in your tool, the main algorithms, the third-party tools and/or packages, etc.)

TESTSUITE AND TEST INFRASTRUCTURE: (Describe the content of your testsuite regarding the number of examples, the approach to automate the test, etc.)

TASK DISTRIBUTION:
Bruno Piedade -
Diogo Torres -
João Damas -

PROS: Performs very thorough syntactic and semantic analysis, ensuring that a program that passes the tool is, indeed, valid.
In each function, the best value for .limit locals and .limit stack is used, so as to not use more space than needed (not including register optimization).
Also, it covers a great deal of specific details, such as initializing any type of array (local or global) through a single value, multiple nested scopes, ... //TODO: Complete

CONS: Although it optimizes loop generation (the best template is used) and tries to generate the best code for each scenario (e.g. constants are always loaded the most efficient way possible, iinc is always used whenever possible to increment local variables), it does not perform register optimization nor constant propagation.
