/* Generated By:JJTree: Do not edit this line. ASTRhs.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTRhs extends SimpleNode {
  protected String operator;
  protected boolean print;

  public ASTRhs(int id) {
    super(id);
	this.operator = "";
	this.print = false;
  }

  public ASTRhs(Yal p, int id) {
    super(p, id);
  }

  public void setValues(String value){
    this.operator = value;
	this.print = true;
  }
  
  @Override
  public boolean print() { return this.print; }

  @Override
  public String toString() { return /*YalTreeConstants.jjtNodeName[id]; + " " + */this.operator; }

}
/* JavaCC - OriginalChecksum=597ed5e72fbe47b2ad2ba8d7f544824e (do not edit this line) */
