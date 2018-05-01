import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class CodeGenerator {

	private SimpleNode root;

	private SymbolTable st;

	public Function currFunction;

	private FileOutputStream fileOut;
	private PrintStream fileStream;

	// Declarations to be included in constructor <clinit>
	private ArrayList<Declaration> initArrays;

	private int stacklimit;
	private int stackval;

	private int localsval;
	private int label;

	private boolean hasGlobalAttrs;

	public CodeGenerator(File inputFile, SimpleNode root, SymbolTable st) {
		String path = inputFile.getAbsolutePath().split(inputFile.getName())[0];
		String outFileName = inputFile.getName().split("\\.")[0] + ".j";

		File file = new File(path+outFileName);
		try{
			fileOut = new FileOutputStream(file);
		}
		catch(IOException e){
			System.out.println("Failed to create output stream");
			System.exit(-1);
		}
		fileStream = new PrintStream(this.fileOut);
		this.st = st;
		this.root = root;
		initArrays = new ArrayList<Declaration>();
		label = 0;
		hasGlobalAttrs = false;
	}

	public void generate() throws IOException {
		fileStream.println(".class public " + st.module);
		fileStream.println(".super java/lang/Object"+System.lineSeparator());

		generateGlobals(root);
		if(hasGlobalAttrs)
			fileStream.println();

		generateFunctions(root);

		fileStream.close();
		fileOut.close();
	}

	private void generateGlobals(SimpleNode node) throws IOException {
		int children = node.jjtGetNumChildren();

		SimpleNode child;
		for(int i=0; i< children; i++)
		{
			child = (SimpleNode) node.jjtGetChild(i);

			if(child.getId() == YalTreeConstants.JJTDECLARATION)
			{
				genDeclaration(child);
				hasGlobalAttrs=true;
			}
		}

	}

	private void generateFunctions(SimpleNode node) throws IOException{
		int children = node.jjtGetNumChildren();

		SimpleNode child;
		for(int i=0; i< children; i++)
		{
			child = (SimpleNode) node.jjtGetChild(i);

			if(child.getId() == YalTreeConstants.JJTFUNCTION)
			{
				genFunction(child);
				fileStream.println();
			}
		}
	}

	private void genDeclaration(SimpleNode node) throws IOException {

		SimpleNode lhs = (SimpleNode)node.jjtGetChild(0);
		String name = lhs.getValue();

		Declaration var;

		//tira os "[]" do nome caso existam
		if(name.indexOf("[]")!=-1)
		{
			name = name.substring(0, name.indexOf("["));
			var = st.globalDeclarations.get(name);
			if (var.type == null)
				var.type = "array";
		}
		else
		{
			var = st.globalDeclarations.get(name);
			if (var.type == null)
				var.type = "integer";

		}

		if (var.isInt())
		{
			if (var.init)
				fileStream.println(".field static " + var.name + " I = " + var.value);
			else
				fileStream.println(".field static " + var.name + " I");
		}
		else if (var.isArray())
		{
			fileStream.println(".field static " + var.name + " [I");
			if (var.init)
				initArrays.add(var);
		}

	}

	private void genFunction(SimpleNode node) throws IOException {
		String name = node.getValue();
		if (name.indexOf(".")!=-1) {
			name = name.substring(name.indexOf(".")+1);
		}

		currFunction = st.functions.get(name);

		stacklimit = 0;
		stackval = 0;
		localsval = 0;

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

		//System.out.println("localsval after parameters: "+localsval);
		if(currFunction.ret.type.compareTo("void")!=0)
		{
			currFunction.ret.local=localsval;
			localsval++;
		}

		fileStream.print(".limit stack    \n");
		long stackpos = fileOut.getChannel().position() - 4;
		fileStream.print(".limit locals    \n");
		long localspos = fileOut.getChannel().position() - 4;

		fileStream.println();

		SimpleNode child;
		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			child = (SimpleNode) node.jjtGetChild(i);
			if(child.getId() == YalTreeConstants.JJTSTMTLST)
			{
				genStmtlst(child);
			}
		}

		if(currFunction.ret.type.compareTo("void")!=0)
		{
			ldLocal(currFunction.ret);

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
		fileOut.getChannel().position(stackpos);
		fileStream.print(stacklimit);
		fileOut.getChannel().position(localspos);
		fileStream.print(localsval);

		fileOut.getChannel().position(tmp);
	}

	private void genStmtlst(SimpleNode node) throws IOException {

		SimpleNode child;
		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			child = (SimpleNode) node.jjtGetChild(i);

			// ASSIGN
			if(child.getId() == YalTreeConstants.JJTASSIGN)
			{

				System.out.println("Generating assign...");
				genAssign(child);
				fileStream.println();
			}
			// CALL
			if(child.getId() == YalTreeConstants.JJTCALL)
			{
				System.out.println("Generating call...");
				genCall(child);
				fileStream.println();
			}
			// WHILE
			if(child.getId() == YalTreeConstants.JJTIF)
			{
				System.out.println("Generating if...");
				genIf(child);
				fileStream.println();
			}
			// IF
			if(child.getId() == YalTreeConstants.JJTWHILE)
			{
				System.out.println("Generating while...");
				genWhile(child);
				fileStream.println();
			}
		}
	}

	private void genCall(SimpleNode node) throws IOException{
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
			//REVER ISTO NA SEMANTICA
			func = name;
			mod = st.module;
		}

		if(node.jjtGetNumChildren() != 0){
			params = argumentList((SimpleNode) node.jjtGetChild(0));
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
		changeStack(-nparam);
	}

	public String argumentList(SimpleNode node){
		String ret = "(";

		SimpleNode arg;
		Declaration local;
		for(int i = 0; i<node.jjtGetNumChildren(); i++)
		{
			arg = (SimpleNode)node.jjtGetChild(i);
			String val = arg.getValue();

			if(val.indexOf("\"") !=-1)
			{
				ldStr(val);
				ret+="Ljava/lang/String;";
			}
			else if(SymbolTable.isInt(val)){
				ldConst(Integer.parseInt(val));
				ret+="I";
			}
			else{

				Declaration var = lookupVar(val);
				ldVar(var);

				if(var.isInt())
					ret+="I";
				else if (var.isArray())
					ret+="[I";
			}
		}
		ret+=")";
		return ret;
	}

	private void genAccess(SimpleNode node){
		String name = node.getValue();
		boolean sizeAccess = false;

		if(name.indexOf(".")!=-1){
			name = name.substring(0, name.indexOf("."));
			sizeAccess = true;
		}

		Declaration var = lookupVar(name);
		ldVar(var);

		if(node.jjtGetNumChildren() == 1){
			String indexName = ((SimpleNode) node.jjtGetChild(0)).getValue();
			if(SymbolTable.isInt(indexName)){
				ldConst(Integer.parseInt(indexName));
			}
			else{
				Declaration index = lookupVar(indexName);
				ldVar(var);
			}

			fileStream.println("iaload");
			changeStack(-1);
		}
		else if(sizeAccess){
			fileStream.println("arraylength");
		}
	}

	private Declaration genAccessAssign(SimpleNode node){
		String name = node.getValue();

		Declaration var = lookupVarAssign(name);
		System.out.println(var.name);

		if(var.local == -1){
			var.local = localsval;
			localsval++;
		}

		if(var.isArray()){
			if(node.jjtGetNumChildren() == 1){
				String indexName = ((SimpleNode) node.jjtGetChild(0)).getValue();
				if(SymbolTable.isInt(indexName)){
					ldConst(Integer.parseInt(indexName));
				}
				else{
					Declaration index = lookupVar(indexName);
					ldVar(var);
				}

				var.access="integer";
			}
			else
				var.access="array";
		}

		return var;
	}

	private void genArraySize(SimpleNode node){
		if(node.jjtGetNumChildren() > 0){
			genAccess((SimpleNode) node.jjtGetChild(0));
		}
		else{
			String val = node.getValue();
			ldConst(Integer.parseInt(val));
		}

		fileStream.println("newarray int");
	}

	private void genAssign(SimpleNode node) throws IOException {
		Declaration lhs = genAccessAssign((SimpleNode) node.jjtGetChild(0));

		SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);
		int children = rhs.jjtGetNumChildren();
		SimpleNode term1 = (SimpleNode) rhs.jjtGetChild(0);

		if(term1.getId() == YalTreeConstants.JJTTERM){
			genTerm(term1);
			if(children == 2){
				SimpleNode term2 = (SimpleNode) rhs.jjtGetChild(1);
				genTerm(term2);
				fileStream.println(op2str(rhs.getValue()));
			}
		}
		else

		if(term1.getId() == YalTreeConstants.JJTARRAYSIZE){
			genArraySize(term1);
		}

		stVar(lhs);
	}

	private void genTerm(SimpleNode node) throws IOException {
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
				genAccess(child);
			}
			else

			if(child.getId() == YalTreeConstants.JJTCALL){
				genCall(child);
			}
		}
		else{
			ldConst(Integer.parseInt(value));
		}

		if(op.compareTo("-")==0)
			fileStream.println("ineg");
	}

	private void genExprtest(SimpleNode node) throws IOException {
		genAccess((SimpleNode) node.jjtGetChild(0));

		SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);
		int children = rhs.jjtGetNumChildren();
		SimpleNode term1 = (SimpleNode) rhs.jjtGetChild(0);

		if(term1.getId() == YalTreeConstants.JJTTERM){
			genTerm(term1);
			if(children == 2){
				SimpleNode term2 = (SimpleNode) rhs.jjtGetChild(1);
				genTerm(term2);
				fileStream.println(op2str(rhs.getValue()));
			}
		}

		fileStream.println(cmpop2str(node.getValue()));
	}

	private void genIf(SimpleNode node) throws IOException {
		genExprtest((SimpleNode) node.jjtGetChild(0));
		fileStream.println();
		genStmtlst((SimpleNode) node.jjtGetChild(1));
	}

	private void genWhile(SimpleNode node) throws IOException {
		genExprtest((SimpleNode) node.jjtGetChild(0));
		fileStream.println();
		genStmtlst((SimpleNode) node.jjtGetChild(1));
	}

	private void stVar(Declaration var){
		if(var.global)
			stGlobal(var);
		else
			stLocal(var);
	}

	private void stLocal(Declaration var){
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

		changeStack(-1);
	}

	private void stGlobal(Declaration var){
		fileStream.print("putstatic " + st.module + "/" + var.name );
		if(var.isInt())
			fileStream.println(" I");
		else
			fileStream.println(" [I");
		changeStack(-1);
	}

	private void ldVar(Declaration var){
		if(var.isArray() && var.intAccess()){
			fileStream.println("iastore");
			changeStack(-3);
		}
		else{
			if(var.global)
				ldGlobal(var);
			else
				ldLocal(var);
		}
	}

	private void ldLocal(Declaration var){
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

		changeStack(1);
	}

	private void ldGlobal(Declaration var){
		fileStream.print("getstatic " + st.module + "/" + var.name);
		if(var.isInt())
			fileStream.println(" I");
		else
			fileStream.println(" [I");
		changeStack(1);
	}

	private void ldConst(int val){
		if( val>5 || val<0){
			if((val>=-128 && val<=127)){
				fileStream.println("bipush " + val);
			}
			else if(val>=-32768 && val<=32767){
				fileStream.println("sipush " + val);
			}
			else{
				fileStream.println("ldc " + val);
			}
		} else
			fileStream.println("iconst_" + val);

		changeStack(1);
	}

	public void ldStr(String str){
		fileStream.println("ldc " + str);
		changeStack(1);
	}

	public String op2str(String op){
		String res="";
		if(op.compareTo("*")==0){
			res="imul";
		}else if(op.compareTo("/")==0){
			res="idiv";
		}else if(op.compareTo("<<")==0){
			res="ishl";
		}else if(op.compareTo(">>")==0){
			res="ishr";
		}else if(op.compareTo("&")==0){
			res="iand";
		}else if(op.compareTo("+")==0){
			res="iadd";
		}else if(op.compareTo("-")==0){
			res="isub";
		}else if(op.compareTo("|")==0){
			res="ior";
		}
		changeStack(-1);
		return res;
	}

	public String cmpop2str(String op){
		String res="";
		if(op.compareTo("==")==0)
			res="if_icmpne";
		else if(op.compareTo("=<")==0 || op.compareTo("<=")==0)
			res="if_icmpgt";
		else if(op.compareTo("=>")==0 || op.compareTo(">=")==0)
			res="if_icmplt";
		else if(op.compareTo(">")==0)
			res="if_icmple";
		else if(op.compareTo("!=")==0)
			res="if_icmpeq";
		else if(op.compareTo("<")==0)
			res="if_icmpge";
		changeStack(-2);
		return res;
	}

	private void changeStack(int i){
		stackval = stackval + i;
		if(stackval < 0)
			stackval = 0;
		if(stackval > stacklimit)
			stacklimit = stackval;
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
