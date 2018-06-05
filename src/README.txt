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

SEMANTIC ANALYSIS: The compiler enforces and displays an error message whenever the following main semantic rules are not followed:

Global Declarations:

	In the global declarations a variable may be only declared and not initialized and in this case it will not have a defined type (unless it is explicitly declared as var[], in which case it will be considered as an array). Its type will be later defined when it is first assigned to a value.
	Otherwise the variables can be declared and assigned in a single statement.
	Arrays can be assigned to a value (i.e. a[] = 2) which assigns the value to all of its elements.
	Whenever a array is (re)initialized with a [size] statement all elements default to 0.
	Index ranges for array accesses are checked.
	
	Ex:
	
		a; <- undefined type
		a=[10]; <- first assignment, creates an array with 10 elements
		a=1; <- sets all elements to 1.
		b[]; <- unitialized array
		b=1; <- error, assignment to unitialized array
		b=[5]; <- ok
		b[-1] <- error, index out of bounds
		
	
Functions:

	Functions can be declared in any order and will always be visible. Their name must also be unique.
	When a return variable for a function is declared it must be initialized with a valid value. If the return variable and one of the parameters have the same name it will only be possible to read the parameter and assign the return value. Furthermore if an attribute also shares the same name it will be impossible to access it.
	In calls to other functions, if the function belongs to the same module both the arguments must match its definition in number and type and must be initialized and if the return is not void it must be compatible with the variable to be assigned. Otherwise it is assumed that an integer is returned unless it belongs to the io module, in which case void is returned.
	
	Ex:
		function m = f(m){
			m = m; <- initializes the return variable with the value of the parameter
			
			f2(); <- ok
			f2(m); <- error, invalid arguments
			c = otherModule/f2(); <- returns an integer
		}
		
		function f2(){}
		
		function ret = f2(){} <- error, unitialized return
		
	
Function Bodies - Variables, cicles, conditions and arithmetic expressions
	
	
	A variable can be initialized as an array or as an integer and must be assigned when it is declared and its type will be defined to one compatible with the right hand side operator.
	Arrays can be assigned to a value (i.e. a[] = 2) which assigns the value to all of its elements.
	
	Ex:
		
		a = 1+1; <- defines a as an integer
		b = a.size; <- error, size access of an integer variable
		a[0] = 1; 
		b = [a];
		b = a; <- sets all elements to a.
		b[0] = 2;
		...
		
	
	
	Variables used in arithmetic expressions and comparisons must be numeric.
	
	Ex: 
		a = 1;
		b = 2
		c = [10];
		if(a < b){...} <- ok
		if(a < 2){...} <- ok
		if(a < c.size){...} <- ok
		if(a < c){...} <- error
		d = a+b; <- ok
		d = b+c <- error
			
	In nested if statements all declarations must be compatible to be visible on upper scopes (i.e. in an if/else statement a new variable must be initialized as the same type in both branches) otherwise their usage must be restrained to the statement in question.
	
	Ex:
		a = 1;
		
		if(a == 1){
			c = 3;
		}
		else{
			c = [10];
		}
		b = c; <- error, c might reach this point as an array or an integer
		
		
		if(a == 1){
			c = 3;
		}
		else{
			b = [10];
		}
		b = c; <- error, c might reach this point unitialized

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
