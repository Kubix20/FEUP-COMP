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
	
	public static boolean isInt(String i)
	{
		return i.matches("^\\d+");
	}
	
	public void stAssign(SimpleNode node){
		
	}
	
	public void stCall(SimpleNode node){
		
	}
	
	public void stStmt(SimpleNode node){
		
	}
	
	public void stFunctionBody(SimpleNode node){
		
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
				if ( (!function.containsParameter(paramName)) ){
					
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
		errors.add("Warning on line " +line+": "+msg);
	}
	
	private void logError(int line, String msg){
		errors.add("Error on line " +line+": "+msg);
	}
}