/* Generated By:JJTree: Do not edit this line. ASTRhs.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package tree;

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
  public String getValue(){ return this.operator; }

  @Override
  public boolean print() { return this.print; }

  @Override
  public String toString() { return "Operator " + this.operator; }

}
/* JavaCC - OriginalChecksum=0d37bfd17ba77d348d53d731f0025b14 (do not edit this line) */
