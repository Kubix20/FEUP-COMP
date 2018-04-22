import java.util.HashMap;
import java.util.ArrayList;

public class SymbolTable{
	
	private String module;
	private HashMap<String,Function> functions;
	private Function currFunction;
	private HashMap<String,Declaration> globalDeclarations;
	private ArrayList<String> errors;
	
	public SymbolTable(){
		this.functions = new HashMap<String,Function>();
		this.globalDeclarations = new HashMap<String,Declaration>();
		this.errors = new ArrayList<String>();
	}
	
	public static boolean isInt(String i)
	{
		return i.matches("^\\d+");
	}
	
	private Declaration lookupVariable(String name){
		Declaration var = null;
		
		if (currFunction.ret.name.compareTo(name)==0) 
			var = currFunction.ret;
		
		if (currFunction.localDeclarations.containsKey(name))
			var = currFunction.localDeclarations.get(name);
		
		if (currFunction.parameters.containsKey(name))
			var = currFunction.parameters.get(name);
		
		if (globalDeclarations.containsKey(name))
			var = globalDeclarations.get(name);
		
		return var;
	}
	
	public void stAssign(SimpleNode node){
		Declaration lhs = stAccessAssign((SimpleNode) node.jjtGetChild(0));

		SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);
		int children = rhs.jjtGetNumChildren();
		SimpleNode child = (SimpleNode) rhs.jjtGetChild(0);
		
		if( child.getId() == YalTreeConstants.JJTTERM ){
			System.out.println("Term");
			String rhsType;
			String type1 = stTerm(child);
			System.out.println("Type1: "+type1);
			rhsType = type1;
			String type2 = "";
			if(children == 2){
				System.out.println("2nd Term");
				type2 = stTerm((SimpleNode) rhs.jjtGetChild(1));
				System.out.println("Type2: "+type2);
			}
			else
				rhsType = type1;			
			
			if(lhs.type.compareTo(rhsType) == 0){
				lhs.init = true;
			}
			else{
				if(lhs.type.compareTo("undefined") == 0)
					lhs.init(rhsType);
				else
					logError(1,"Imcopatible types "+lhs.name);
			}
		}
		else
		
