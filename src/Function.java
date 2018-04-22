import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;

public class Function {
	
	public String name;
	public boolean declared;
	public LinkedHashMap<String, Declaration> parameters;
	public HashMap<String, Declaration> localDeclarations;
	public Declaration ret;
	
	public Function(){
		name="";
		declared=false;
		parameters = new LinkedHashMap<String, Declaration>();
		localDeclarations = new HashMap<String, Declaration>();
	};
	
	public Function(String name, String returnName, String returnType){
		this.name=name;
		ret=new Declaration(returnName,returnType);
		declared=false;
		parameters = new LinkedHashMap<String, Declaration>();
		localDeclarations = new HashMap<String, Declaration>();
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
		parameters.put(name, new Declaration(name,type));
		return true;
	}
}
