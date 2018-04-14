import java.util.HashMap;
import java.util.ArrayList;


public class SymbolTable{
	
	private String module;
	private HashMap<String,Function> functions;
	private HashMap<String,Declaration> globalDeclarations;
	private ArrayList<String> errors;
	
	public SymbolTable(){
		this.functions = new HashMap<String,Function>();
		this.globalDeclarations = new HashMap<String,Declaration>();
		this.errors = new ArrayList<String>();
	}
	
	public void stStmt(SimpleNode node){
		
	}
	
	public void stFunctionBody(SimpleNode node){
		
	}
	
	public void stFunction(SimpleNode node){
		
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
				//stFunction(node);
			}
		}
	}
	

	private void declareFunctions(SimpleNode root){
		SimpleNode node;
		for(int i=0; i< root.jjtGetNumChildren(); i++)
		{
			node = ((SimpleNode) root.jjtGetChild(i));

			if(node.getId() == YalTreeConstants.JJTFUNCTION)
			{
				stFunctionBody(node);
			}
			declareFunctions(node);
		}
		
	}
	
	public void analyze(SimpleNode root){
		
		if(root.getId() == YalTreeConstants.JJTMODULE){
			module = root.getValue();
		}
		
		declareGlobals(root);
		//declareFunctions();
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
            functionsStr += "      "+value+doubleNewLine;
		}
	
		String content= "Module "+this.module+newLine+
						"Global declarations:"+newLine+declarations+
						"Functions: "+ functionsStr;			
					 
		System.out.println(content);
	}
}