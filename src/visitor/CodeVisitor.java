package visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import semantic.*;
import syntax.*;

public class CodeVisitor implements Visitor<String, Scope> {
  private static final String C_HEADER =
      "#include <stdio.h>\n" + "#include <stdbool.h>\n#include <string.h>\n#include<stdlib.h>\n";

  private static final String COMMENT_HEADER = "/*This file was generated by yasplcc*/";
  private static final String DECL_HEADER = "/***********Declarations***********/\n" + ""
      + "#define LENGTH  2048\n" + "char str1[LENGTH], str2[LENGTH];\n";
  private static final String MAIN_HEADER = "/***********Main*******************/";
  private SymbolTable symbolTable;
  private StringBuilder mallocString = new StringBuilder();

  // use the mallocString for create into main the malloc for global variable (string)
  public CodeVisitor(SymbolTable table) {
    super();
    this.symbolTable = table;
  }

  @Override
  public String visit(ArithOperation arithOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("(");
    builder.append(arithOperation.getLeftOperand().accept(this, param));
    builder.append(mapOperand(arithOperation.getOperation())).append(" ");
    builder.append(arithOperation.getRightOperand().accept(this, param));
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String visit(BooleanOperation booleanOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("(");
    builder.append(booleanOperation.getLeftOperand().accept(this, param)).append(" ");
    builder.append(mapOperand(booleanOperation.getOperation())).append(" ");
    builder.append(booleanOperation.getRightOperand().accept(this, param));
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String visit(RelopOperation relopOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("(");
    if ((relopOperation.getLeftOperand().getNodeType() == ReturnType.STRING)
        && (relopOperation.getRightOperand().getNodeType() == ReturnType.STRING)) {
      if(relopOperation.getLeftOperand() instanceof StringConst) {
      builder.append("strcmp(" + relopOperation.getLeftOperand().accept(this, param)+ ",");
      } else {
        builder.append("strcmp(" + relopOperation.getLeftOperand().accept(this, param).replaceAll("\\*", "")+ ",");

      }if(relopOperation.getRightOperand() instanceof StringConst) {
        builder.append(relopOperation.getRightOperand().accept(this, param) + ")" + "");
      } else {
        builder.append(relopOperation.getRightOperand().accept(this, param).replaceAll("\\*", " ") + ")" + "");

      }
          builder.append(mapOperand(relopOperation.getOperation()) + "0");
    } else {
      builder.append(relopOperation.getLeftOperand().accept(this, param)).append(" ");
      builder.append(mapOperand(relopOperation.getOperation())).append(" ");
      builder.append(relopOperation.getRightOperand().accept(this, param));
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String visit(MinusExpression minus, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("-");
    builder.append(minus.getExpr().accept(this, param));
    return builder.toString();
  }

  @Override
  public String visit(NotExpression notExpression, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("!");
    builder.append("(");
    builder.append(notExpression.getExpr().accept(this, param));
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String visit(TrueExpression trueExpression, Scope param) {
    return String.valueOf(trueExpression.getValue());
  }

  @Override
  public String visit(FalseExpression falseExpression, Scope param) {
    return String.valueOf(falseExpression.getValue());
  }

  @Override
  public String visit(IdentifierExpression identifierExpression, Scope param) {
    int addr = symbolTable.findAddr(identifierExpression.getName());

    param = this.symbolTable.lookup(addr);
    SemanticSymbol ss = param.get(addr);
    if (ss instanceof Variable) {
      Variable var = (Variable) ss;

      // add * if var is a string or var has partype = output or inout
      if (var.getReturnType() == ReturnType.STRING || var.getVarType() == VariableType.OUT
          || var.getVarType() == VariableType.INOUT) {
        return "*" + identifierExpression.getName();
      }
    }
    return identifierExpression.getName();
  }

  @Override
  public String visit(IntConst intConst, Scope param) {
    return String.valueOf(intConst.getValue());
  }

  @Override
  public String visit(DoubleConst doubleConst, Scope param) {
    return String.valueOf(doubleConst.getValue());
  }

  @Override
  public String visit(CharConst charConst, Scope param) {
    return "'" + String.valueOf(charConst.getValue()) + "'";
  }

  @Override
  public String visit(StringConst stringConst, Scope param) {
    return "\"" + String.valueOf(stringConst.getValue()) + "\"";
  }

  @Override
  public String visit(WhileOperation whileOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("while(");
    String condiction = whileOperation.getCondition().accept(this, param);
    String whileCompStat = whileOperation.getWhileCompStat().accept(this, param);
    builder.append(condiction).append(") {\n");
    builder.append(whileCompStat).append("\n}\n");
    return builder.toString();
  }

  @Override
  public String visit(IfThenOperation ifThenOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("if(");
    String condiction = ifThenOperation.getCondition().accept(this, param);
    String thenCompStat = ifThenOperation.getThenCompStat().accept(this, param);
    builder.append(condiction).append(") {\n");
    builder.append(thenCompStat).append("\n}\n");
    return builder.toString();
  }

  @Override
  public String visit(IfThenElseOperation ifThenElseOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("if(");
    String condiction = ifThenElseOperation.getCondition().accept(this, param);
    String thenCompStat = ifThenElseOperation.getThenCompStat().accept(this, param);
    String elseCompStat = ifThenElseOperation.getElseCompStat().accept(this, param);
    builder.append(condiction).append(") {\n");
    builder.append(thenCompStat).append("\n} else {\n");
    builder.append(elseCompStat).append("\n}\n");
    return builder.toString();
  }

  @Override
  public String visit(ReadOperation readOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("scanf(\"");
    StringJoiner sj = new StringJoiner(" ");
    String varsList = readOperation.getVars().accept(this, param);
    readOperation.getVars().getVarsNames()
        .forEach(v -> sj.add(mapType(v.getNodeType().getValue().toString())));
    builder.append(sj.toString());
    builder.append("\",");
    builder.append(varsList.replaceAll("\\*", ""));
    builder.append(");\n");
    return builder.toString();
  }

  @Override
  public String visit(Vars vars, Scope param) {
    StringJoiner inputs = new StringJoiner(", ");
    vars.getVarsNames().forEach(v -> {
      if (v.getNodeType().getValue().equals("string")) {
        inputs.add(v.accept(this, param));
      } else {
        inputs.add("&".concat(v.accept(this, param)));

      }
    });
    return inputs.toString();
  }

  @Override
  public String visit(Args args, Scope param) {
    StringBuilder builder = new StringBuilder();
    StringJoiner sj = new StringJoiner(", ");
    args.getExprArgs().forEach(e -> {
      sj.add(e.accept(this, param));
    });
    builder.append(sj).toString();
    return builder.toString();
  }

  @Override
  public String visit(WriteOperation writeOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    writeOperation.getArgs().getExprArgs().forEach(e -> {
      if (e instanceof RelopOperation) {
        builder.append(relopPrint(e, param));
      } else if (e instanceof ArithOperation) {
        builder.append(mathPrint(e, param));
      } else if (e instanceof iConst) {
        StringJoiner sj = new StringJoiner(",");
        builder.append("printf(\"");
        builder.append(strcat(e, param, sj) + "\\n\",");
        builder.append(sj.toString().replaceAll("\\*", ""));
        builder.append(");\n");
      }
    });
    return builder.toString();
  }

  private String strcat(Expression expr, Scope param, StringJoiner sj) {
    if (expr instanceof iConst) {
      if (expr instanceof StringConst) {
        sj.add("\"" + ((iConst<?>) expr).getValue().toString().replaceAll("\n", "\\\\n") + "\"");
      } else {
        sj.add(expr.accept(this, param).replace("*", ""));
      }
      return mapType(expr.getNodeType().getValue());
    }

    ArithOperation ao = (ArithOperation) expr;
    String app = strcat(ao.getLeftOperand(), param, sj) + strcat(ao.getRightOperand(), param, sj);
    return app;
  }

  private String relopPrint(Expression e, Scope param) {
    String toReturn;
    RelopOperation ro = (RelopOperation) e;
    toReturn = "printf(\"%d\\n\"," + ro.getLeftOperand().accept(this, param) + " "
        + mapOperand(ro.getOperation()) + " " + ro.getRightOperand().accept(this, param) + ");\n";
    return toReturn;
  }

  private String mathPrint(Expression writeOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    if (writeOperation instanceof ArithOperation) {
      ArithOperation ao = (ArithOperation) writeOperation;
      ReturnType left = ao.getLeftOperand().getNodeType();
      ReturnType right = ao.getRightOperand().getNodeType();
      // somma interi
      if (left == ReturnType.INTEGER && right == ReturnType.INTEGER) {
        return "printf(\"%d\\n\"," + ao.getLeftOperand().accept(this, param)
            + mapOperand(ao.getOperation()) + ao.getRightOperand().accept(this, param) + ");\n";
      } // somma un double con un double o un int
      else if ((left == ReturnType.DOUBLE && right == ReturnType.DOUBLE)
          || (left == ReturnType.INTEGER && right == ReturnType.DOUBLE)
          || (left == ReturnType.DOUBLE && right == ReturnType.INTEGER)) {
        return "printf(\"%lf\\n\"," + ao.getLeftOperand().accept(this, param)
            + mapOperand(ao.getOperation()) + ao.getRightOperand().accept(this, param) + ");\n";
      } // somma un char con un char o un int
      else if ((left == ReturnType.CHAR && right == ReturnType.CHAR)
          || (left == ReturnType.CHAR && right == ReturnType.INTEGER)
          || (left == ReturnType.INTEGER && right == ReturnType.CHAR)) {
        return "printf(\"%d\\n\"," + ao.getLeftOperand().accept(this, param)
            + mapOperand(ao.getOperation()) + ao.getRightOperand().accept(this, param) + ");\n";
      } else if (left == ReturnType.STRING || right == ReturnType.STRING) {
        builder.append("sprintf(str1,\"");
        StringJoiner sj = new StringJoiner(",");
        builder.append(strcat(writeOperation, param, sj));
        builder.append("\",");
        builder.append(sj.toString());
        builder.append(");\n");
        builder.append("printf(\"%s\\n\",str1);\n");
        return builder.toString();
      }
    }
    return "error";
  }

  @Override
  public String visit(AssignOperation assignOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    assignOperation.getVarName().accept(this, param);
    assignOperation.getExpr().accept(this, param);
    if (assignOperation.getVarName().getNodeType() == ReturnType.STRING) {
      builder.append("sprintf(str1,\"");
      StringJoiner sj = new StringJoiner(",");
      builder.append(strcat(assignOperation.getExpr(), param, sj) + "\",");
      builder.append(sj.toString());
      builder.append(");\n");
      builder
          .append(assignOperation.getVarName().getName() + " = malloc(sizeof(char) * LENGTH);\n");
      builder.append("strcpy(" + assignOperation.getVarName().getName() + ",str1);\n");
    } else {
      builder.append(assignOperation.getVarName().accept(this, param));
      builder.append("=");
      builder.append(assignOperation.getExpr().accept(this, param)).append(";\n");
    }
    return builder.toString();
  }

  @Override
  public String visit(CallWithParamsOperation callWithParamsOperation, Scope param) {
    String NameFunction = callWithParamsOperation.getFunctionName().accept(this, param);
    StringBuilder builder = new StringBuilder();
    StringJoiner paramsCall = new StringJoiner(", ");
    int addrFunction =
        this.symbolTable.findAddr(callWithParamsOperation.getFunctionName().getName());
    FunctionSymbol fs = (FunctionSymbol) this.symbolTable.lookup(addrFunction).get(addrFunction);
    String[] splitOutput = fs.getOutputDom().split("x");
    builder.append(NameFunction).append("(");
    for (int i = 0; i < callWithParamsOperation.getArgs().size(); i++) {
      if ((splitOutput[i].equals("out") || splitOutput[i].equals("inout"))
          && (!callWithParamsOperation.getArgs().get(i).getNodeType().getValue()
              .equals("string"))) {
        paramsCall
            .add("&" + callWithParamsOperation.getArgs().get(i).accept(this, param).toString());
      } else {
        if(callWithParamsOperation.getArgs().get(i) instanceof StringConst) {
          paramsCall.add(callWithParamsOperation.getArgs().get(i).accept(this, param).toString());
        }else {
        paramsCall.add(callWithParamsOperation.getArgs().get(i).accept(this, param).toString()
            .replace("*", ""));
        }
      }
    }
    builder.append(paramsCall);
    builder.append(");\n");
    return builder.toString();
  }


  @Override
  public String visit(CallWithoutParamsOperation callWithoutParamsOperation, Scope param) {
    callWithoutParamsOperation.getFunctionName().accept(this, param);
    StringBuilder builder = new StringBuilder();
    builder.append(callWithoutParamsOperation.getFunctionName().getName()).append("();\n");
    return builder.toString();
  }

  @Override
  public String visit(Program program, Scope param) {
    this.symbolTable.setCurrentScope(program.getAttachScope());
    String declarations = this.compactCode(program.getDeclsNode(), program.getAttachScope());
    String statements = this.compactCode(program.getStatementsNode(), program.getAttachScope());
    StringBuilder programBuilder = new StringBuilder();
    programBuilder.append(COMMENT_HEADER).append('\n').append(C_HEADER).append('\n')
        .append(DECL_HEADER).append('\n').append(declarations).append('\n').append(MAIN_HEADER)
        .append('\n').append("int main(void){\n").append(mallocString.toString()).append(statements)
        .append('\n').append(" return 0;\n}\n");

    this.symbolTable.exitScope();
    return programBuilder.toString();
  }

  @Override
  public String visit(TypeNode type, Scope param) {
    return type.getTypeName();
  }

  @Override
  public String visit(VarInitValue varInitValue, Scope param) {
    StringBuilder builder = new StringBuilder();
    if (varInitValue.getExpr() != null) {
      builder.append(varInitValue.getExpr().accept(this, param));
    }
    return builder.toString();
  }

  @Override
  public String visit(VarInitValueId varInitValueId, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append(varInitValueId.getVarName().accept(this, param));
    String initialValue = varInitValueId.getInitialValue().accept(this, param);
    // if has a initial value into global scope (head) add in C a global variable with the value
    if (!initialValue.equals("")) {
      builder.append("=");
      builder.append(initialValue).append("");
    }
    if (this.symbolTable.equalGlobalScope(param)) {
      if (varInitValueId.getVarName().getNodeType() == ReturnType.STRING) {
        mallocString
            .append(varInitValueId.getVarName().getName() + "= malloc(sizeof(char) * LENGTH);\n");
        if (!initialValue.equals("")) {
          mallocString.append(
              "strcpy(" + varInitValueId.getVarName().getName() + "," + initialValue + ");\n");
        }
      }
    } // close eq global scope
    else if (varInitValueId.getVarName().getNodeType() == ReturnType.STRING
        && initialValue.equals("")) {
      builder.append("=malloc(sizeof(char) * LENGTH)");
    }
    return builder.toString();
  }

  @Override
  public String visit(VarDeclaration varDeclaration, Scope param) {
    StringBuilder builder = new StringBuilder();
    StringJoiner varJoiner = new StringJoiner(", ");
    String type = varDeclaration.getTypeNode().accept(this, param);
    if (type.equals("string")) {
      varDeclaration.getVariables().forEach(v -> {
        varJoiner.add(v.accept(this, param));
      });

      builder.append(toCType(type)).append(' ').append(varJoiner.toString()).append(';')
          .append('\n');


    } else {
      varDeclaration.getVariables().forEach(v -> {

        varJoiner.add(v.accept(this, param));
      });
      builder.append(type).append(' ').append(varJoiner.toString()).append(';').append('\n');

    }
    return builder.toString();
  }

  @Override
  public String visit(ParDeclsNode parDeclsNode, Scope param) {
    StringBuilder builder = new StringBuilder();
    String type = parDeclsNode.getType().accept(this, param);
    String id = parDeclsNode.getVarName().accept(this, param);
    type = toCType(type);
    builder.append(type).append(" ");
    builder.append(id);
    return builder.toString();
  }

  @Override
  public String visit(ParType parType, Scope param) {
    return parType.getType();
  }

  @Override
  public String visit(DefFunctionWithParamsOperation defFunctionWithParamsOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    StringJoiner listParams = new StringJoiner(",");
    StringBuilder strcpyVarToReturn = new StringBuilder();
    List<String> varName = new ArrayList<String>();
    String FunctionName = defFunctionWithParamsOperation.getFunctionName().accept(this, param);
    int addrFunction =
        this.symbolTable.findAddr(defFunctionWithParamsOperation.getFunctionName().getName());
    FunctionSymbol fs = (FunctionSymbol) this.symbolTable.getCurrentScope().get(addrFunction);
    this.symbolTable.setCurrentScope(defFunctionWithParamsOperation.getAttachScope());
    defFunctionWithParamsOperation.getdefListParams().forEach(p -> {
      listParams.add(p.accept(this, defFunctionWithParamsOperation.getAttachScope()));
      varName.add(p.getVarName().getName());
    });
    String body = defFunctionWithParamsOperation.getBody().accept(this,
        defFunctionWithParamsOperation.getAttachScope());
    builder.append("void").append(" ");
    builder.append(FunctionName).append("(");
    builder.append(listParams).append(") {\n");

    List<String> varType = Arrays.asList(fs.getInputDom().split("x"));
    List<String> parType = Arrays.asList(fs.getOutputDom().split("x"));
    for (int i = 0; i < varType.size(); i++) {
      if (varType.get(i).equals("string") && !parType.get(i).equals("in")) {
        builder.append("char* _" + varName.get(i) + " = " + varName.get(i) + ";\n");
        strcpyVarToReturn.append("strcpy(_" + varName.get(i) + "," + varName.get(i) + ");\n");
      }
    }
    builder.append(body);
    builder.append(strcpyVarToReturn.toString());
    builder.append("\n}\n");
    this.symbolTable.exitScope();
    return builder.toString();
  }

  @Override
  public String visit(DefFunctionWithoutParamsOperation defFunctionWithoutParamsOperation,
      Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("void").append(" ");
    builder.append(defFunctionWithoutParamsOperation.getFunctionName().accept(this, param));
    this.symbolTable.setCurrentScope(defFunctionWithoutParamsOperation.getAttachScope());
    builder.append("() {\n");
    builder.append(defFunctionWithoutParamsOperation.getBody().accept(this,
        defFunctionWithoutParamsOperation.getAttachScope()));
    builder.append("\n}");
    this.symbolTable.exitScope();
    return builder.toString();
  }

  @Override
  public String visit(BodyNode body, Scope param) {
    StringBuilder builder = new StringBuilder();
    body.getVarDecls().forEach(v -> {
      builder.append(v.accept(this, param));
    });
    body.getStatementsNode().forEach(s -> {
      builder.append(s.accept(this, param));
    });
    builder.toString();
    return builder.toString();
  }

  @Override
  public String visit(CompStat compStat, Scope param) {
    return "    " + compactCode(compStat.getStatementsNode(), param);
  }

  private static String mapType(String type) {
    switch (type) {
      case "bool":
      case "int":
        return "%d";
      case "double":
        return "%lf";
      case "string":
        return "%s";
      case "char":
        return "%c";
      default:
        return "%d";
    }
  }

  private static String mapOperand(String op) {
    switch (op) {
      case "PLUS":
        return "+";
      case "MINUS":
        return "-";
      case "TIMES":
        return "*";
      case "DIV":
        return "/";
      case "MOD":
        return "%";
      case "GT":
        return ">";
      case "LT":
        return "<";
      case "GE":
        return ">=";
      case "LE":
        return "<=";
      case "EQ":
        return "==";
      case "AND":
        return "&&";
      case "OR":
        return "||";
      default:
        return "";
    }
  }

  private static String toCType(String type) {
    if (type.equals("string")) {
      type = "char";
    }
    return type;
  }

  private String compactCode(List<? extends YasplNode> list, Scope scope) {
    return list.stream().map(l -> l.accept(this, scope)).reduce("", String::concat);
  }

  @Override
  public String visit(SwitchBodyNode switchBodyNode, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("case ");
    builder.append(switchBodyNode.getExpr().accept(this, param));
    builder.append(": \n");
    builder.append(switchBodyNode.getBody().accept(this, param));
    builder.append("break; \n");
    return builder.toString();
  }

  @Override
  public String visit(SwitchOperation switchOperation, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("switch (");
    builder.append(switchOperation.getExpr().accept(this, param));
    builder.append(") {\n");
    if(switchOperation.getSwitchBody() != null) {
      switchOperation.getSwitchBody().forEach(b -> {
        builder.append(b.accept(this, param));
      });
    }
    if(switchOperation.getDefBody()!=null) {
    builder.append(switchOperation.getDefBody().accept(this, param));
    }
    builder.append("\n}\n");
    return builder.toString();
  }

  @Override
  public String visit(DefBodyNode defBodyNode, Scope param) {
    StringBuilder builder = new StringBuilder();
    builder.append("default: \n");
    builder.append(defBodyNode.getBody().accept(this, param));
    builder.append("break; \n");
    return builder.toString();
  }

}
