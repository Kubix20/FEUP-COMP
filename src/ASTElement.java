/* Generated By:JJTree: Do not edit this line. ASTElement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTElement extends SimpleNode {
  protected String value;
  protected String access;

  public ASTElement(int id) {
    super(id);
	this.value = "";
	this.access = "";
  }

  public ASTElement(Yal p, int id) {
    super(p, id);
  }

  public void setValues(String value, String access){
    this.value = value;
	if(access != "")
		this.access = "[]";
  }
  
  @Override
  public String getValue(){ return this.value + this.access; }

  @Override
  public String toString() { 
	return YalTreeConstants.jjtNodeName[id] + " " + this.value + this.access; 
  }
}
/* JavaCC - OriginalChecksum=7a5adb44de950c1b6633af76600b4a86 (do not edit this line) */
