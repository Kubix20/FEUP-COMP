import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class CodeGenerator {

	private SimpleNode root;
	private SymbolTable st;
	public Function currFunction;
	private FileOutputStream fileOut;
	private PrintStream fileStream;
	private int stacklimit;
	private int stackval;
	private int localsval;
	private int loopLabelNr;
	private int ifLabelNr;
	private boolean hasGlobalAttrs;
	private boolean hasGlobalAttrsInits;
	private ArrayList<Declaration> globalArraysInit;
	private Declaration counter;
	private Declaration arrayValueAssignRhs;

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

	public void generateDeclaration(Declaration var){
		//System.out.println("Value: "+var.value+" Size: "+var.size);

		//Initialize array
		loadConst(var.size);
		fileStream.println("newarray int");
		storeGlobal(var);
		fileStream.println();

		//Set value to all elements if needed
		if(var.value != 0)
			generateArrayValueAssign(var,var.value);
	}

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
			//TODO: check this
			//localsval++;
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

	public String generateArgList(SimpleNode node){
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

	private void generateAccess(SimpleNode node){
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

	private Declaration generateAccessAssign(SimpleNode node){
		String name = node.getValue();

		Declaration var = lookupVarAssign(name);
		System.out.println(var.name);

		//IMPORTANTE
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

	private void generateArraySize(SimpleNode node){
		if(node.jjtGetNumChildren() > 0){
			generateAccess((SimpleNode) node.jjtGetChild(0));
		}
		else{
			String val = node.getValue();
			loadConst(Integer.parseInt(val));
		}

		fileStream.println("newarray int");
	}

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

	private void generateArrayValueAssign(Declaration array, Integer val){
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

	private void storeVar(Declaration var){
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

	private void storeLocal(Declaration var){
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

	private void storeGlobal(Declaration var){
		fileStream.print("putstatic " + st.module + "/" + var.name );
		if(var.isInt())
			fileStream.println(" I");
		else
			fileStream.println(" [I");
		updateStack(-1);
	}

	private void loadVar(Declaration var){
		if(var.global)
			loadGlobal(var);
		else
			loadLocal(var);
	}

	private void loadLocal(Declaration var){
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

	private void loadGlobal(Declaration var){
		fileStream.print("getstatic " + st.module + "/" + var.name);
		if(var.isInt())
			fileStream.println(" I");
		else
			fileStream.println(" [I");
		updateStack(1);
	}

	private void loadConst(int val){
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

	public void loadString(String str){
		fileStream.println("ldc " + str);
		updateStack(1);
	}

	private String getVarType(String type){
		if(type.compareTo("array")==0)
			return "array";
		else
			return "integer";
	}

	private String getConstType(String value){
		int val = Integer.parseInt(value);
		if(val > -128 && val < 127)
			return "smallint."+value;
		else
			return "bigint."+value;
	}

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

	private void updateStack(int factor){
		stackval = stackval + factor;
		if(stackval < 0) stackval = 0;
		if(stackval > stacklimit) stacklimit = stackval;
	}

	private void setLocalsNewIfBranch(){
		for(String name : currFunction.localDeclarations.keySet())
			currFunction.localDeclarations.get(name).newIfBranch = true;
	}

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
