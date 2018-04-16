import java.util.ArrayList;
import java.util.HashMap;

public class Function {
	
	public String name;
	public boolean declared;
	public ArrayList<Declaration> parameters;
	public HashMap<String, Declaration> localDeclarations;
	public Declaration ret;
	
	public Function(){
		name="";
		declared=false;
		parameters = new ArrayList<Declaration>();
		localDeclarations = new HashMap<String, Declaration>();
	};
	
	public Function(String name, String returnName, String returnType){
		this.name=name;
		ret=new Declaration(returnName,returnType);
		declared=false;
		parameters = new ArrayList<Declaration>();
		localDeclarations = new HashMap<String, Declaration>();
	};

	public String toString(){
		String newLine = System.lineSeparator();
		String doubleNewLine = newLine+newLine;
	
		String params="";
		for (Declaration parameter : parameters){
            params += "         "+parameter+newLine;
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
	
	public boolean containsParameter(String name){
		for(Declaration parameter : parameters){
			if(parameter.name.compareTo(name) == 0)
				return true;
		}
		
		return false;
	}
	
	public boolean addParameter(String name, String type){
		parameters.add(new Declaration(name,type));
		return true;
	}
}
