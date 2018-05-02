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

	public CodeGenerator(File inputFile, ASTModule root, SymbolTable st) {
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

	private void generateGlobals(ASTModule node) throws IOException {
		int children = node.jjtGetNumChildren();

		for(int i=0; i< children; i++)
		{
			if(node.jjtGetChild(i) instanceof ASTDeclaration)
			{
				generateDeclaration((ASTDeclaration)node.jjtGetChild(i));
				hasGlobalAttrs=true;
			}
		}

	}

	private void generateFunctions(ASTModule node) throws IOException{
		int children = node.jjtGetNumChildren();

		for(int i=0; i< children; i++)
		{
			if(node.jjtGetChild(i) instanceof ASTFunction)
			{
				generateFunction((ASTFunction)node.jjtGetChild(i));
				fileStream.println();
			}
		}
	}

	private void generateDeclaration(ASTDeclaration node) throws IOException {

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

	private void generateFunction(ASTFunction node) throws IOException {
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

		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			if(node.jjtGetChild(i) instanceof ASTStmtlst)
			{
				generateStmtlst((ASTStmtlst)node.jjtGetChild(i));
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
		fileOut.getChannel().position(stackpos);
		fileStream.print(stacklimit);
		fileOut.getChannel().position(localspos);
		fileStream.print(localsval);

		fileOut.getChannel().position(tmp);
	}

	private void generateStmtlst(ASTStmtlst node) throws IOException {
		for(int i=0; i< node.jjtGetNumChildren(); i++)
		{
			if(node.jjtGetChild(i) instanceof ASTAssign)
			{
				System.out.println("Generating assign...");
				generateAssign((ASTAssign)node.jjtGetChild(i));
				fileStream.println();
			}

			if(node.jjtGetChild(i) instanceof ASTCall)
			{
				System.out.println("Generating call...");
				generateCall((ASTCall)node.jjtGetChild(i));
				fileStream.println();
			}

			if(node.jjtGetChild(i) instanceof ASTIf)
			{
				System.out.println("Generating if...");
				generateIf((ASTIf)node.jjtGetChild(i));
				fileStream.println();
			}

			if(node.jjtGetChild(i) instanceof ASTWhile)
			{
				System.out.println("Generating while...");
				generateWhile((ASTWhile)node.jjtGetChild(i));
				fileStream.println();
			}
		}
	}

	private void generateCall(ASTCall node) throws IOException{
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
			//TODO: REVER ISTO NA SEMANTICA
			func = name;
			mod = st.module;
		}

		if(node.jjtGetNumChildren() != 0){
			params = generateArgumentList((ASTVarlist) node.jjtGetChild(0));
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

	public String generateArgumentList(ASTVarlist node){
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
				loadVar(var);
			}

			fileStream.println("iaload");
			updateStack(-1);
		}
		else if(sizeAccess){
			fileStream.println("arraylength");
		}
	}

	private Declaration generateAccessAssign(SimpleNode node){
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
					loadConst(Integer.parseInt(indexName));
				}
				else{
					Declaration index = lookupVar(indexName);
					loadVar(var);
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
		Declaration lhs = generateAccessAssign((SimpleNode) node.jjtGetChild(0));

		SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);
		int children = rhs.jjtGetNumChildren();
		SimpleNode term1 = (SimpleNode) rhs.jjtGetChild(0);

		if(term1.getId() == YalTreeConstants.JJTTERM){
			generateTerm(term1);
			if(children == 2){
				SimpleNode term2 = (SimpleNode) rhs.jjtGetChild(1);
				generateTerm(term2);
				fileStream.println(OpToString(rhs.getValue()));
			}
		}
		else

		if(term1.getId() == YalTreeConstants.JJTARRAYSIZE){
			generateArraySize(term1);
		}

		storeVar(lhs);
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
			loadConst(Integer.parseInt(value));
		}

		if(op.compareTo("-")==0)
			fileStream.println("ineg");
	}

	private void generateExprtest(SimpleNode node) throws IOException {
		generateAccess((SimpleNode) node.jjtGetChild(0));

		SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);
		int children = rhs.jjtGetNumChildren();
		SimpleNode term1 = (SimpleNode) rhs.jjtGetChild(0);

		if(term1.getId() == YalTreeConstants.JJTTERM){
			generateTerm(term1);
			if(children == 2){
				SimpleNode term2 = (SimpleNode) rhs.jjtGetChild(1);
				generateTerm(term2);
				fileStream.println(OpToString(rhs.getValue()));
			}
		}

		fileStream.println(cmpOpToString(node.getValue()));
	}

	private void generateIf(SimpleNode node) throws IOException {
		generateExprtest((SimpleNode) node.jjtGetChild(0));
		fileStream.println();
		generateStmtlst((SimpleNode) node.jjtGetChild(1));
	}

	private void generateWhile(SimpleNode node) throws IOException {
		generateExprtest((SimpleNode) node.jjtGetChild(0));
		fileStream.println();
		generateStmtlst((SimpleNode) node.jjtGetChild(1));
	}

	private void storeVar(Declaration var){
		if(var.global)
			storeGlobal(var);
		else
			storeLocal(var);
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
		if(var.isArray() && var.intAccess()){
			fileStream.println("iastore");
			updateStack(-3);
		}
		else{
			if(var.global)
				loadGlobal(var);
			else
				loadLocal(var);
		}
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

	public String OpToString(String op){
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

	// os saltos a realizar serao com base no inverso por isso devolve-se a operacao complementar
	// ex: se condicao if(a > b) o salto sera feito quando a <= b
	public String cmpOpToString(String op){
		String res = "";
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
				res = "if_cmple";
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

	private void updateStack(int factor){
		stackval = (stackval + factor < 0 ? 0 : stackval + factor);
		if(stackval > stacklimit) stacklimit = stackval;
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
