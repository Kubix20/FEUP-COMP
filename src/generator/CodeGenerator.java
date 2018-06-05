package generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import semantic.*;
import tree.*;

/**
	* Performs the final code generation (no register optimization)
	*/
public class CodeGenerator {

	// Root node
	private SimpleNode root;
	// Symbol Table
	private SymbolTable st;
	// Current function
	public Function currFunction;
	// .j output file
	private FileOutputStream fileOut;
	private PrintStream fileStream;
	// Limit stack value
	private int stacklimit;
	// Helper for bounding stack limit
	private int stackval;
	// Number of local variables to current function (for .limit locals)
	private int localsval;
	// Loop label number (to generate unique loop labels)
	private int loopLabelNr;
	// If label number (to generate unique if labels)
	private int ifLabelNr;
	// Does this module have global attributes?
	private boolean hasGlobalAttrs;
	// Does this module have initialized global attributes?
	private boolean hasGlobalAttrsInits;
	// Module's global arrays
	private ArrayList<Declaration> globalArraysInit;
	// Helpers for array initialization through integer (e.g. a[100]; a=1;)
	private Declaration counter; // Counter for loop iteration when assigning values to array elements
	private Declaration arrayValueAssignRhs;

	/**
		* Inits a code generator instance
		* @param inputFile input .yal file (so as to construct output file)
		* @param root root tree node
		* @param st symbol table
		*/
	public CodeGenerator(File inputFile, SimpleNode root, SymbolTable st) {
		String path = inputFile.getAbsolutePath().split(inputFile.getName())[0];
		String outFileName = inputFile.getName().split("\\.")[0] + ".j";

		File file = new File(path+outFileName);
		try{
			fileOut = new FileOutputStream(file);
		}
		catch(IOException e){}
		fileStream = new PrintStream(this.fileOut);
		this.st = st;
		this.root = root;
		this.loopLabelNr = 0;
		this.ifLabelNr = 0;
		hasGlobalAttrs = false;
		hasGlobalAttrsInits = false;
		globalArraysInit = new ArrayList<Declaration>();
	}

	/**
		* Initiates the code generation
		* @throws IOException (in case writing to output file fails)
		*/
	public void generate() throws IOException {
		fileStream.println(".class public " + st.module);
		fileStream.println(".super java/lang/Object"+System.lineSeparator());

		generateGlobals(root);
		if(hasGlobalAttrs)
			fileStream.println();

		if(hasGlobalAttrsInits)
			generateClinit(root);

		generateFunctions(root);

		fileStream.close();
		fileOut.close();
	}

	/**
		* Generates the code for the module's global attributes
		* @param node root node
		* @throws IOException (in case writing to output file fails)
		*/
	private void generateGlobals(SimpleNode node) throws IOException {

		Declaration var;
		for(String name : st.globalDeclarations.keySet())
		{
			var = st.globalDeclarations.get(name);

			if (var.isInt()){
				if(var.value != 0) //TODO: check this
					fileStream.println(".field static " + var.name + " I = "+var.value);
				else
					fileStream.println(".field static " + var.name + " I");
			}
			else if (var.isArray()){
				fileStream.println(".field static " + var.name + " [I");
				if(var.init){
					globalArraysInit.add(var);
					hasGlobalAttrsInits = true;
				}
			}

			hasGlobalAttrs=true;
		}

	}

	/**
		* Generates code for a declaration statement
		* @param var declaration to be generated
		* @throws IOException (in case writing to output file fails)
		*/
	public void generateDeclaration(Declaration var) throws IOException{
		//Initialize array
		loadConst(var.size);
		fileStream.println("newarray int");
		storeGlobal(var);
		fileStream.println();

		//Set value to all elements if needed
		if(var.value != 0)
			generateArrayValueAssign(var,var.value);
	}

