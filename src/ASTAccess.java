/* Generated By:JJTree: Do not edit this line. ASTAccess.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTAccess extends SimpleNode {
  protected String name;
  protected String size="";

  public ASTAccess(int id) {
    super(id);
  }

  public ASTAccess(Yal p, int id) {
    super(p, id);
  }

  public void setValues(String value, String size){
    this.name = value;
    this.size = size;
  }
  
  @Override
  public String getValue(){ return this.name + this.size; }

  @Override
  public String toString() { 
	return "Element " + this.name + this.size; 
  }


}
/* JavaCC - OriginalChecksum=0e7434008275352e0bc1b91ffae8bba4 (do not edit this line) */
