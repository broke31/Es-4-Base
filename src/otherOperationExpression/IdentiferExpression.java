package otherOperationExpression;

import java_cup.runtime.ComplexSymbolFactory.Location;
import syntax.Expression;
import visitor.Visitor;

public class IdentiferExpression extends Expression {
  private final String id;
  public IdentiferExpression(Location left, Location right,String id) {
    super(left, right);
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public <T, P> T accept(Visitor<T, P> visitor, P param) {
    return visitor.visit(this, param);
  }

}
