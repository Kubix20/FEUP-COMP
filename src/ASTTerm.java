/* Generated By:JJTree: Do not edit this line. ASTTerm.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTTerm extends SimpleNode {
  protected String operator;
  protected String value;
  protected boolean print;

  public ASTTerm(int id) {
    super(id);
	this.operator="";
	this.value="";
	this.print = false;
  }

  public ASTTerm(Yal p, int id) {
    super(p, id);
  }

  public void setValues(String value, String operator){
    this.value = value;
    this.operator = operator;
	this.print = true;
  }

  @Override
  public boolean print() { return this.print; }

  @Override
  public String toString() { 
	//return YalTreeConstants.jjtNodeName[id] + " " + this.operator + " " + this.value; 
	return this.operator + " " + this.value;
  }

}
/* JavaCC - OriginalChecksum=245b857e1e84bf938a9b788e4784c2a1 (do not edit this line) */
