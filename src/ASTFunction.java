/* Generated By:JJTree: Do not edit this line. ASTFunction.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
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
  public String toString() { return YalTreeConstants.jjtNodeName[id] + " " + this.name; }
}
/* JavaCC - OriginalChecksum=f96fba650609b3f6cc22d69d6da3cc71 (do not edit this line) */
