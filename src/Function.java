import java.util.ArrayList;
import java.util.HashMap;

public class Function {
	
	public String name;
	public boolean declared;
	public ArrayList<String> parameters;
	public ArrayList<String> paramtype;
	public HashMap<String, Declaration> localDeclarations;
	public Declaration ret;
	
	public Function(){
		name="";
		declared=false;
		parameters = new ArrayList<String>();
		paramtype = new ArrayList<String>();
		localDeclarations = new HashMap<String, Declaration>();
	};
	
	public Function(String n, String r, String rn){
		name=n;
		ret=new Declaration(rn,r);
		declared=false;
		parameters = new ArrayList<String>();
		paramtype = new ArrayList<String>();
		localDeclarations = new HashMap<String, Declaration>();
	};

	public String toString(){
		if (ret.type.compareTo("void")==0) {
			return "Function " + name;
		}
		return "Function " + name + " with return value";
	}
}
