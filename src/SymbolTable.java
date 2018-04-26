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
	
	public static boolean isInt(String i){
		return i.matches("^\\d+");
	}
	
	private Declaration lookupVariable(String name){
		Declaration var = null;
		
		if(currFunction != null){
			if (currFunction.localDeclarations.containsKey(name)){
				var = currFunction.localDeclarations.get(name);
				return var;
			}
			
			if (currFunction.parameters.containsKey(name)){
				var = currFunction.parameters.get(name);
				return var;
			}
			
			if (currFunction.ret.name.compareTo(name)==0) {
				var = currFunction.ret;
				return var;
			}
		
		}
		
		if (globalDeclarations.containsKey(name))
			var = globalDeclarations.get(name);
		
		return var;
	}
	
	private Declaration lookupVariableAssign(String name){
		Declaration var = null;
		
		if(currFunction != null){
			if (currFunction.localDeclarations.containsKey(name)){
				var = currFunction.localDeclarations.get(name);
				return var;
			}
			
			if (currFunction.ret.name.compareTo(name)==0){
				var = currFunction.ret;
				return var;
			}
		
			if (currFunction.parameters.containsKey(name)){
				var = currFunction.parameters.get(name);
				return var;
			}
		}
		
		if (globalDeclarations.containsKey(name))
			var = globalDeclarations.get(name);
		
		return var;
	}
	
	public void stAssign(SimpleNode node){
		int line = node.getLine();
		Declaration lhs = stAccessAssign((SimpleNode) node.jjtGetChild(0));
		
		if(lhs == null)
			return;
		
		String lhsAccess = lhs.access;

		SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);
		int children = rhs.jjtGetNumChildren();
		SimpleNode child = (SimpleNode) rhs.jjtGetChild(0);
		
		if( child.getId() == YalTreeConstants.JJTTERM ){
			System.out.println("Term");
			String rhsType;
			String type1 = stTerm(child);
			//System.out.println("Type1: "+type1);
			rhsType = type1;
			String type2 = "";
			if(children == 2){
				System.out.println("2nd Term");
				type2 = stTerm((SimpleNode) rhs.jjtGetChild(1));
				//System.out.println("Type2: "+type2);
				
				if(type2.compareTo("undefined") != 0){
					if(type1.compareTo("integer") != 0 || type2.compareTo("integer") != 0){
						logError(line,"Incompatible types for arithmetic expression: "+type1+" and "+type2);
						return;
					}
				}
				else
					return;
			}
			
			
			//if type is undefined, initialize according to the rhs type
			if(lhs.isUndefined()){
				lhs.init(rhsType);
			}
			else{
				
				if(rhsType.compareTo("undefined") != 0){
					if(lhsAccess.compareTo("integer") == 0){
						if(lhsAccess.compareTo(rhsType) != 0){
							logError(line,"Incompatible types: "+lhsAccess+" and "+rhsType);
							return;
						}
					}
					
					lhs.init = true;
				}
			}
		}
		else
		
		if(	child.getId() == YalTreeConstants.JJTARRAYSIZE ){
			System.out.println("ArraySize");
			
			if(!stArraySize(child))
				return;
			
			
			//if type is undefined, initialize as array
			if(lhs.isUndefined()){
				lhs.initArray();
			}
			else{
				
				//check if the access is of array type
				if(lhsAccess.compareTo("array") == 0)
					lhs.init = true;
				else
					logError(line,"Incompatible types: "+lhsAccess+" and array");
			}
		}
	}
	
	public Declaration stCall(SimpleNode node){
		int line = node.getLine();
		String name = node.getValue();
		//System.out.println("Call name: "+name);
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
					logError(line,"Wrong number of arguments for function "+function.name+", expected "+expectedArgNr+" and got "+argNr);
				
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
									
								if(!var.isInitialized()){
									logError(line,"Argument nr "+currParam+", "+paramName+" might not have been initialized");
									//break;
								}
									
								paramType = var.type;
							}
							else{
								logError(line,"Argument nr "+currParam+", "+paramName+" not found");
								//break;
							}
									
						}
							
						String expectedType = function.parameters.get(paramKey).type;
						if(expectedType.compareTo(paramType) != 0){
							logError(line,"Non matching argument types for argument "+currParam+", expected "+expectedType+" and got "+paramType);
							//break;
						}
							
							currParam++;
					}
				}
				
				ret = function.ret;
				
			}
			else
				logError(line,"Call to undefined function "+functionName);
				
			
		}
		
		return ret;
	}
	
	public String stTerm(SimpleNode node){
		int line = node.getLine();
		
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
					
					if(!var.isInitialized()){
						logError(line,"Variable "+var.name+" might not have been initialized");
						return "undefined";
					}
					
					if(op != "" && var.isArray()){
						logError(line,"Illegal use of operator on array type");
						return "undefined";
					}
				
					return var.access;
				}
			}
			
			if( child.getId() == YalTreeConstants.JJTCALL ){
				var = stCall(child);
				
				if(var != null){
					if(op != "" && var.isArray()){
						logError(line,"Illegal use of operator on array type");
						return "undefined";
					}
				
					return var.type;
				}
				else
					return "integer";
			}
		}
		else
			return "integer";
		
		return "undefined";
	}	
	
	public boolean stArraySize(SimpleNode node){
		if(node.jjtGetNumChildren() > 0){
			Declaration access = stAccess((SimpleNode) node.jjtGetChild(0));
					
			if(access == null)
				return false;
			else if(access.undefinedAccess())
				return false;
		}
		
		return true;
	}
	
	public Declaration stAccessAssign(SimpleNode node){
		int line = node.getLine();
		String name = node.getValue();
		
		if(name.indexOf(".")!=-1) {
			logError(line,"Invalid access to size of variable "+name);
			return null;
		}
		
		//System.out.println("Assign Access name: "+name);
		
		Declaration var = lookupVariableAssign(name);
		
		if(var == null){
			var = new Declaration(name);
			currFunction.localDeclarations.put(name, var);
		}
		
		var.access = "undefined";
		
		if(node.jjtGetNumChildren() == 1) {
			
			String indexName = ((SimpleNode) node.jjtGetChild(0)).getValue();
			//System.out.println("Index: "+indexName);
			
			if(var.isArray()){
				if(var.isInitialized()){
					if(!isInt(indexName)){
				
						Declaration index = lookupVariable(indexName);
						if(index != null){
							if(index.isInitialized())
								var.access = "integer";
							else
								logError(line,"Index "+indexName+" access of variable "+name+" might not have been initialized");
						}
						else
							logError(line,"Index "+indexName+" access of variable "+name+" not found");				}
					else
						var.access = "integer";
				}
				else
					logError(line,"Invalid index access of variable "+name+" not initialized");
			}
			else
				logError(line,"Invalid index access of variable "+name+" not of array type");
		}
		else
			var.access = var.type;
		
		return var;
	}
	
	public Declaration stAccess(SimpleNode node){
		int line = node.getLine();
		String name = node.getValue();
		
		boolean sizeAccess = false;
		if(name.indexOf(".")!=-1) {
			name = name.substring(0, name.indexOf("."));
			sizeAccess = true;
		}
		
		//System.out.println("Access name: "+name);
		
		Declaration var = lookupVariable(name);
		if(var == null){
			logError(line,"Variable "+name+" not found");
			return null;
		}
		
		var.access = "undefined";
		
		if(node.jjtGetNumChildren() == 1) {
			
			String indexName = ((SimpleNode) node.jjtGetChild(0)).getValue();
			//System.out.println("Index: "+indexName);
			
			if(var.isArray()){
				if(var.isInitialized()){
					if(!isInt(indexName)){
				
						Declaration index = lookupVariable(indexName);
						if(index != null){
							if(index.isInitialized())
								var.access = "integer";
							else
								logError(line,"Index "+indexName+" access of variable "+name+" might not have been initialized");
						}
						else
							logError(line,"Index "+indexName+" access of variable "+indexName+" not found");
					}
					else
						var.access = "integer";
				}
				else
					logError(line,"Invalid index access of variable "+name+" not initialized");
			}
			else
				logError(line,"Invalid index access of variable "+name+" not of array type");
		}
		else {
			
			if(sizeAccess){
				
				if(var.isArray()){
					if(var.isInitialized()){
						var.access = "integer";
					}
					else
						logError(line,"invalid size access of variable "+var.name+" not initialized");
				}
				else
					logError(line,"Invalid size access of variable "+var.name);
			}
			else
				var.access = var.type;
		}
		
		return var;
	}
	
	public void stExprtest(SimpleNode node){
		int line = node.getLine();
		Declaration lhs = stAccess((SimpleNode) node.jjtGetChild(0));
		
		if(lhs == null)
			return;
		
		String lhsType = lhs.access;
		
		SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);
		int children = rhs.jjtGetNumChildren();
		SimpleNode child = (SimpleNode) rhs.jjtGetChild(0);
		
		if( child.getId() == YalTreeConstants.JJTTERM ){
			System.out.println("Term");
			String rhsType;
			String type1 = stTerm(child);
			//System.out.println("Type1: "+type1);
			rhsType = type1;
			String type2 = "";
			if(children == 2){
				System.out.println("2nd Term");
				type2 = stTerm((SimpleNode) rhs.jjtGetChild(1));
				//System.out.println("Type2: "+type2);
				
				if(type1.compareTo("integer") != 0 || type2.compareTo("integer") != 0){
					logError(line,"Incompatible types for arithmetic expression: "+type1+" and "+type2);
					return;
				}
			}
			
			if(lhsType.compareTo("integer") != 0 || rhsType.compareTo("integer") != 0)
				logError(line,"Incomparable types "+lhsType+" and "+rhsType);
		}
		else
		
		if(	child.getId() == YalTreeConstants.JJTARRAYSIZE ){
			logError(line,"Unable to compare to array size declaration");
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
		
		if(!functions.containsKey(functionName))
			return;
		
		currFunction = functions.get(functionName);
		
		stStmtlst(node);
		
		if(currFunction.ret.type.compareTo("void") != 0) {
			if(!currFunction.ret.isInitialized())
				logError("Return "+currFunction.ret.name+" might not have been initialized on function "+currFunction.name);
		}
	}
	
	public void stFunction(SimpleNode node){
		int line = node.getLine();
		String functionName = node.getValue();
		//System.out.println("Function Name: "+functionName);
		String returnName="";
		String returnType="void";
		String comp=null;
		int currChild=0;
		
		if (functionName.indexOf(".")!=-1) {
			functionName = functionName.substring(functionName.indexOf(".")+1);
			logWarning(line,"Function name contains '.' ." +"Name changed to "+functionName);
		}
		
		if (functions.containsKey(functionName)) {
			logError(line,"Function "+functionName+"already declared");
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
			//System.out.println("Retorno: " + returnName + " "+ returnType);
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
					function.addParameter(paramName,paramType);
				} 
				else
					logError(line,"Repeated argument "+paramName+" on function "+functionName);
			}
			currChild++;
		}
		
		//add function
		functions.put(functionName, function);
	}
	
	public void stDeclaration(SimpleNode node){
		int line = node.getLine();
		
		int children = node.jjtGetNumChildren();
		
		boolean newVar = false;
		String lhsName = ((SimpleNode) node.jjtGetChild(0)).getValue();
		String lhsType = "integer";
		if(lhsName.indexOf("[]")!=-1) {
			lhsType = "array";
			lhsName = lhsName.substring(0, lhsName.indexOf("["));
		}
		
		Declaration lhs = globalDeclarations.get(lhsName);
		
		if(lhs == null){
			lhs = new Declaration(lhsName,lhsType,true);
			globalDeclarations.put(lhsName, lhs);
			newVar = true;
		}
		
		if(lhsType.compareTo("array") == 0 && lhs.isInt()) {
			logError(line,"Invalid access of variable "+lhs.name+" of type integer");
		}
		
		if(children > 1){
			
			String rhsType = "undefined"; 
			SimpleNode child = (SimpleNode) node.jjtGetChild(1);
			if(child.getId() == YalTreeConstants.JJTARRAYSIZE){
				System.out.println("ArraySize");
				
				if(!stArraySize(child))
					return;
				
				rhsType = "array";
			}
			
			if(child.getId() == YalTreeConstants.JJTINTELEMENT){
				System.out.println("IntElement");
				rhsType = "integer";
			}
			
			if(newVar){
				//System.out.println("Type: "+rhsType);
				if(lhs.isArray()){
					if(rhsType.compareTo("integer") == 0)
						logError(line,"Illegal array "+lhs.name+" declaration");
				}
				else
					lhs.init(rhsType);
			}
			else {
				if(lhs.isInt()){
					if(rhsType.compareTo("array") == 0)
						logError(line,"Incompatible types: "+lhs.type+" and "+rhsType);
				}
			}
		}
		else{
			if(!newVar)
				logError(line,"Variable "+lhs.name+" already declared");
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
	
	public int printErrors(){
		String newLine = System.lineSeparator();
		for(String error : errors)
			System.out.println(error+newLine);
		
		return errors.size();
	}
	
	private void logWarning(int line, String msg){
		System.out.println("Warning on line " +line+": "+msg);
		errors.add("Warning on line " +line+": "+msg);
	}
	
	private void logError(String msg){
		System.out.println("Error: "+msg);
		errors.add("Error: "+msg);
	}
	
	private void logError(int line, String msg){
		System.out.println("Error on line " +line+": "+msg);
		errors.add("Error on line " +line+": "+msg);
	}
}