import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SymbolTable{

	public String module;
	public HashMap<String,Function> functions;
	public Function currFunction;
	public LinkedHashMap<String,Declaration> globalDeclarations;
	public ArrayList<String> errors;
	public ArrayList<String> warnings;

	public SymbolTable(){
		this.functions = new HashMap<String,Function>();
		this.globalDeclarations = new LinkedHashMap<String,Declaration>();
		this.errors = new ArrayList<String>();
		this.warnings = new ArrayList<String>();
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

	public void analyseAssign(SimpleNode node){
		int line = node.getLine();
		Declaration lhs = analyseAccessAssign((SimpleNode) node.jjtGetChild(0));

		if(lhs == null)
			return;

		String lhsAccess = lhs.access;

		SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);
		int children = rhs.jjtGetNumChildren();
		SimpleNode child = (SimpleNode) rhs.jjtGetChild(0);

		if( child.getId() == YalTreeConstants.JJTTERM ){
			System.out.println("Term");
			String rhsType;
			String type1 = analyseTerm(child);
			rhsType = type1;
			String type2 = "";
			if(children == 2){
				System.out.println("2nd Term");
				type2 = analyseTerm((SimpleNode) rhs.jjtGetChild(1));

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

			if(!analyseArraySize(child))
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

	public Declaration analyseCall(SimpleNode node){
		int line = node.getLine();
		String name = node.getValue();
		String moduleName = "";
		String functionName = "";
		int dotIndex;
		if((dotIndex = name.indexOf("."))!=-1){
			moduleName = name.substring(0,dotIndex);
			functionName = name.substring(dotIndex+1);
		}
		else
			functionName = name;

		Function function = functions.get(functionName);
		Declaration ret = null;

		if(moduleName == "" || moduleName.compareTo(module)==0){

			if(function != null){

				//check arguments
				int argNr = 0;
				int expectedArgNr =  function.parameters.size();

				if(node.jjtGetNumChildren() > 0)
					argNr = ((SimpleNode) node.jjtGetChild(0)).jjtGetNumChildren();

				if(argNr != expectedArgNr){
					logError(line,"Wrong number of arguments for function "+function.name+", expected "+expectedArgNr+" and got "+argNr);
				}
				else{

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
									}

									paramType = var.type;
								}
								else{
									logError(line,"Argument nr "+currParam+", "+paramName+" not found");
								}

							}

							String expectedType = function.parameters.get(paramKey).type;
							if(expectedType.compareTo(paramType) != 0){
								logError(line,"Non matching argument types for argument "+currParam+", expected "+expectedType+" and got "+paramType);
							}

								currParam++;
						}
					}

				}

				ret = function.ret;

			}
			else
				logError(line,"Call to undefined function "+functionName);


		}

		return ret;
	}

	public String analyseTerm(SimpleNode node){
		int line = node.getLine();

		String parts = node.getValue();

		System.out.println("Parts: "+parts);
		String op = "";
		String value = "";
		if(parts.charAt(0) == ' '){
			value = parts.trim();
		}
		else{
			op = ""+parts.charAt(0);
			value = parts.substring(1,parts.length()).trim();
		}

		System.out.println("Term op: "+op);
		System.out.println("Term value: "+value);

		if(node.jjtGetNumChildren() == 1) {
			SimpleNode child = (SimpleNode) node.jjtGetChild(0);
			Declaration var;

			if( child.getId() == YalTreeConstants.JJTACCESS ){
				var = analyseAccess(child);

				if(var != null){

					if(!var.isInitialized()){
						logError(line,"Variable "+var.name+" might not have been initialized");
						return "undefined";
					}

					System.out.println("Term var name: "+var.name);

					if(var.isArray() && op.compareTo("")!=0 ){
						logError(line,"Illegal use of operator on array type");
						return "undefined";
					}

					return var.access;
				}
			}

			if( child.getId() == YalTreeConstants.JJTCALL ){
				var = analyseCall(child);

				if(var != null){
					if(var.isArray() && op.compareTo("")!=0){
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

	public boolean analyseArraySize(SimpleNode node){
		if(node.jjtGetNumChildren() > 0){
			Declaration access = analyseAccess((SimpleNode) node.jjtGetChild(0));

			if(access == null)
				return false;
			else if(access.undefinedAccess())
				return false;
		}

		return true;
	}

	public Declaration analyseAccessAssign(SimpleNode node){
		int line = node.getLine();
		String name = node.getValue();

		if(name.indexOf(".")!=-1) {
			logError(line,"Invalid access to size of variable "+name);
			return null;
		}


		Declaration var = lookupVariableAssign(name);

		if(var == null){
			var = new Declaration(name);
			currFunction.localDeclarations.put(name, var);
			currFunction.addIfScopeDeclaration(var);
		}

		var.access = "undefined";

		if(var.partialIfStatus()){
			logError(line,"Variable "+var.name+" may reach this position uninitialized");
			return var;
		}

		if(var.incompatibleIfStatus()){
			logError(line,"Variable "+var.name+" may reach this position as an integer or as an array");
			return var;
		}

		if(node.jjtGetNumChildren() == 1) {

			String indexName = ((SimpleNode) node.jjtGetChild(0)).getValue();

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

	public Declaration analyseAccess(SimpleNode node){
		int line = node.getLine();
		String name = node.getValue();

		boolean sizeAccess = false;
		if(name.indexOf(".")!=-1) {
			name = name.substring(0, name.indexOf("."));
			sizeAccess = true;
		}

		Declaration var = lookupVariable(name);
		if(var == null){
			logError(line,"Variable "+name+" not found");
			return null;
		}

		var.access = "undefined";

		if(var.partialIfStatus()){
			logError(line,"Variable "+var.name+" may reach this position uninitialized");
			return var;
		}

		if(var.incompatibleIfStatus()){
			logError(line,"Variable "+var.name+" may reach this position as an integer or as an array");
			return var;
		}

		if(node.jjtGetNumChildren() == 1) {

			String indexName = ((SimpleNode) node.jjtGetChild(0)).getValue();

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
						logError(line,"Invalid size access of variable "+var.name+" not initialized");
				}
				else
					logError(line,"Invalid size access of variable "+var.name);
			}
			else
				var.access = var.type;
		}

		return var;
	}

	public void analyseExprtest(SimpleNode node){
		int line = node.getLine();
		Declaration lhs = analyseAccess((SimpleNode) node.jjtGetChild(0));

		if(lhs == null)
			return;

		String lhsType = lhs.access;

		SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);
		int children = rhs.jjtGetNumChildren();
		SimpleNode child = (SimpleNode) rhs.jjtGetChild(0);

		if( child.getId() == YalTreeConstants.JJTTERM ){
			System.out.println("Term");
			String rhsType;
			String type1 = analyseTerm(child);
			rhsType = type1;
			String type2 = "";
			if(children == 2){
				System.out.println("2nd Term");
				type2 = analyseTerm((SimpleNode) rhs.jjtGetChild(1));

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

	public void analyseIf(SimpleNode node){
		currFunction.ifScopeDeclarations.add(new ArrayList<Declaration>());
		int children = node.jjtGetNumChildren();

		analyseExprtest((SimpleNode) node.jjtGetChild(0));
		analyseStmtlst((SimpleNode) node.jjtGetChild(1));

		//else clause
		if(children > 2){

			ArrayList<Declaration> currScope = currFunction.getCurrIfScope();
			ArrayList<Declaration> ifScope = new ArrayList<Declaration>();
			System.out.println("ifScope size: "+currScope.size());

			for(Declaration var : currScope){
				ifScope.add(new Declaration(var));
			}
			currFunction.deleteCurrIfScopeDeclarations();

			analyseStmtlst((SimpleNode) node.jjtGetChild(2));
			ArrayList<Declaration> elseScope = currFunction.getCurrIfScope();
			System.out.println("elseScope size: "+elseScope.size());

			for(Declaration ifVar : ifScope){
				System.out.println("IfScope: "+ifVar.name+" ifStatus: "+ifVar.ifStatus);
				boolean found = false;

				int j = 0;
				for(Declaration elseVar: elseScope){
					System.out.println("ElseScope: "+elseVar.name+" ifStatus: "+elseVar.ifStatus);
					if(ifVar.name.compareTo(elseVar.name) == 0){
						found = true;

						if(ifVar.incompatibleIfStatus() || elseVar.incompatibleIfStatus()){
							elseVar.ifStatus = "incompatible";
						}
						else{
							if(ifVar.type.compareTo(elseVar.type) == 0)
								elseVar.ifStatus = "complete";
							else
								elseVar.ifStatus = "incompatible";
							}


							currFunction.addHigherIfScopeDeclaration(elseVar);
							elseScope.remove(j);

							break;
					}

					j++;

				}

				if(!found){
					if(ifVar.ifStatus.compareTo("") == 0);
						ifVar.ifStatus = "partial";
					currFunction.addHigherIfScopeDeclaration(ifVar);
					currFunction.localDeclarations.put(ifVar.name,ifVar);
				}
			}

		currFunction.clearCurrIfScope();
			}
			else{
				currFunction.clearCurrIfScope();
			}

		currFunction.ifScopeDeclarations.remove(currFunction.ifScopeDeclarations.size()-1);
	}

	public void analyseStmtlst(SimpleNode node){
		SimpleNode child;
		for(int i = 0; i < node.jjtGetNumChildren();i++){
			child = (SimpleNode) node.jjtGetChild(i);

			if(child.getId() == YalTreeConstants.JJTWHILE ){
				System.out.println("While");
				analyseExprtest((SimpleNode) child.jjtGetChild(0));
				analyseStmtlst((SimpleNode) child.jjtGetChild(1));
			}

			if(child.getId() == YalTreeConstants.JJTIF ){
				System.out.println("If");
				analyseIf(child);
			}

			if(child.getId() == YalTreeConstants.JJTASSIGN ){
				System.out.println("Assign");
				analyseAssign(child);
			}

			if(child.getId() == YalTreeConstants.JJTCALL ){
				System.out.println("Call");
				analyseCall(child);
			}
		}
	}

	public void analyseFunctionBody(SimpleNode node){
		String functionName = node.getValue();
		int children = node.jjtGetNumChildren();

		// extrai o nome da funcao caso seja chamada da forma modulo.funcName(...)
		if (functionName.indexOf(".")!=-1) {
			functionName = functionName.substring(functionName.indexOf(".")+1);
		}

		if(!functions.containsKey(functionName))
			return;

		currFunction = functions.get(functionName);

		SimpleNode child;
		for(int i = 0; i < children;i++){
			child = (SimpleNode) node.jjtGetChild(i);

			if(child.getId() == YalTreeConstants.JJTSTMTLST){
				System.out.println("Stmtlst");
				analyseStmtlst(child);
				break;
			}
		}

		if(currFunction.ret.type.compareTo("void") != 0) {
			if(!currFunction.ret.isInitialized())
				logError("Return "+currFunction.ret.name+" might not have been initialized on function "+currFunction.name);
		}
	}

	public void analyseFunction(SimpleNode node){
		int line = node.getLine();
		String functionName = node.getValue();
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

			if(functionName.compareTo("main")==0){
				logError(line,"Function main must return void");
			}
			else{
				returnType="integer";

				comp = ((SimpleNode) node.jjtGetChild(currChild)).getValue();
				if(comp.indexOf("[]")!=-1)
				{
					comp = comp.substring(0, comp.indexOf("["));
					returnType="array";
				}
				returnName=comp;
			}
			currChild++;
		}

		Function function = new Function(functionName, returnName, returnType);

		//parsing de argumentos, caso existam
		if ( ((SimpleNode) node.jjtGetChild(currChild)).getId() == YalTreeConstants.JJTVARLIST ) {
			//main nao tem argumentos
			if(functionName.compareTo("main")==0){
				logError(line,"Function main cannot have any arguments");
			}
			else{
				for (int i=0; i<node.jjtGetChild(currChild).jjtGetNumChildren();i++) {
					String paramName = ((SimpleNode) node.jjtGetChild(currChild).jjtGetChild(i)).getValue();
					String paramType="integer";

					if(paramName.indexOf("[]")!=-1)
					{
						paramName = paramName.substring(0, paramName.indexOf("["));
						paramType="array";
					}

					if ( (!function.parameters.containsKey(paramName)) ){
						function.addParameter(paramName,paramType);
					}
					else
						logError(line,"Repeated argument "+paramName+" on function "+functionName);
				}
			}
			currChild++;
		}

		functions.put(functionName, function);
	}

	public void analyseDeclaration(SimpleNode node){
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

				if(!analyseArraySize(child))
					return;

				rhsType = "array";
			}

			if(child.getId() == YalTreeConstants.JJTINTELEMENT){
				System.out.println("IntElement");
				rhsType = "integer";
			}

			if(newVar){
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

			if(node.getId() == YalTreeConstants.JJTDECLARATION)
			{
				analyseDeclaration(node);
			} else

			if(node.getId() == YalTreeConstants.JJTFUNCTION)
			{
				System.out.println("Function");
				analyseFunction(node);
			}
		}
		
		Declaration dec;
		for (String name : globalDeclarations.keySet()){
			System.out.println(name);
			dec = globalDeclarations.get(name);
			System.out.println(dec);
            if(!dec.init)
				logWarning("Global variable "+dec.name+" might not have been initialized");
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
				analyseFunctionBody(node);
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
	
	public void printWarnings(){
		String newLine = System.lineSeparator();
		for(String warning : warnings)
			System.out.println(warning+newLine);
	}

	private void logWarning(String msg){
		//System.out.println("Warning: "+msg);
		warnings.add("Warning: "+msg);
	}
	
	private void logWarning(int line, String msg){
		//System.out.println("Warning on line " +line+": "+msg);
		warnings.add("Warning on line " +line+": "+msg);
	}

	private void logError(String msg){
		//System.out.println("Error: "+msg);
		errors.add("Error: "+msg);
	}

	private void logError(int line, String msg){
		//System.out.println("Error on line " +line+": "+msg);
		errors.add("Error on line " +line+": "+msg);
	}
}
