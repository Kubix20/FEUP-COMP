package semantic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;

public class Function {

	public String name;
	public boolean declared;
	public LinkedHashMap<String, Declaration> parameters;
	public HashMap<String, Declaration> localDeclarations;
	public ArrayList<ArrayList<Declaration>> ifScopeDeclarations;
	public Declaration ret;

	public Function(String name){
		this.name=name;
		declared=false;
		parameters = new LinkedHashMap<String, Declaration>();
		localDeclarations = new HashMap<String, Declaration>();
		ifScopeDeclarations = new ArrayList<ArrayList<Declaration>>();
	};

	public Function(String name, String returnName, String returnType){
		this.name=name;
		ret=new Declaration(returnName,returnType,false);
		declared=false;
		parameters = new LinkedHashMap<String, Declaration>();
		localDeclarations = new HashMap<String, Declaration>();
		ifScopeDeclarations = new ArrayList<ArrayList<Declaration>>();
	};

	public String toString(){
		String newLine = System.lineSeparator();
		String doubleNewLine = newLine+newLine;

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

	public boolean addParameter(String name, String type){
		Declaration param = new Declaration(name);
		System.out.println("Param Type: "+type);
		param.init(type);
		parameters.put(name, param);
		return true;
	}

	public ArrayList<Declaration> getCurrIfScope(){
		int curr = ifScopeDeclarations.size()-1;
		return ifScopeDeclarations.get(curr);
	}

	public void deleteCurrIfScopeDeclarations(){
		int size = ifScopeDeclarations.size();
		ArrayList<Declaration> currScope = ifScopeDeclarations.get(size-1);

		for(Declaration var : currScope)
			localDeclarations.remove(var.name);

		currScope.clear();
	}

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

	public void addHigherIfScopeDeclaration(Declaration var){
		int size = ifScopeDeclarations.size();

		for(int i = 0; i < size-1;i++){
			ifScopeDeclarations.get(i).add(var);
		}
	}

	public void addIfScopeDeclaration(Declaration var){

		int size = ifScopeDeclarations.size();

		if(size > 0){
			ifScopeDeclarations.get(size-1).add(var);
		}
	}
}
