/* Generated By:JJTree: Do not edit this line. ASTFunction.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package tree;

public
class ASTFunction extends SimpleNode {

  protected String name;

  public ASTFunction(int id) {
    super(id);
  }

  public ASTFunction(Yal p, int id) {
    super(p, id);
  }

  public void setValues(String value){
    this.name = value;
  }

  @Override
  public String getValue(){ return this.name; }

  @Override
  public String toString() { return YalTreeConstants.jjtNodeName[id] + " " + this.name; }
}
/* JavaCC - OriginalChecksum=77b54aaeca1f40ece67e91439245fc30 (do not edit this line) */
