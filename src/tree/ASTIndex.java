/* Generated By:JJTree: Do not edit this line. ASTIndex.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package tree;

public
class ASTIndex extends SimpleNode {
  protected String value;

  public ASTIndex(int id) {
    super(id);
	this.value="";
  }

  public ASTIndex(Yal p, int id) {
    super(p, id);
  }

  public void setValues(String value){
	this.value = value;
  }


  @Override
  public String getValue(){ return this.value; }

  @Override
  public String toString() {
	/*return YalTreeConstants.jjtNodeName[id] +  "[" + this.value + "]";*/
    return "At " +	this.value;
  }


}
/* JavaCC - OriginalChecksum=88adddf20e6818ae23934fbc58356bbb (do not edit this line) */