	/**
		* Generates the special static initialization method <clinit>
		* @param node root node
		* @throws IOException (in case writing to output file fails)
		*/
	public void generateClinit(SimpleNode node) throws IOException {

		stacklimit = 0;
		stackval = 0;
		localsval = 0;
		counter = null;
		arrayValueAssignRhs = null;

		fileStream.println(".method static public <clinit>()V");

		fileStream.print(".limit locals    \n");
		long localspos = fileOut.getChannel().position() - 4;
		fileStream.print(".limit stack    \n");
		long stackpos = fileOut.getChannel().position() - 4;
		fileStream.println();

		for(Declaration var : globalArraysInit){
			generateDeclaration(var);
		}

		//Update limit locals and limit stack
		long tmp = fileOut.getChannel().position();
		fileOut.getChannel().position(localspos);
		fileStream.print(localsval);
		fileOut.getChannel().position(stackpos);
		fileStream.print(stacklimit);
		fileOut.getChannel().position(tmp);

		fileStream.println("return");
		fileStream.println(".end method");
		fileStream.println();
	}

	/**
		* Generates the code for all module functions
		* @param node root node
		* @throws IOException (in case writing to output file fails)
		*/
	private void generateFunctions(SimpleNode node) throws IOException{
		int children = node.jjtGetNumChildren();

		SimpleNode child;
		for(int i=0; i< children; i++)
		{
			child = (SimpleNode) node.jjtGetChild(i);

			if(child.getId() == YalTreeConstants.JJTFUNCTION)
			{
				generateFunction(child);
				fileStream.println();
			}
		}
	}

	/**
		* Generates code for a specific function
		* @param node function node
		* @throws IOException (in case writing to output file fails)
		*/
	private void generateFunction(SimpleNode node) throws IOException {
		String name = node.getValue();
		if (name.indexOf(".")!=-1) {
			name = name.substring(name.indexOf(".")+1);
		}

		currFunction = st.functions.get(name);

		stacklimit = 0;
		stackval = 0;
		localsval = 0;
		counter = null;
		arrayValueAssignRhs = null;

		fileStream.print(".method public static " + name + "(");

		if(name.equals("main")){
			fileStream.print("[Ljava/lang/String;)V\n");
			localsval++;
		}
		else
		{
			Declaration param;
			for (String paramName : currFunction.parameters.keySet()){
				param = currFunction.parameters.get(paramName);
				if(param.isInt())
					fileStream.print("I");
				else
					fileStream.print("[I");
				param.local = localsval;
				localsval++;
			}
			fileStream.print(")");

			if(currFunction.ret.isInt())
				fileStream.print("I");
			else if (currFunction.ret.isArray())
				fileStream.print("[I");
			else if (currFunction.ret.type.compareTo("void") == 0)
				fileStream.print("V");

			fileStream.println();
		}

		if(currFunction.ret.type.compareTo("void")!=0)
		{
			currFunction.ret.local=localsval;
			localsval++;
		}

		fileStream.print(".limit locals    \n");
		long localspos = fileOut.getChannel().position() - 4;
		fileStream.print(".limit stack    \n");
		long stackpos = fileOut.getChannel().position() - 4;

		fileStream.println();

		SimpleNode child;
		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			child = (SimpleNode) node.jjtGetChild(i);
			if(child.getId() == YalTreeConstants.JJTSTMTLST)
			{
				generateStmtlst(child);
			}
		}

		if(currFunction.ret.type.compareTo("void")!=0)
		{
			loadLocal(currFunction.ret);

			if(currFunction.ret.isInt())
				fileStream.println("ireturn");
			else
				fileStream.println("areturn");
		}
		else
			fileStream.println("return");

		fileStream.println(".end method");

		//escrever o limit stack e limit locals, no inicio da funcao
		long tmp = fileOut.getChannel().position();
		fileOut.getChannel().position(localspos);
		fileStream.print(localsval);
		fileOut.getChannel().position(stackpos);
		fileStream.print(stacklimit);

