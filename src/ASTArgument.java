/* Generated By:JJTree: Do not edit this line. ASTArgument.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTArgument extends SimpleNode {
  protected String value;

  public ASTArgument(int id) {
    super(id);
  }

  public ASTArgument(Yal p, int id) {
    super(p, id);
  }

  public void setValues(String value){
    this.value = value;
  }
  
  @Override
  public String getValue(){ return this.value; }

  @Override
  public String toString() { return  YalTreeConstants.jjtNodeName[id] + " " + this.value; }


}
/* JavaCC - OriginalChecksum=d41e87d66ffa07b09d301e3b1eb4b87c (do not edit this line) */
