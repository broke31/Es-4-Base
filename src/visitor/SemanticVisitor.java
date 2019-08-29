package visitor;

import java.util.List;
import java.util.logging.Logger;
import semantic.FunctionSymbol;
import semantic.ReturnType;
import semantic.Scope;
import semantic.SemanticSymbol;
import semantic.SymbolTable;
import semantic.Variable;
import syntax.*;

public class SemanticVisitor implements Visitor<ReturnType, Logger> {
  private final SymbolTable symbolTable;

  public SemanticVisitor(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  @Override
  public ReturnType visit(ArithOperation arithOperation, Logger param) {
    arithOperation.getLeftOperand().accept(this, param);
    arithOperation.getRightOperand().accept(this, param);
    if (this.isUndefined(arithOperation.getLeftOperand())
        && this.isUndefined(arithOperation.getRightOperand())) {
      ReturnType left = arithOperation.getLeftOperand().getReturnType();
      ReturnType right = arithOperation.getRightOperand().getReturnType();
      if (left == ReturnType.STRING || left == ReturnType.CHAR || right == ReturnType.STRING
          || right == ReturnType.CHAR) {
        arithOperation.setNodeType(ReturnType.VOID);
      } else if ((left == ReturnType.INTEGER || left == ReturnType.DOUBLE)
          && (right == ReturnType.INTEGER || right == ReturnType.DOUBLE)) {
        if (left == ReturnType.DOUBLE || right == ReturnType.DOUBLE) {
          arithOperation.setNodeType(ReturnType.VOID);
        } else {
          arithOperation.setNodeType(ReturnType.VOID);
        }
      } else {
        param.severe(GenerateError.ErrorGenerate("ArithOperation: Expected Integer or Double",
            arithOperation));
        arithOperation.setNodeType(ReturnType.UNDEFINED);
      }
    } // end if isUndefined
    else {
      param.severe(GenerateError.ErrorGenerate("ArithOperation not allowed", arithOperation));
      arithOperation.setNodeType(ReturnType.UNDEFINED);
    }
    return arithOperation.getNodeType();
  }

  @Override
  public ReturnType visit(BooleanOperation booleanOperation, Logger param) {
    booleanOperation.getLeftOperand().accept(this, param);
    booleanOperation.getRightOperand().accept(this, param);
    if (this.isUndefined(booleanOperation.getLeftOperand())
        && (this.isUndefined(booleanOperation.getRightOperand()))) {
      ReturnType left = booleanOperation.getLeftOperand().getNodeType();
      ReturnType right = booleanOperation.getRightOperand().getNodeType();
      if (left == ReturnType.BOOLEAN && right == ReturnType.BOOLEAN) {
        booleanOperation.setNodeType(ReturnType.BOOLEAN);
      } else {
        param.severe(
            GenerateError.ErrorGenerate("BooleanOperation: Expected Boolean", booleanOperation));
        booleanOperation.setNodeType(ReturnType.UNDEFINED);
      }
    } // else isUndefined
    else {
      param.severe(GenerateError.ErrorGenerate("BooleanOperation not allowed", booleanOperation));
      booleanOperation.setNodeType(ReturnType.UNDEFINED);
    }
    return booleanOperation.getNodeType();
  }

  @Override
  public ReturnType visit(RelopOperation relopOperation, Logger param) {
    relopOperation.getLeftOperand().accept(this, param);
    relopOperation.getRightOperand().accept(this, param);
    if (this.isUndefined(relopOperation.getLeftOperand())
        && this.isUndefined(relopOperation.getRightOperand())) {
      ReturnType left = relopOperation.getLeftOperand().getNodeType();
      ReturnType right = relopOperation.getRightOperand().getNodeType();
      if ((left == ReturnType.INTEGER || left == ReturnType.DOUBLE)
          && (right == ReturnType.INTEGER || right == ReturnType.DOUBLE)) {
        relopOperation.setNodeType(ReturnType.BOOLEAN);
      } else if (left == ReturnType.CHAR && right == ReturnType.CHAR
          && relopOperation.getOperation().equals("EQ")) {
        relopOperation.setNodeType(ReturnType.BOOLEAN);
      } else if (left == ReturnType.STRING && right == ReturnType.STRING
          && relopOperation.getOperation().equals("EQ")) {
        relopOperation.setNodeType(ReturnType.BOOLEAN);
      } else if (left == ReturnType.BOOLEAN && right == ReturnType.BOOLEAN) {
        if (relopOperation.getOperation().equals("EQ") || relopOperation.equals("NOT")) {
          relopOperation.setNodeType(ReturnType.BOOLEAN);
        }
      } else {
        param.severe(GenerateError.ErrorGenerate("RelopOperation: Expected Integer or Double",
            relopOperation));
        relopOperation.setNodeType(ReturnType.UNDEFINED);
      }
    } // else is Undefined
    else {
      param.severe(GenerateError.ErrorGenerate("RelopOperation not allowed", relopOperation));
      relopOperation.setNodeType(ReturnType.UNDEFINED);
    }
    return relopOperation.getNodeType();
  }

  @Override
  public ReturnType visit(MinusExpression minus, Logger param) {
    minus.getExpr().accept(this, param);
    if (this.isUndefined(minus.getExpr())) {
      ReturnType type = minus.getExpr().getNodeType();
      if (type == ReturnType.INTEGER) {
        minus.setNodeType(ReturnType.INTEGER);
      } else if (type == ReturnType.DOUBLE) {
        minus.setNodeType(ReturnType.DOUBLE);
      } else {
        param.severe(
            GenerateError.ErrorGenerate("MinusExpression: Expected Integer or Double", minus));
        minus.setNodeType(ReturnType.UNDEFINED);
      }
    } // else isUndefined
    else {
      param.severe(GenerateError.ErrorGenerate("MinusExpression not allowed", minus));
      minus.setNodeType(ReturnType.UNDEFINED);
    }
    return minus.getNodeType();
  }

  @Override
  public ReturnType visit(NotExpression notExpression, Logger param) {
    notExpression.getExpr().accept(this, param);
    if (this.isUndefined(notExpression.getExpr())) {
      ReturnType type = notExpression.getExpr().getNodeType();
      if (type == ReturnType.BOOLEAN) {
        notExpression.setNodeType(ReturnType.BOOLEAN);
      } else {
        param.severe(GenerateError.ErrorGenerate("NotExpression: expected Boolean", notExpression));
        notExpression.setNodeType(ReturnType.UNDEFINED);
      }
    } else {
      param.severe(GenerateError.ErrorGenerate("NotExpression not allowed", notExpression));
      notExpression.setNodeType(ReturnType.UNDEFINED);
    }
    return notExpression.getNodeType();
  }

  @Override
  public ReturnType visit(TrueExpression trueExpression, Logger param) {
    trueExpression.setNodeType(ReturnType.BOOLEAN);
    return trueExpression.getNodeType();
  }

  @Override
  public ReturnType visit(FalseExpression falseExpression, Logger param) {
    falseExpression.setNodeType(ReturnType.BOOLEAN);
    return falseExpression.getNodeType();
  }

  @Override
  public ReturnType visit(IdentifierExpression identiferExpression, Logger param) {
    int address = this.symbolTable.findAddr(identiferExpression.getName());
    Scope scope = this.symbolTable.lookup(address);
    ReturnType type = (scope != null) ? scope.get(address).getReturnType() : ReturnType.UNDEFINED;
    identiferExpression.setNodeType(type);
    return identiferExpression.getNodeType();
  }

  @Override
  public ReturnType visit(IntConst intConst, Logger param) {
    intConst.setNodeType(ReturnType.INTEGER);
    return intConst.getNodeType();
  }

  @Override
  public ReturnType visit(DoubleConst doubleConst, Logger param) {
    doubleConst.setNodeType(ReturnType.DOUBLE);
    return doubleConst.getNodeType();
  }

  @Override
  public ReturnType visit(CharConst charConst, Logger param) {
    charConst.setNodeType(ReturnType.CHAR);
    return charConst.getNodeType();
  }

  @Override
  public ReturnType visit(StringConst stringConst, Logger param) {
    stringConst.setNodeType(ReturnType.STRING);
    return stringConst.getNodeType();
  }

  @Override
  public ReturnType visit(WhileOperation whileOperation, Logger param) {
    whileOperation.getCondition().accept(this, param);
    whileOperation.getWhileCompStat().accept(this, param);
    if (this.isUndefined(whileOperation.getCondition())
        && this.isUndefined(whileOperation.getWhileCompStat())) {
      if (whileOperation.getCondition().getNodeType() == ReturnType.BOOLEAN) {
        whileOperation.setNodeType(ReturnType.VOID);
      } else {
        param.severe(
            GenerateError.ErrorGenerate("While Operation: Invalid Condition", whileOperation));
        whileOperation.setNodeType(ReturnType.UNDEFINED);
      }
    } else {
      param.severe(GenerateError.ErrorGenerate("Error While Operation", whileOperation));
      whileOperation.setNodeType(ReturnType.UNDEFINED);
    }
    return whileOperation.getNodeType();
  }

  @Override
  public ReturnType visit(IfThenOperation ifThenOperation, Logger param) {
    ifThenOperation.getCondition().accept(this, param);
    ifThenOperation.getThenCompStat().accept(this, param);
    if (this.isUndefined(ifThenOperation.getCondition())
        && this.isUndefined(ifThenOperation.getThenCompStat())) {
      if (ifThenOperation.getCondition().getNodeType() == ReturnType.BOOLEAN) {
        ifThenOperation.setNodeType(ReturnType.VOID);
      } else {
        param.severe(
            GenerateError.ErrorGenerate("IfThen Operation: invalid condition", ifThenOperation));
        ifThenOperation.setNodeType(ReturnType.UNDEFINED);
      }
    } else {
      param.severe(GenerateError.ErrorGenerate("Error IfThen Operation", ifThenOperation));
      ifThenOperation.setNodeType(ReturnType.UNDEFINED);
    }
    return ifThenOperation.getNodeType();
  }

  @Override
  public ReturnType visit(IfThenElseOperation ifThenElseOperation, Logger param) {
    ifThenElseOperation.getCondition().accept(this, param);
    ifThenElseOperation.getThenCompStat().accept(this, param);
    ifThenElseOperation.getElseCompStat().accept(this, param);
    if (this.isUndefined(ifThenElseOperation.getCondition())
        && this.isUndefined(ifThenElseOperation.getThenCompStat())
        && this.isUndefined(ifThenElseOperation.getElseCompStat())) {
      if (ifThenElseOperation.getCondition().getNodeType() == ReturnType.BOOLEAN) {
        ifThenElseOperation.setNodeType(ReturnType.VOID);
      } else {
        param.severe(GenerateError.ErrorGenerate("IfThenElse Operation: invalid condition",
            ifThenElseOperation));
        ifThenElseOperation.setNodeType(ReturnType.UNDEFINED);
      }
    } else {
      param.severe(GenerateError.ErrorGenerate("Error IfThenElse Operation", ifThenElseOperation));
      ifThenElseOperation.setNodeType(ReturnType.UNDEFINED);
    }
    return ifThenElseOperation.getNodeType();
  }

  @Override
  public ReturnType visit(ReadOperation readOperation, Logger param) {
    readOperation.getVars().accept(this, param);
    if (this.checkAll(readOperation.getVars().getVarsNames())) {
      readOperation.setNodeType(ReturnType.VOID);
    } else {
      param.severe(GenerateError.ErrorGenerate("Error Read Operation", readOperation));
      readOperation.setNodeType(ReturnType.UNDEFINED);
    }
    return readOperation.getNodeType();
  }

  @Override
  public ReturnType visit(Vars vars, Logger param) {
    vars.getVarsNames().forEach(i -> i.accept(this, param));
    if (checkAll(vars.getVarsNames())) {
      vars.setNodeType(ReturnType.VOID);
    } else {
      vars.setNodeType(ReturnType.UNDEFINED);
      param.severe(GenerateError.ErrorGenerate("Error Vars", vars));
    }
    return vars.getNodeType();
  }

  @Override
  public ReturnType visit(Args args, Logger param) {
    args.getExprArgs().forEach(e -> e.accept(this, param));
    if (checkAll(args.getExprArgs())) {
      args.setNodeType(ReturnType.VOID);
    } else {
      args.setNodeType(ReturnType.UNDEFINED);
      param.severe(GenerateError.ErrorGenerate("Error Args", args));
    }
    return args.getNodeType();
  }

  @Override
  public ReturnType visit(WriteOperation writeOperation, Logger param) {
    writeOperation.getArgs().accept(this, param);
    if (this.checkAll(writeOperation.getArgs().getExprArgs())) {
      writeOperation.setNodeType(ReturnType.VOID);
    } else {
      param.severe(GenerateError.ErrorGenerate("Error Write Operation", writeOperation));
      writeOperation.setNodeType(ReturnType.UNDEFINED);
    }
    return writeOperation.getNodeType();
  }

  @Override
  public ReturnType visit(AssignOperation assignOperation, Logger param) {
    assignOperation.getVarName().accept(this, param);
    assignOperation.getExpr().accept(this, param);
    if (this.isUndefined(assignOperation.getVarName())
        && (this.isUndefined(assignOperation.getExpr()))) {
      ReturnType identifier = assignOperation.getVarName().getNodeType();
      ReturnType expression = assignOperation.getExpr().getNodeType();
      if (identifier == ReturnType.INTEGER && expression == ReturnType.INTEGER) {
        assignOperation.setNodeType(ReturnType.VOID);
      } else if (identifier == ReturnType.DOUBLE && expression == ReturnType.DOUBLE) {
        assignOperation.setNodeType(ReturnType.VOID);

      } else if (identifier == ReturnType.CHAR && expression == ReturnType.CHAR) {
        assignOperation.setNodeType(ReturnType.VOID);
      } else if (identifier == ReturnType.STRING && expression == ReturnType.STRING) {
        assignOperation.setNodeType(ReturnType.VOID);

      } else if (identifier == ReturnType.BOOLEAN && expression == ReturnType.BOOLEAN) {
        assignOperation.setNodeType(ReturnType.VOID);
      } else {
        param.severe(GenerateError.ErrorGenerate(
            "Assign Operation: expected " + identifier + " for variable '"
                + assignOperation.getVarName().getName() + "' but found " + expression,
            assignOperation));
        assignOperation.setNodeType(ReturnType.UNDEFINED);
      }
    } else {
      param.severe(GenerateError.ErrorGenerate("Error Assign Operation", assignOperation));
      assignOperation.setNodeType(ReturnType.UNDEFINED);
    }
    return assignOperation.getNodeType();
  }

  @Override
  public ReturnType visit(CallWithParamsOperation callWithParamsOperation, Logger param) {
    callWithParamsOperation.getFunctionName().accept(this, param);
    callWithParamsOperation.getArgs().forEach(a -> a.accept(this, param));
    if (this.isUndefined(callWithParamsOperation.getFunctionName())) {
      int addr = this.symbolTable.findAddr(callWithParamsOperation.getFunctionName().getName());
      FunctionSymbol fs = (FunctionSymbol) this.symbolTable.getCurrentScope().get(addr);
      if (fs.getInputDom().equals(callWithParamsOperation.getDomain())) {
        callWithParamsOperation.setNodeType(ReturnType.VOID);
      } else {
        param.severe(GenerateError.ErrorGenerate("CallWithParamsOperation: invalid parameters "
            + "expected: '"+fs.getInputDom()+"' but found: '"+callWithParamsOperation.getDomain()+""
                + "'",
            callWithParamsOperation));
        callWithParamsOperation.setNodeType(ReturnType.UNDEFINED);
      }
    } else {
      callWithParamsOperation.setNodeType(ReturnType.UNDEFINED);
      param.severe(
          GenerateError.ErrorGenerate("Error callWithParamsOperation", callWithParamsOperation));
    }
    return callWithParamsOperation.getNodeType();
  }

  @Override
  public ReturnType visit(CallWithoutParamsOperation callWithoutParamsOperation, Logger param) {
    callWithoutParamsOperation.getFunctionName().accept(this, param);
    if (this.isUndefined(callWithoutParamsOperation.getFunctionName())) {
      int addr = this.symbolTable.findAddr(callWithoutParamsOperation.getFunctionName().getName());
      FunctionSymbol fs = (FunctionSymbol) this.symbolTable.getCurrentScope().get(addr);
      if (fs.getInputDom().equals(callWithoutParamsOperation.getDomain())) {
        callWithoutParamsOperation.setNodeType(ReturnType.VOID);
      } else {
        param.severe(GenerateError.ErrorGenerate("CallWithoutParamsOperation: invalid parameters"
            + "expected: "+fs.getInputDom()+"but found "+callWithoutParamsOperation.getDomain(),
            callWithoutParamsOperation));
        callWithoutParamsOperation.setNodeType(ReturnType.UNDEFINED);
      }
    } else {
      callWithoutParamsOperation.setNodeType(ReturnType.UNDEFINED);
      param.severe(GenerateError.ErrorGenerate("Error CallWithoutParamsOperation",
          callWithoutParamsOperation));
    }
    return callWithoutParamsOperation.getNodeType();
  }

  @Override
  public ReturnType visit(Program program, Logger param) {
    this.symbolTable.enterScope();
    program.getDeclsNode().forEach(d -> d.accept(this, param));
    program.getStatementsNode().forEach(s -> s.accept(this, param));
    if (checkAll(program.getDeclsNode()) && (checkAll(program.getStatementsNode()))) {
      program.setNodeType(ReturnType.VOID);
    } else {
      program.setNodeType(ReturnType.UNDEFINED);
      param.severe(GenerateError.ErrorGenerate("Error Program", program));
    }
    program.attachScope(this.symbolTable.getCurrentScope());
    this.symbolTable.exitScope();
    return program.getNodeType();
  }

  @Override
  public ReturnType visit(TypeNode type, Logger param) {
    if (type.getTypeName().equals("int") || type.getTypeName().equals("double")
        || type.getTypeName().equals("string") || type.getTypeName().equals("bool")
        || type.getTypeName().equals("char")) {
      type.setNodeType(ReturnType.getEnumFor(type.getTypeName()));
    } else {
      param.severe(GenerateError.ErrorGenerate("TypeNode: invalid type", type));
      type.setNodeType(ReturnType.UNDEFINED);
    }
    return type.getNodeType();
  }

  @Override
  public ReturnType visit(VarInitValue varInitValue, Logger param) {
    if (varInitValue.getExpr() != null) {
      varInitValue.getExpr().accept(this, param);
      if (isUndefined(varInitValue.getExpr())) {
        varInitValue.setNodeType(ReturnType.VOID);
      } else {
        varInitValue.setNodeType(ReturnType.UNDEFINED);
        param.severe(GenerateError.ErrorGenerate("Error VarInitValue", varInitValue));
      }
      return varInitValue.getNodeType();
    }
    varInitValue.setNodeType(ReturnType.VOID);
    return varInitValue.getNodeType();
  }

  @Override
  public ReturnType visit(VarInitValueId varInitValueId, Logger param) {
    varInitValueId.getVarName().accept(this, param);
    if(varInitValueId.getInitialValue().getExpr()!=null) {
    varInitValueId.getInitialValue().accept(this, param);
    if (isUndefined(varInitValueId.getVarName()) && isUndefined(varInitValueId.getInitialValue())) {
      if (varInitValueId.getVarName().getNodeType()
          .equals(varInitValueId.getInitialValue().getExpr().getNodeType())) {
        varInitValueId.setNodeType(ReturnType.VOID);
      }
      else {
        param
            .severe(
                GenerateError.ErrorGenerate(
                    "VarInitValueId: Error  expected " + varInitValueId.getVarName().getNodeType()
                        + " for variable '" + varInitValueId.getVarName().getName() + "' but found "
                        + varInitValueId.getInitialValue().getExpr().getNodeType(),
                    varInitValueId));
        varInitValueId.setNodeType(ReturnType.UNDEFINED);
      }
    }

    else {
      param.severe(GenerateError.ErrorGenerate(
          "Error in VarInitValueId: undefined parameters",
          varInitValueId));
    }
    }
    return varInitValueId.getNodeType();
  }

  @Override
  public ReturnType visit(VarDeclaration varDeclaration, Logger param) {
    
    varDeclaration.getTypeNode().accept(this, param);
    varDeclaration.getVariables().forEach(v -> {
      int variableAddress = this.symbolTable.findAddr(v.getVarName().getName());
      if (this.symbolTable.prob(variableAddress))
        v.setNodeType(this.symbolTable.getCurrentScope().get(variableAddress).getReturnType());
      else
        v.setNodeType(ReturnType.UNDEFINED);
    });
    if (this.allUndefined(varDeclaration.getVariables())
        && this.isUndefined(varDeclaration.getTypeNode())) {
      ReturnType varType = varDeclaration.getTypeNode().getNodeType();
      varDeclaration.setNodeType(varType);
      varDeclaration.getVariables().forEach(v -> {
        int tmpAddress = this.symbolTable.findAddr(v.getVarName().getName());
        this.symbolTable.add(tmpAddress, new Variable(varType));
      });
    } else {
      param.severe(GenerateError.ErrorGenerate("Error VariableDeclaration", varDeclaration));
      varDeclaration.setNodeType(ReturnType.UNDEFINED);
    }
    varDeclaration.getVariables().forEach(v -> v.accept(this, param));
    return varDeclaration.getNodeType();
  }

  @Override
  public ReturnType visit(ParDeclsNode parDeclsNode, Logger param) {
    parDeclsNode.getParType().accept(this, param);
    parDeclsNode.getType().accept(this, param);
    parDeclsNode.getVarName().accept(this, param);
    if (isUndefined(parDeclsNode.getParType()) && isUndefined(parDeclsNode.getType())) {
      parDeclsNode.setNodeType(parDeclsNode.getType().getNodeType());
    } else {
      parDeclsNode.setNodeType(ReturnType.UNDEFINED);
      param.severe(GenerateError.ErrorGenerate("Error ParDeclsNode", parDeclsNode));
    }
    return parDeclsNode.getNodeType();
  }

  @Override
  public ReturnType visit(ParType parType, Logger param) {
    if (parType.getParType().equals("in") || parType.getParType().equals("out")
        || parType.getParType().equals("inout")) {
      parType.setNodeType(ReturnType.getEnumFor(parType.getParType()));
    } else {
      param.severe(GenerateError.ErrorGenerate("ParType: invalid parType", parType));
      parType.setNodeType(ReturnType.UNDEFINED);
    }
    return parType.getNodeType();
  }

  @Override
  public ReturnType visit(VarDecls varDecls, Logger param) {
    varDecls.getVarsDeclarations().forEach(v -> v.accept(this, param));
    if (checkAll(varDecls.getVarsDeclarations())) {
      varDecls.getVarsDeclarations().forEach(v -> {
        int varAddr = this.symbolTable.findAddr(v.getVariables().get(0).getVarName().getName());
        SemanticSymbol s = new Variable(v.getVariables().get(0).getVarName().getReturnType());
        this.symbolTable.add(varAddr, s);
      });
      varDecls.setNodeType(ReturnType.VOID);
    } else {
      param.severe(GenerateError.ErrorGenerate("Error in VarDecls", varDecls));
      varDecls.setNodeType(ReturnType.UNDEFINED);
    }
    return varDecls.getNodeType();
  }

  @Override
  public ReturnType visit(DefFunctionWithParamsOperation defFunctionWithParamsOperation,
      Logger param) {
    defFunctionWithParamsOperation.getFunctionName().accept(this, param);
    if (!isUndefined(defFunctionWithParamsOperation.getFunctionName())) {
      int addrFunction =
          this.symbolTable.findAddr(defFunctionWithParamsOperation.getFunctionName().getName());
      FunctionSymbol fs = new FunctionSymbol(ReturnType.VOID, "undefined", "undefined");
      this.symbolTable.add(addrFunction, fs);
      this.symbolTable.enterScope();
      defFunctionWithParamsOperation.getdefListParams().forEach(p -> p.accept(this, param));
      defFunctionWithParamsOperation.getdefListParams().forEach(p -> {
        int addr = this.symbolTable.findAddr(p.getVarName().getName());
        Variable var = new Variable(p.getReturnType());
        this.symbolTable.add(addr, var);
      });
      defFunctionWithParamsOperation.getBody().accept(this, param);
      if (checkAll(defFunctionWithParamsOperation.getdefListParams())
          && isUndefined(defFunctionWithParamsOperation.getBody())) {
        defFunctionWithParamsOperation.setNodeType(ReturnType.VOID);
        fs.setInputDom(defFunctionWithParamsOperation.getDomain());
      } else {
        defFunctionWithParamsOperation.setNodeType(ReturnType.UNDEFINED);
        param.severe(GenerateError.ErrorGenerate(
            "DefFunctionWithParamsOperation: invalid parameters", defFunctionWithParamsOperation));
      }
      defFunctionWithParamsOperation.attachScope(this.symbolTable.getCurrentScope());
      this.symbolTable.exitScope();
    } else {
      defFunctionWithParamsOperation.setNodeType(ReturnType.UNDEFINED);
      param.severe(
          GenerateError.ErrorGenerate("DefFunctionWithParamsOperation: function already declared",
              defFunctionWithParamsOperation));
    }
    return defFunctionWithParamsOperation.getNodeType();
  }

  @Override
  public ReturnType visit(BodyNode body, Logger param) {
    body.getVarDecls().forEach(v -> v.accept(this, param));
    body.getStatementsNode().forEach(s -> s.accept(this, param));
    if (this.checkAll(body.getVarDecls()) && this.checkAll(body.getStatementsNode())) {
      body.setNodeType(ReturnType.VOID);
    } else {
      param.severe(GenerateError.ErrorGenerate("BodyNode: invalid body", body));
      body.setNodeType(ReturnType.UNDEFINED);
    }
    return body.getNodeType();
  }

  @Override
  public ReturnType visit(DefFunctionWithoutParamsOperation defFunctionWithoutParamsOperation,
      Logger param) {
    defFunctionWithoutParamsOperation.getFunctionName().accept(this, param);
    if (!isUndefined(defFunctionWithoutParamsOperation.getFunctionName())) {
      int functionAddress =
          this.symbolTable.findAddr(defFunctionWithoutParamsOperation.getFunctionName().getName());
      FunctionSymbol fs = new FunctionSymbol(ReturnType.VOID, "undefined", "undefined");
      this.symbolTable.add(functionAddress, fs);
      this.symbolTable.enterScope();
      defFunctionWithoutParamsOperation.getBody().accept(this, param);
      if (isUndefined(defFunctionWithoutParamsOperation.getBody())) {
        fs.setInputDom(defFunctionWithoutParamsOperation.getDomain());
        defFunctionWithoutParamsOperation.setNodeType(ReturnType.VOID);
      } else {
        defFunctionWithoutParamsOperation.setNodeType(ReturnType.UNDEFINED);
        param.severe(
            GenerateError.ErrorGenerate("DefFunctionWithoutParamsOperation: invalid parameters",
                defFunctionWithoutParamsOperation));
      }
      defFunctionWithoutParamsOperation.attachScope(this.symbolTable.getCurrentScope());
      this.symbolTable.exitScope();
    } else {
      defFunctionWithoutParamsOperation.setNodeType(ReturnType.UNDEFINED);
      param.severe(GenerateError.ErrorGenerate(
          "DefFunctionWithoutParamsOperation: function already declared",
          defFunctionWithoutParamsOperation));
    }
    return defFunctionWithoutParamsOperation.getNodeType();
  }

  @Override
  public ReturnType visit(CompStat compStat, Logger param) {
    compStat.getStatementsNode().forEach(s -> s.accept(this, param));
    if (checkAll(compStat.getStatementsNode())) {
      compStat.setNodeType(ReturnType.VOID);
    } else {
      compStat.setNodeType(ReturnType.UNDEFINED);
      param.severe(GenerateError.ErrorGenerate("Error CompStat", compStat));
    }
    return compStat.getNodeType();
  }

  private boolean checkAll(List<? extends YasplNode> list) {
    return list.stream().allMatch(node -> node.getNodeType() != ReturnType.UNDEFINED);
  }

  private boolean allUndefined(List<? extends YasplNode> list) {
    return list.stream().allMatch(node -> node.getNodeType() == ReturnType.UNDEFINED);
  }

  private boolean isUndefined(YasplNode node) {
    return node.getNodeType() != ReturnType.UNDEFINED;
  }

  private boolean checkReadType(List<? extends YasplNode> node) {
    return node.stream().allMatch(
        n -> n.getNodeType() != ReturnType.STRING && n.getNodeType() != ReturnType.BOOLEAN);
  }

}