		fileOut.getChannel().position(tmp);
	}

	private void generateStmtlst(SimpleNode node) throws IOException {

		SimpleNode child;
		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			child = (SimpleNode) node.jjtGetChild(i);

			if(child.getId() == YalTreeConstants.JJTASSIGN)
			{

				System.out.println("Generating assign...");
				generateAssign(child);
				fileStream.println();
			}
			if(child.getId() == YalTreeConstants.JJTCALL)
			{
				System.out.println("Generating call...");
				generateCall(child);
				fileStream.println();
			}
			if(child.getId() == YalTreeConstants.JJTIF)
			{
				System.out.println("Generating if...");
				generateIf(child);
				fileStream.println();
			}
			if(child.getId() == YalTreeConstants.JJTWHILE)
			{
				System.out.println("Generating while...");
				generateWhile(child);
				fileStream.println();
			}
		}
	}

	/**
		* Generates code for a call statement
		* @param node call statement node
		* @throws IOException (in case writing to output file fails)
		*/
	private void generateCall(SimpleNode node) throws IOException{
		String mod = "";
		String func = "";
		String params = "";
		String ret = "";
		int nparam = 0;

		String name = node.getValue();
		int dotIndex;
		if((dotIndex = name.indexOf("."))!=-1){
			mod = name.substring(0,dotIndex);
			func = name.substring(dotIndex+1,name.length());
		}
		else{
			func = name;
			mod = st.module;
		}

		if(node.jjtGetNumChildren() != 0){
			params = generateArgList((SimpleNode) node.jjtGetChild(0));
			nparam = node.jjtGetChild(0).jjtGetNumChildren();
		}
		else
			params = "()";

		if (mod.compareTo("io") == 0){
			if ((func.compareTo("print") == 0) || (func.compareTo("println") == 0) || (func.compareTo("write") == 0))
				ret = "V";
			else
				ret = "I";
		}
		else if(mod.compareTo(st.module) == 0){

			if(st.functions.get(func).ret.isInt())
				ret = "I";
			else if(st.functions.get(func).ret.isArray())
				ret = "[I";
			else
				ret = "V";
		}
		else
			ret ="I";

		fileStream.println("invokestatic "+mod+"/"+func+params+ret);
		updateStack(-nparam);
	}

	/**
		* Gets the return type for a call statement
		* @return return call type as a string
		*/
	private String getCallType(SimpleNode node){
		String type;
		String mod = "";
		String func = "";
		String params = "";
		String ret = "";
		int nparam = 0;

		String name = node.getValue();
		int dotIndex;
		if((dotIndex = name.indexOf("."))!=-1){
			mod = name.substring(0,dotIndex);
			func = name.substring(dotIndex+1,name.length());
		}
		else{
			func = name;
			mod = st.module;
		}


		if(mod.compareTo(st.module) == 0)
			type = st.functions.get(func).ret.type;
		else
			type = "integer";

		return type;
	}

	/**
		* Generates code for argument list in function call
		* @param node
		* @return argument list code as a string
		* @throws IOException
		*/
	public String generateArgList(SimpleNode node) throws IOException{
		String ret = "(";

		SimpleNode arg;
		Declaration local;
		for(int i = 0; i<node.jjtGetNumChildren(); i++)
		{
			arg = (SimpleNode)node.jjtGetChild(i);
			String val = arg.getValue();

			if(val.indexOf("\"") !=-1)
			{
				loadString(val);
				ret+="Ljava/lang/String;";
			}
			else if(SymbolTable.isInt(val)){
				loadConst(Integer.parseInt(val));
				ret+="I";
			}
			else{

				Declaration var = lookupVar(val);
				loadVar(var);

				if(var.isInt())
					ret+="I";
				else if (var.isArray())
					ret+="[I";
			}
		}
		ret+=")";
		return ret;
	}

	/**
		* Generates code for an access statement
		* @param node access statement node
		* @throws IOException
		*/
	private void generateAccess(SimpleNode node) throws IOException{
		String name = node.getValue();
		boolean sizeAccess = false;

		if(name.indexOf(".")!=-1){
			name = name.substring(0, name.indexOf("."));
			sizeAccess = true;
		}

		Declaration var = lookupVar(name);
		loadVar(var);

		if(node.jjtGetNumChildren() == 1){
			String indexName = ((SimpleNode) node.jjtGetChild(0)).getValue();
			if(SymbolTable.isInt(indexName)){
				loadConst(Integer.parseInt(indexName));
			}
			else{
				Declaration index = lookupVar(indexName);
				loadVar(index);
			}

			fileStream.println("iaload");
			updateStack(-1);
		}
		else if(sizeAccess){
			fileStream.println("arraylength");
		}
	}

	/**
		* Gets the access type
		* @param node access node
		* @return access type as a string
		*/
	private String getAccessType(SimpleNode node){
		String type;
		String name = node.getValue();
		boolean sizeAccess = false;

		if(name.indexOf(".")!=-1){
			name = name.substring(0, name.indexOf("."));
			sizeAccess = true;
		}

		Declaration var = lookupVar(name);
		type = var.type+"."+var.local;

		if(node.jjtGetNumChildren() == 1){
			type = "arrayaccess.";
		}
		else if(sizeAccess){
			type = "sizeaccess.";
		}

		return type;
	}

	/**
		* Generates code for an access in the LHS of an assign statement
		* @param node LHS access node
		* @return access as a Declaration object with respective code generation attributes set
		* @throws IOException
		*/
	private Declaration generateAccessAssign(SimpleNode node) throws IOException{
		String name = node.getValue();

		Declaration var = lookupVarAssign(name);
		System.out.println(var.name);

		//IMPORTANT
		if(!var.global && var.local == -1){
			var.local = localsval;
			localsval++;
		}

		if(var.isArray()){
			if(node.jjtGetNumChildren() == 1){

				var.access = "array";
				loadVar(var);

				String indexName = ((SimpleNode) node.jjtGetChild(0)).getValue();
				if(SymbolTable.isInt(indexName)){
					loadConst(Integer.parseInt(indexName));
				}
				else{
					Declaration index = lookupVar(indexName);
					loadVar(index);
				}

				var.access="integer";
			}
			else
				var.access="array";
		}

		return var;
	}

	/**
		* Generates code for an array creation
		* @param node array size node
		* @throws IOException (in case writing to output file fails)
		*/
	private void generateArraySize(SimpleNode node) throws IOException{
		if(node.jjtGetNumChildren() > 0){
			generateAccess((SimpleNode) node.jjtGetChild(0));
		}
		else{
			String val = node.getValue();
			loadConst(Integer.parseInt(val));
		}

		fileStream.println("newarray int");
	}

	/**
		* Generates code for an assign statement
		* @param node assign node
		* @throws IOException (in case writing to output file fails)
		*/
	private void generateAssign(SimpleNode node) throws IOException {
		boolean storeVar = true;
		Declaration lhs = generateAccessAssign((SimpleNode) node.jjtGetChild(0));
		System.out.println(lhs.ifStatus);

		SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);
		int children = rhs.jjtGetNumChildren();

		SimpleNode term1 = (SimpleNode) rhs.jjtGetChild(0);

		if(term1.getId() == YalTreeConstants.JJTTERM){

			String[] term1TypeParts = getTermType(term1).split("\\.");
			String term1Type = term1TypeParts[0];
			String term1Value = "";
			if(term1TypeParts.length > 1)
				term1Value = term1TypeParts[1];

			//Temporarily update the variable's type if defined on an if else statement
			if(lhs.incompatibleIfStatus()){
				String branchType = getVarType(term1Type);
				if(lhs.newIfBranch){
					System.out.println("Update: "+branchType);
					lhs.type = branchType;
					lhs.access = "";
					lhs.newIfBranch = false;
				}
			}

			if(children == 2){

				SimpleNode term2 = (SimpleNode) rhs.jjtGetChild(1);

				String[] term2TypeParts = getTermType(term2).split("\\.");
				String term2Type = term2TypeParts[0];
				String term2Value = "";
				if(term2TypeParts.length > 1)
					term2Value = term2TypeParts[1];

				//var = var+integer
				if(!lhs.global && term1Type.compareTo("integer")==0 && term1Value.compareTo(""+lhs.local)==0
				   && term2Type.compareTo("smallint")==0 && rhs.getValue().compareTo("+")==0){

					storeVar = false;
					fileStream.println("iinc "+lhs.local+" "+term2Value);
				}
				//var = integer+var
				else if(!lhs.global && term1Type.compareTo("smallint")==0 && rhs.getValue().compareTo("+")==0
					    && term1Type.compareTo("integer")==0 && term1Value.compareTo(""+lhs.local)==0){

					storeVar = false;
					fileStream.println("iinc "+lhs.local+" "+term1Value);
				}

				else{
					generateTerm(term1);
					generateTerm(term2);
					fileStream.println(arithmeticOpToStr(rhs.getValue()));

					//array = integer+integer
					if(lhs.arrayAccess()){
						storeVar = false;
						System.out.println("Assign array to value");
						generateArrayValueAssign(lhs,null);
					}
				}
			}
			else{

				if(lhs.arrayAccess()){

					//array = constant
					if(term1Type.compareTo("smallint")==0 || term1Type.compareTo("bigint")==0){
						storeVar = false;
						generateArrayValueAssign(lhs,Integer.valueOf(term1Value));
					}
					//array = integer
					else if(term1Type.compareTo("array")!=0){
						storeVar = false;
						generateTerm(term1);
						generateArrayValueAssign(lhs,null);
					}
					else
						generateTerm(term1);
				}
				else
					generateTerm(term1);
			}
		}
		else

		if(term1.getId() == YalTreeConstants.JJTARRAYSIZE){

			//Temporarily update the variable's type if defined on an if else statement
			if(lhs.incompatibleIfStatus()){
				if(lhs.newIfBranch){
					System.out.println("Update: array");
					lhs.type = "array";
					lhs.access = "";
					lhs.newIfBranch = false;
				}
			}
			generateArraySize(term1);
		}

		if(storeVar)
			storeVar(lhs);
	}

	/**
		* Get a term's type (delegates to respective and more specific getType functions)
		* @param node term node
		* @return term type as a string
		*/
	private String getTermType(SimpleNode node){
		String type = "";
		String parts = node.getValue();
		String op = "";
		String value = "";
		if(parts.charAt(0) == ' '){
			value = parts.trim();
		}
		else{
			op = ""+parts.charAt(0);
			value = parts.substring(1,parts.length()).trim();
		}

		if(node.jjtGetNumChildren() == 1){
			SimpleNode child = (SimpleNode) node.jjtGetChild(0);

			if(child.getId() == YalTreeConstants.JJTACCESS){
				type = getAccessType(child);
			}
			else

			if(child.getId() == YalTreeConstants.JJTCALL){
				type = getCallType(child);
			}
		}
		else{
			type = getConstType(op+value);
		}

		return type;
	}

	/**
		* Generates code for a term node
		* @param node term node
		* @throws IOException (in case writing to output file fails)
		*/
	private void generateTerm(SimpleNode node) throws IOException {
		String parts = node.getValue();
		String op = "";
		String value = "";
		if(parts.charAt(0) == ' '){
			value = parts.trim();
		}
		else{
			op = ""+parts.charAt(0);
			value = parts.substring(1,parts.length()).trim();
		}

		if(node.jjtGetNumChildren() == 1){
			SimpleNode child = (SimpleNode) node.jjtGetChild(0);

			if(child.getId() == YalTreeConstants.JJTACCESS){
				generateAccess(child);
			}
			else

			if(child.getId() == YalTreeConstants.JJTCALL){
				generateCall(child);
			}
		}
		else{
			if(op.compareTo("-")==0)
				value = op+value;

			loadConst(Integer.parseInt(value));
		}
	}

	/**
		* Generates code for a conditional test (used in if and while statements)
		* @param node conditional test node
		* @param label conditional label to be used
		* @throws IOException (in case writing to output file fails)
		*/
	private void generateExprtest(SimpleNode node, String label) throws IOException {
		generateAccess((SimpleNode) node.jjtGetChild(0));

		SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);
		int children = rhs.jjtGetNumChildren();

		SimpleNode term1 = (SimpleNode) rhs.jjtGetChild(0);
		String[] term1TypeParts = getTermType(term1).split("\\.");
		String term1Type = term1TypeParts[0];
		String term1Value = "";
		if(term1TypeParts.length > 1)
			term1Value = term1TypeParts[1];

		if(children == 2){
			generateTerm(term1);
			SimpleNode term2 = (SimpleNode) rhs.jjtGetChild(1);
			generateTerm(term2);
			fileStream.println(arithmeticOpToStr(rhs.getValue()));
			fileStream.print(comparisonOpToStr(node.getValue()));
		}
		else{
			if(term1Type.compareTo("smallint")==0 && term1Value.compareTo("0")==0){
				fileStream.print(comparisonZeroOpToStr(node.getValue()));
			}
			else{
				generateTerm(term1);
				fileStream.print(comparisonOpToStr(node.getValue()));
			}
		}

		fileStream.println(" "+label);
	}

	/**
		* Generates code for an if statement
		* @param node if statement node
		* @throws IOException (in case writing to output file fails)
		*/
	private void generateIf(SimpleNode node) throws IOException {
		int children = node.jjtGetNumChildren();
		int labelNr = this.ifLabelNr;
		this.ifLabelNr++;

		String elseLabel = "else"+labelNr;
		String ifLabelEnd = "if"+labelNr+"_end";
		String exprLabel = ifLabelEnd;

		if(children > 2)
			exprLabel = elseLabel;

		generateExprtest((SimpleNode) node.jjtGetChild(0),exprLabel);
		fileStream.println();
		setLocalsNewIfBranch();
		generateStmtlst((SimpleNode) node.jjtGetChild(1));

		//else clause
		if(children > 2){
			fileStream.println("goto "+ifLabelEnd);
			fileStream.println();
			fileStream.println(elseLabel+":");
			setLocalsNewIfBranch();
			generateStmtlst((SimpleNode) node.jjtGetChild(2));
		}

		fileStream.println();
		fileStream.println(ifLabelEnd+":");
		fileStream.println();
	}

	/**
		* Generates code for a while statement
		* @param node while statement node
		* @throws IOException (in case writing to output file fails)
		*/
	private void generateWhile(SimpleNode node) throws IOException {
		int labelNr = this.loopLabelNr;
		this.loopLabelNr++;

		String loopLabel = "loop"+labelNr;
		String loopLabelEnd = "loop"+labelNr+"_end";

		fileStream.println(loopLabel+":");
		fileStream.println();

		generateExprtest((SimpleNode) node.jjtGetChild(0),loopLabelEnd);

		fileStream.println();

		generateStmtlst((SimpleNode) node.jjtGetChild(1));
		fileStream.println("goto "+loopLabel);

		fileStream.println();
		fileStream.println(loopLabelEnd+":");
		fileStream.println();
	}

	/**
		* Generates code for special array initialization (e.g. a[100]; a=1;)
		* @param array array to be initialized
		* @param val value to initialize all array elements with
		* @throws IOException (in case writing to output file fails)
		*/
	private void generateArrayValueAssign(Declaration array, Integer val) throws IOException{
		int labelNr = this.loopLabelNr;
		this.loopLabelNr++;

		String loopLabel = "loop"+labelNr;
		String loopLabelEnd = "loop"+labelNr+"_end";

		if(counter == null){
			counter = new Declaration("counter","integer",false);
			counter.local = localsval;
			localsval++;
		}

		//Not a constant
		if(val==null){
			if(arrayValueAssignRhs==null){
				arrayValueAssignRhs = new Declaration("arrayValueAssignRhs","integer",false);
				arrayValueAssignRhs.local = localsval;
				localsval++;
			}
			storeVar(arrayValueAssignRhs);
		}

		//Set counter to 0
		loadConst(0);
		storeVar(counter);

		//Check end of loop: counter < array.length
		fileStream.println(loopLabel+":");
		loadVar(counter);
		loadVar(array);
		fileStream.println("arraylength");
		fileStream.println(comparisonOpToStr("<")+" "+loopLabelEnd);

		//Set array at counter position: array[counter] = val
		loadVar(array);
		loadVar(counter);
		if(val==null)
			loadVar(arrayValueAssignRhs);
		else
			loadConst(val.intValue());
		fileStream.println("iastore");
		updateStack(-3);


		//Increment counter
		fileStream.println("iinc "+counter.local+" 1");
		fileStream.println("goto "+loopLabel);

		fileStream.println(loopLabelEnd+":");
		fileStream.println();
	}

	/**
		* Generates code for storing a variable
		* @param var variable to be stored
		* @throws IOException (in case writing to output file fails)
		*/
	private void storeVar(Declaration var) throws IOException{
		if(var.isArray() && var.intAccess()){
			fileStream.println("iastore");
			updateStack(-3);
		}
		else{
			if(var.global)
				storeGlobal(var);
			else
				storeLocal(var);
		}
	}

	/**
		* Generates code to store a local variable
		* @param var local variable to be stored
		* @throws IOException (in case writing to output file fails)
		*/
	private void storeLocal(Declaration var) throws IOException{
		if(var.local==-1)
		{
			var.local = localsval;
			localsval++;
		}

		String type;
		if(var.isInt())
			type="i";
		else
			type="a";

		int i = var.local;
		if(i>3)
			fileStream.println(type+"store "+i);
		else
			fileStream.println(type+"store_"+i);

		updateStack(-1);
	}

	/**
		* Generates code to store a global variable
		* @param var global variable to be stored
		* @throws IOException (in case writing to output file fails)
		*/
	private void storeGlobal(Declaration var) throws IOException{
		fileStream.print("putstatic " + st.module + "/" + var.name );
		if(var.isInt())
			fileStream.println(" I");
		else
			fileStream.println(" [I");
		updateStack(-1);
	}

	/**
		* Generates code to load a variable
		* @param var variable to be loaded
		* @throws IOException (in case writing to output file fails)
		*/
	private void loadVar(Declaration var) throws IOException{
		if(var.global)
			loadGlobal(var);
		else
			loadLocal(var);
	}

	/**
		* Generates code to load a local variable
		* @param var local variable to be loaded
		* @throws IOException (in case writing to output file fails)
		*/
	private void loadLocal(Declaration var) throws IOException{
		String type;
		if(var.isInt())
			type="i";
		else
			type="a";

		int i=var.local;
		if(i<0)
			return;

		if(i>3)
			fileStream.println(type+"load "+i);
		else
			fileStream.println(type+"load_"+i);

		updateStack(1);
	}

	/**
		* Generates code to load a global variable
		* @param var global variable to be loaded
		* @throws IOException (in case writing to output file fails)
		*/
	private void loadGlobal(Declaration var) throws IOException{
		fileStream.print("getstatic " + st.module + "/" + var.name);
		if(var.isInt())
			fileStream.println(" I");
		else
			fileStream.println(" [I");
		updateStack(1);
	}

	/**
		* Generates (optimized) code to load a constant
		* @param val constant to be loaded
		* @throws IOException (in case writing to output file fails)
		*/
	private void loadConst(int val) throws IOException{
		//iconst - otimizado para inteiros 0 a 5
		if(val >= 0 && val <= 5)
			fileStream.println("iconst_" + val);
		else if(val == -1)
			fileStream.println("iconst_m1");
		//bipush - otimizado para inteiros representaveis em 1 byte
		else if(val>=-128 && val<=127)
			fileStream.println("bipush " + val);
		//sipush - otimizado para inteiros representaveis em 2 bytes
		else if(val>=-32768 && val<=32767)
			fileStream.println("sipush " + val);
		//nao ha otimizacoes possiveis para carregar a constante
		else
			fileStream.println("ldc " + val);

		updateStack(1);
	}

	/**
		* Generates code to load a string
		* @param str string to be loaded
		* @throws IOException (in case writing to output file fails)
		*/
	public void loadString(String str) throws IOException{
		fileStream.println("ldc " + str);
		updateStack(1);
	}

	/**
		* Gets a variable's type
		* @param type variable to be tested
		* @return variable type (integer or array)
		*/
	private String getVarType(String type){
		if(type.compareTo("array")==0)
			return "array";
		else
			return "integer";
	}

	/**
		* Gets a constant value's type (big or small integer; serves as a helper identifier only)
		* @param value constant value as a string
		* @return value as a string with corresponding smallint (if in [-127,127[) or bigint (otherwise) prefix
		*/
	private String getConstType(String value){
		int val = Integer.parseInt(value);
		if(val > -128 && val < 127)
			return "smallint."+value;
		else
			return "bigint."+value;
	}

	/**
		* Gets the code representation of an arithmetic operator
		* @param op arithmetic operator
		* @return operator's representation in target syntax
		*/
	public String arithmeticOpToStr(String op){
		String res = "";
		switch(op) {
			case "+":
				res = "iadd";
				break;
			case "-":
				res = "isub";
				break;
			case "*":
				res = "imul";
				break;
			case "/":
				res = "idiv";
				break;
			case "<<":
				res = "ishl";
				break;
			case ">>":
				res = "ishr";
				break;
			case "&":
				res = "iand";
				break;
			case "|":
				res = "ior";
				break;
			default:
				break;
		}
		updateStack(-1);
		return res;
	}

	/**
		* Gets the code representation for the negation (due to how jumps are made) of a comparison operator
		* @param op comparison operator
		* @return operator's negation's representation in target syntax
		*/
	public String comparisonOpToStr(String op){
		String res = "";
		// os saltos a realizar serao com base no inverso por isso devolve-se a operacao complementar
		// ex: se condicao if(a > b) o salto sera feito quando a <= b
		switch(op) {
			case "==":
				res = "if_icmpne";
				break;
			case "!=":
				res = "if_icmpeq";
				break;
			case "<=":
				res = "if_icmpgt";
				break;
			case ">=":
				res = "if_icmplt";
				break;
			case ">":
				res = "if_icmple";
				break;
			case "<":
				res = "if_icmpge";
				break;
			default:
				break;
		}

		updateStack(-2);
		return res;
	}

	/**
		* Gets the representation for the negation (due to how jumps are made) of a special case of comparison operator (when comparing to 0)
		* @param op comparison operator
		* @return operator's negation's representation in target syntax
		*/
	public String comparisonZeroOpToStr(String op){
		String res = "";
		// os saltos a realizar serao com base no inverso por isso devolve-se a operacao complementar
		// ex: se condicao if(a > b) o salto sera feito quando a <= b
		switch(op) {
			case "==":
				res = "ifne";
				break;
			case "!=":
				res = "ifeq";
				break;
			case "<=":
				res = "ifgt";
				break;
			case ">=":
				res = "iflt";
				break;
			case ">":
				res = "ifle";
				break;
			case "<":
				res = "ifge";
				break;
			default:
				break;
		}

		updateStack(-1);
		return res;
	}

	/**
		* Updates the stackval helper by a given factor and, if necessary, the stack limit
		* @param factor factor to update stackval
		*/
	private void updateStack(int factor){
		stackval = stackval + factor;
		if(stackval < 0) stackval = 0;
		if(stackval > stacklimit) stacklimit = stackval;
	}

	/**
		* Set a new if branch in current function
		*/
	private void setLocalsNewIfBranch(){
		for(String name : currFunction.localDeclarations.keySet())
			currFunction.localDeclarations.get(name).newIfBranch = true;
	}

	/**
		* Looks up recursively a variable
		* @param name variable name
		* @return variable as a Declaration object
		*/
	private Declaration lookupVar(String name){
		if(currFunction != null){
			if(currFunction.localDeclarations.containsKey(name)){
				return currFunction.localDeclarations.get(name);
			}

			if(currFunction.parameters.containsKey(name)){
				return currFunction.parameters.get(name);
			}

			if(currFunction.ret.name.compareTo(name)==0){
				return currFunction.ret;
			}
		}

		if(st.globalDeclarations.containsKey(name))
			return st.globalDeclarations.get(name);

		return null;
	}

	/**
		* Looks up recursively a variable in an assign statement
		* @param name variable name
		* @return variable as a Declaration object
		*/
	private Declaration lookupVarAssign(String name){
		if(currFunction != null){
			if(currFunction.localDeclarations.containsKey(name)){
				return currFunction.localDeclarations.get(name);
			}

			if(currFunction.ret.name.compareTo(name)==0){
				return currFunction.ret;
			}

			if(currFunction.parameters.containsKey(name)){
				return currFunction.parameters.get(name);
			}
		}

		if(st.globalDeclarations.containsKey(name))
			return st.globalDeclarations.get(name);

		return null;
	}
}
