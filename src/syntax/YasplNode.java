package syntax;

import java_cup.runtime.ComplexSymbolFactory;
import visitor.Visitor;

public abstract class YasplNode {
  ComplexSymbolFactory.Location left,right;
  private ReturnType returnType;
  public YasplNode(ComplexSymbolFactory.Location left, ComplexSymbolFactory.Location right) {
    this.left = left;
    this.right = right;
    this.returnType = ReturnType.UNDEFINED;
  }

  public ReturnType getReturnType() {
    return returnType;
  }

  public ComplexSymbolFactory.Location getRightLocation() {
    return right;
  }

  public ComplexSymbolFactory.Location getLeftLocation() {
    return left;
  }

  public abstract <T, P> T accept(Visitor <T,P> visitor, P param);
  
  public ReturnType getNodeType() {
    return returnType;
}

/**
 * set a new node type
 * @param nodeType the new node type
 */
public void setNodeType(ReturnType nodeType) {
    this.returnType = nodeType;
}
}
