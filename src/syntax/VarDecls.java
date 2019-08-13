package syntax;

import java.util.List;
import java_cup.runtime.ComplexSymbolFactory.Location;
import visitor.Visitor;

public class VarDecls extends YasplNode {
  private final List<VarDeclaration> varsDeclarasions;
  public VarDecls(Location left, Location right,List<VarDeclaration> varDeclarations) {
    super(left, right);
    this.varsDeclarasions = varDeclarations;
  }

  
  public List<VarDeclaration> getVarsDeclarasions() {
    return varsDeclarasions;
  }


  @Override
  public <T, P> T accept(Visitor<T, P> visitor, P param) {
    return visitor.visit(this, param);
  }

}
