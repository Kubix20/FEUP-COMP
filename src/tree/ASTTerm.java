/* Generated By:JJTree: Do not edit this line. ASTTerm.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package tree;

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
  public String getValue(){ return this.operator + " " + this.value; }

  @Override
  public String toString() {
	return "Element " + this.operator + this.value;
  }

}
/* JavaCC - OriginalChecksum=150744bca6bd8a5d0fba6f6bab151bd3 (do not edit this line) */