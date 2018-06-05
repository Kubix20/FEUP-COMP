package semantic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;

/**
	* Stores information about a function in the code
	*/
public class Function {

	// The function's name
	public String name;
	// Has this function been declared?
	public boolean declared;
	// Function's parameters (key = parameter name, value = parameter object)
	public LinkedHashMap<String, Declaration> parameters;
	// Function's local variables (key = variable name, value = variable object)
	public HashMap<String, Declaration> localDeclarations;
	// Function's if scope local variables
	public ArrayList<ArrayList<Declaration>> ifScopeDeclarations;
	// Function's return value
	public Declaration ret;

	/**
		* Init a function given its name
		* @param name function's name
		*/
	public Function(String name){
		this.name=name;
		declared=false;
		parameters = new LinkedHashMap<String, Declaration>();
		localDeclarations = new HashMap<String, Declaration>();
		ifScopeDeclarations = new ArrayList<ArrayList<Declaration>>();
	};

	/**
		* Init a function given its name and return type
		* @param name function's name
		* @param returnName return variable name
		* @param returnType return variable type
		*/
	public Function(String name, String returnName, String returnType){
		this.name=name;
		ret=new Declaration(returnName,returnType,false);
		declared=false;
		parameters = new LinkedHashMap<String, Declaration>();
		localDeclarations = new HashMap<String, Declaration>();
		ifScopeDeclarations = new ArrayList<ArrayList<Declaration>>();
	};

	/**
		* Prints the function as a string
		* @return function as a string
		*/
	public String toString(){
		String newLine = System.lineSeparator();

		String params ="";
		for (String name: parameters.keySet()){
            String value = parameters.get(name).toString();
            params += "         "+value+newLine;
		}

		String locals ="";
		for (String name: localDeclarations.keySet()){
            String value = localDeclarations.get(name).toString();
            locals += "         "+value+newLine;
		}

		String retStr = "void";
		if(ret.type.compareTo("void")!=0)
			retStr = ret.toString();

		String content= "   Function "+name+newLine+
						"      Params:"+newLine+params+
						"      Locals:"+newLine+locals+
						"      Return:"+newLine+
						"         "+retStr;


		return content;
	}

	/**
		* Adds a parameter to the function
		* @param name parameter name
		* @param type parameter type
		*/
	public boolean addParameter(String name, String type){
		Declaration param = new Declaration(name);
		System.out.println("Param Type: "+type);
		param.init(type);
		parameters.put(name, param);
		return true;
	}

	/**
		* Get current if scope
		* @return local declarations to current if scope
		*/
	public ArrayList<Declaration> getCurrIfScope(){
		int curr = ifScopeDeclarations.size()-1;
		return ifScopeDeclarations.get(curr);
	}

	/**
		* Clear current if scope declarations
		*/
	public void deleteCurrIfScopeDeclarations(){
		int size = ifScopeDeclarations.size();
		ArrayList<Declaration> currScope = ifScopeDeclarations.get(size-1);

		for(Declaration var : currScope)
			localDeclarations.remove(var.name);

		currScope.clear();
	}

	/**
		* Clear current if scope
		*/
	public void clearCurrIfScope(){

		int size = ifScopeDeclarations.size();

		if(size == 0)
			return;

		ArrayList<Declaration> currScope = ifScopeDeclarations.get(size-1);

		for(Declaration var : currScope){

			System.out.println("Cleared: "+var.name);

			if(var.ifStatus.compareTo("") == 0)
				var.ifStatus = "partial";

			for(int i = 0; i < size-1;i++){
				ifScopeDeclarations.get(i).add(var);
			}
		}

		currScope.clear();
	}

	/**
		* Add a declaration to higher if scope
		* @param var declaration to be added
		*/
	public void addHigherIfScopeDeclaration(Declaration var){
		int size = ifScopeDeclarations.size();

		for(int i = 0; i < size-1;i++){
			ifScopeDeclarations.get(i).add(var);
		}
	}

	/**
		* Add a declaration to current if scope
		* @param var declaration to be added
		*/
	public void addIfScopeDeclaration(Declaration var){

		int size = ifScopeDeclarations.size();

		if(size > 0){
			ifScopeDeclarations.get(size-1).add(var);
		}
	}
}