		if(	child.getId() == YalTreeConstants.JJTARRAYSIZE ){
			System.out.println("ArraySize");
			
			if(lhs.isArray()){
				lhs.init = true;
			}
			else{
				if(lhs.type.compareTo("undefined") == 0)
					lhs.initArray();
				else
					logError(1,"Imcopatible types "+lhs.name);
			}
			
			/*
			if(lhs.init){
				if(!lhs.isArray())
					logError(1,"Imcopatible types");
			}
			else
				lhs.initArray();
			*/
		}
	}
	
	public Declaration stCall(SimpleNode node){
		String name = node.getValue();
		System.out.println("Call name: "+name);
		String moduleName = "";
		String functionName = "";
		int dotIndex;
		if((dotIndex = name.indexOf("."))!=-1){
			moduleName = name.substring(0,dotIndex-1);
			functionName = name.substring(dotIndex+1,name.length()-1);
		}
		else
			functionName = name;
		
		Function function = functions.get(functionName);
		Declaration ret = null;
		
		if(moduleName == ""){
			
			if(function != null){
				
				//check arguments
				int argNr = 0;
				int expectedArgNr =  function.parameters.size();
				
				if(node.jjtGetNumChildren() > 0){
					//System.out.println("Params");
					//System.out.println(((SimpleNode) node.jjtGetChild(0)).getId());
					argNr = ((SimpleNode) node.jjtGetChild(0)).jjtGetNumChildren();
					//System.out.println(argNr);	
				}
				
				if(argNr != expectedArgNr)
					logError(1,"Wrong number of arguments for function "+function.name+", expected "+expectedArgNr+" and got "+argNr);
				
				if(node.jjtGetNumChildren() > 0){
					
					int currParam = 0;
					SimpleNode argList = (SimpleNode) node.jjtGetChild(0);
					
					SimpleNode param;
					for (String paramKey: function.parameters.keySet()){
							
						param = (SimpleNode) argList.jjtGetChild(currParam);
						String paramName = param.getValue();
						String paramType = "integer";
							
						if(!isInt(paramName)){
							Declaration var = lookupVariable(paramName);
							if(var != null){
									
								if(!var.init){
									logError(1,"Argument nr "+currParam+", "+paramName+" might not have been initialized");
									//break;
								}
									
								paramType = var.type;
							}
							else{
								logError(1,"Argument nr "+currParam+", "+paramName+" not declared");
								//break;
							}
									
						}
							
						String expectedType = function.parameters.get(paramKey).type;
						if(expectedType.compareTo(paramType) != 0){
							logError(1,"Non matching argument types for argument "+currParam+", expected "+expectedType+" and got "+paramType);
							//break;
						}
							
							currParam++;
					}
				}
				
				ret = function.ret;
				
			}
			else
				logError(1,"Call to undefined function "+functionName);
				
			
		}
		
		return ret;
	}
	
	public String stTerm(SimpleNode node){
		//System.out.println("Term value: "+node.getValue());
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
		/*
		System.out.println("Term op: "+op);
		System.out.println("Term value: "+value);
		*/
		
		if(node.jjtGetNumChildren() == 1) {
			SimpleNode child = (SimpleNode) node.jjtGetChild(0);
			Declaration var;
			
			if( child.getId() == YalTreeConstants.JJTACCESS ){
				var = stAccess(child);
				
				if(var != null){
					System.out.println(var.toString());
				
					if(op != "" && var.isArray()){
						logError(1,"Illegal use of operator on array");
						return "void";
					}
					
					return var.type;
				}
			}
			
			if( child.getId() == YalTreeConstants.JJTCALL ){
				var = stCall(child);
				
				if(var != null){
					if(op != "" && var.isArray()){
						logError(1,"Illegal use of operator on array type");
						return "void";
					}
				
					return var.type;
				}
			}
		}
		else
			return "integer";
		
		return "void";
	}
	
	public Declaration stAccessAssign(SimpleNode node){
		String name = node.getValue();
		
		if(name.indexOf(".")!=-1) {
			logError(1,"Invalid access to size of variable "+name);
			return null;
		}
		
		//System.out.println("Assign Access name: "+name);
		
		Declaration var = lookupVariable(name);
		
		if(var == null){
			var = new Declaration(name);
			currFunction.localDeclarations.put(name, var);
		}
		
		if(node.jjtGetNumChildren() == 1) {
			
			String indexName = ((SimpleNode) node.jjtGetChild(0)).getValue();
			//System.out.println("Index: "+indexName);
			
			if(var.isArray()){
				if(!var.init){
					if(!isInt(indexName)){
				
						Declaration index = lookupVariable(indexName);
						if(var != null){
							if(!var.init)
								logError(1,"Index "+indexName+" access of variable "+name+"not initialized");
						}
						else
							logError(1,"Index "+indexName+" access of variable "+indexName+" not declared");
					}
				}
				else
					logError(1,"Invalid index access of variable "+name+" not initialized");
			}
			else
				logError(1,"Invalid index access of variable "+name+" not of array type");
		}
		
		return var;
	}
	
	public Declaration stAccess(SimpleNode node){
		String name = node.getValue();
		
		if(name.indexOf(".")!=-1) {
			name = name.substring(0, name.indexOf("."));
		}
		
		//System.out.println("Access name: "+name);
		
		Declaration var = lookupVariable(name);
		if(var == null){
			logError(1,"Variable "+name+" not declared");
			return null;
		}
		
		if(node.jjtGetNumChildren() == 1) {
			
			String indexName = ((SimpleNode) node.jjtGetChild(0)).getValue();
			//System.out.println("Index: "+indexName);
			
			if(var.isArray()){
				if(!var.init){
					if(!isInt(indexName)){
				
						Declaration index = lookupVariable(indexName);
						if(var != null){
							if(!var.init)
								logError(1,"Index "+indexName+" access of variable "+name+"not initialized");
						}
						else
							logError(1,"Index "+indexName+" access of variable "+indexName+" not declared");
					}
				}
				else
					logError(1,"Invalid index access of variable "+name+" not initialized");
			}
			else
				logError(1,"Invalid index access of variable "+name+" not of array type");
		}
		
		return var;
	}
	
	public void stExprtest(SimpleNode node){
		int children = node.jjtGetNumChildren();
	
		stAccess((SimpleNode) node.jjtGetChild(0));
	
		SimpleNode child = (SimpleNode) node.jjtGetChild(1);
		
		if( child.getId() == YalTreeConstants.JJTTERM ){
			System.out.println("Term");
			String type1 = stTerm(child);
			/*
			String type2;
			*/
			if(children == 3){
				//type2 = stTerm((SimpleNode) node.jjtGetChild(2));
				System.out.println("2nd Term");
			}
		}
		else
		
		if(	child.getId() == YalTreeConstants.JJTARRAYSIZE ){
			logError(1,"Unable to compare to array size declaration");
		}
	}
	
	public void stStmtlst(SimpleNode node){
		SimpleNode child;
		for(int i = 0; i < node.jjtGetNumChildren();i++){
			child = (SimpleNode) node.jjtGetChild(i);
			
			if(child.getId() == YalTreeConstants.JJTWHILE ){
				System.out.println("While");
				stExprtest((SimpleNode) child.jjtGetChild(0));
				stStmtlst(child);
			}
			
			if(child.getId() == YalTreeConstants.JJTIF ){
				System.out.println("If");
				stExprtest((SimpleNode) child.jjtGetChild(0));
				stStmtlst(child);
			}
			
			if(child.getId() == YalTreeConstants.JJTASSIGN ){
				System.out.println("Assign");
				stAssign(child);
			}
			
			if(child.getId() == YalTreeConstants.JJTCALL ){
				System.out.println("Call");
				stCall(child);
			}
		}
	}
	
	public void stFunctionBody(SimpleNode node){
		String functionName = node.getValue();
		int currChild=0; //proximo filho
	
		//testa se o nome da funcao tem ponto
		if (functionName.indexOf(".")!=-1) {
			functionName = functionName.substring(functionName.indexOf(".")+1);
		}
		
		currFunction = functions.get(functionName);
		
		stStmtlst(node);
		
		/*
		if (functions.get(nomefuncao).ret.type.compareTo("undefined")==0) {
			System.out.println("Aviso na linha " + node.line + 
			": Retorno nao e definido no corpo da funcao");
		}
		*/
	}
	
	public void stFunction(SimpleNode node){
		String functionName = node.getValue();
		System.out.println("Function Name: "+functionName);
		String returnName="";
		String returnType="void";
		String comp=null;
		int currChild=0;
		
		if (functionName.indexOf(".")!=-1) {
			functionName = functionName.substring(functionName.indexOf(".")+1);
			logWarning(1,"Function name contains '.' ." +"Name changed to "+functionName);
		}
		
		if (functions.containsKey(functionName)) {
			logError(1,"Function "+functionName+"already declared");
			return;
		}
		
		if ( ((SimpleNode) node.jjtGetChild(currChild)).getId() == YalTreeConstants.JJTELEMENT ) {
			returnType="integer";
			
			//para efeitos de comparacao retira-se apenas o nome da variavel
			comp = ((SimpleNode) node.jjtGetChild(currChild)).getValue();
			if(comp.indexOf("[]")!=-1)
			{
				comp = comp.substring(0, comp.indexOf("["));
				returnType="array";
			}
			returnName=comp;
			currChild++;
			System.out.println("Retorno: " + returnName + " "+ returnType);
		}
		
		Function function = new Function(functionName, returnName, returnType);
		
		//verifica se tem parametros
		if ( ((SimpleNode) node.jjtGetChild(currChild)).getId() == YalTreeConstants.JJTVARLIST ) {
			for (int i=0; i<node.jjtGetChild(currChild).jjtGetNumChildren();i++) {
				String paramName = ((SimpleNode) node.jjtGetChild(currChild).jjtGetChild(i)).getValue();
				String paramType="integer";

				if(paramName.indexOf("[]")!=-1)
				{
					paramName = paramName.substring(0, paramName.indexOf("["));
					paramType="array";
				}
				
				//testa se nao e repetido
				if ( (!function.parameters.containsKey(paramName)) ){
					
					if (comp!=null) { 
						if (paramName.compareTo(comp)==0) {
							logError(1,"Argument name may cannot be equal to the return");
							return;
						}
					}
					function.addParameter(paramName,paramType);
				} 
				else {
					logError(1,"Repeated Argument");
					return;
				}				
			}
			currChild++;
		}
		
		//add function
		functions.put(functionName, function);
	}
	
	public void stDeclaration(SimpleNode node){
		int nchildren = node.jjtGetNumChildren();
		
		SimpleNode lhs = (SimpleNode) node.jjtGetChild(0);
		String lhsName = lhs.getValue();
		String lhsType = "integer";
		if(lhsName.indexOf("[]")!=-1) {
			lhsType = "array";
			lhsName = lhsName.substring(0, lhsName.indexOf("["));
		}
		
		if(!globalDeclarations.containsKey(lhsName)){
			globalDeclarations.put(lhsName, new Declaration(lhsName,lhsType));
		}
	}
	
	private void declareGlobals(SimpleNode root){
		SimpleNode node;
		for(int i=0; i< root.jjtGetNumChildren(); i++)
		{
			node = (SimpleNode) root.jjtGetChild(i);
					
			// DECLARATION
			if(node.getId() == YalTreeConstants.JJTDECLARATION)
			{
				stDeclaration(node);
			} else
		
			// FUNCTION
			if(node.getId() == YalTreeConstants.JJTFUNCTION)
			{
				System.out.println("Function");
				stFunction(node);
			}
		}
	}
	

	private void analyseFunctions(SimpleNode root){
		SimpleNode node;
		for(int i=0; i< root.jjtGetNumChildren(); i++)
		{
			node = ((SimpleNode) root.jjtGetChild(i));

			if(node.getId() == YalTreeConstants.JJTFUNCTION)
			{
				System.out.println("Function body");
				stFunctionBody(node);
			}
		}
		
	}
	
	public void analyse(SimpleNode root){
		
		if(root.getId() == YalTreeConstants.JJTMODULE){
			module = root.getValue();
		}
		
		declareGlobals(root);
		analyseFunctions(root);
	}
	
	public void print(){
		String newLine = System.lineSeparator();
		String doubleNewLine = newLine+newLine;
	
		String declarations="";
		for (String name: this.globalDeclarations.keySet()){
            String value = this.globalDeclarations.get(name).toString();
            declarations += "      "+value+newLine;
		}
	
		String functionsStr="";
		for (String name: this.functions.keySet()){
            String value = this.functions.get(name).toString();
            functionsStr += value+doubleNewLine;
		}
	
		String content= "Module "+this.module+newLine+
						"Global declarations:"+newLine+declarations+
						"Functions:"+newLine+functionsStr;	
					 
		System.out.println(content);
	}
	
	public boolean printErrors(){
		if(errors.size() == 0)
			return false;
		
		String newLine = System.lineSeparator();
		for(String error : errors)
			System.out.println(error+newLine);
		
		return true;
	}
	
	private void logWarning(int line, String msg){
		System.out.println("Warning on line " +line+": "+msg);
		errors.add("Warning on line " +line+": "+msg);
	}
	
	private void logError(int line, String msg){
		System.out.println("Error on line " +line+": "+msg);
		errors.add("Error on line " +line+": "+msg);
	}
}