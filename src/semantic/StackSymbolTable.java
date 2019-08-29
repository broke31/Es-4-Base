package semantic;

import java.util.Stack;
import lexical.StringTable;

/* implementa una struttura dati della symbol table */
public class StackSymbolTable extends Stack<Scope> implements SymbolTable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private final StringTable table;
  public StackSymbolTable(StringTable st) {
    this.table = st;
  }

  @Override
  public void enterScope() {
    this.push(new Scope());
  }

  @Override
  public void exitScope() {
   this.pop();
  }


  @Override
  public boolean prob(int addr) {
    return this.getCurrentScope().containsKey(addr);
  }

  @Override
  public void add(int addr, SemanticSymbol semanticSymbol) {
    this.getCurrentScope().put(addr, semanticSymbol);

  }

  @Override
  public Scope lookup(int addr) {
    for (int i = this.size() - 1; i >= 0; i--) {
      if (this.elementAt(i).containsKey(addr)) {
        return this.elementAt(i);
      }
    }
    return null;
  }

  @Override
  public int findAddr(String lexema) {
    return this.table.getAddr(lexema);
  }

  @Override
  public Scope getCurrentScope() {
    return this.peek();
  }

  @Override
  public StringTable getStringTable() {
    return this.table;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < this.size(); i++) {
      sb.append("Scope n°").append(i).append('\n').append(this.elementAt(i).toString())
          .append('\n');
    }
    return sb.toString();
  }


}