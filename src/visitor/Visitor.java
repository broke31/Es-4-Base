package visitor;

import binaryOperationExpression.ArithOperation;
import binaryOperationExpression.BooleanExpession;
import binaryOperationExpression.RelopExpression;
import otherOperationExpression.Char_Const;
import otherOperationExpression.Double_Const;
import otherOperationExpression.FalseExpression;
import otherOperationExpression.IdentiferExpression;
import otherOperationExpression.Int_Const;
import otherOperationExpression.MinusExpression;
import otherOperationExpression.NotExpression;
import otherOperationExpression.String_Const;
import otherOperationExpression.TrueExpression;
import statOperation.Args;
import statOperation.AssignOperation;
import statOperation.CallOpParamOperation;
import statOperation.CallWithoutParam;
import statOperation.CompStat;
import statOperation.IfThenElseOperation;
import statOperation.IfThenOperation;
import statOperation.ReadOperation;
import statOperation.Vars;
import statOperation.WhileNode;
import statOperation.WriteOperation;
import syntax.BodyNode;
import syntax.DefDecl;
import syntax.DefFunctionWithParam;
import syntax.DefFunctionWithoutParams;
import syntax.ListParams;
import syntax.ParDeclsNode;
import syntax.ParType;
import syntax.Program;
import syntax.TypeNode;
import syntax.VarDeclInit;
import syntax.VarDeclaration;
import syntax.VarDecls;
import syntax.VarInitValue;
import syntax.VarInitValueId;

public interface Visitor <T,P> {

  T visit(ArithOperation arithOperation, P param) throws RuntimeException;

  T visit(BooleanExpession booleanExpession, P param) throws RuntimeException;

  T visit(RelopExpression relopExpression, P param);

  T visit(MinusExpression minus, P param);

  T visit(NotExpression notOp, P param);

  T visit(TrueExpression trueExpression, P param);

  T visit(FalseExpression falseExpression, P param);

  T visit(IdentiferExpression identiferExpression, P param);

  T visit(Int_Const intConst, P param);

  T visit(Double_Const double_Const, P param);

  T visit(Char_Const char_Const, P param);

  T visit(String_Const string_Const, P param);

  T visit(WhileNode whileNode, P param);

  T visit(IfThenOperation ifThenOperation, P param);

  T visit(IfThenElseOperation ifThenElseOperation, P param);

  T visit(ReadOperation readOperation, P param);

  T visit(Vars vars, P param);

  T visit(Args args, P param);

  T visit(WriteOperation writeOperation, P param);

  T visit(AssignOperation assignOperation, P param);

  T visit(CallOpParamOperation callOpParamOperation, P param);

  T visit(CallWithoutParam callWithoutParam, P param);

  T visit(Program program, P param);

  T visit(TypeNode type, P param);

  T visit(VarInitValue varInitValue, P param);

  T visit(VarDeclInit varDeclInit, P param);

  T visit(VarInitValueId varInitValueId, P param);

  T visit(VarDeclaration varDeclaration, P param);

  T visit(ListParams listParams, P param);

  T visit(ParDeclsNode parDecls, P param);

  T visit(ParType parType, P param);

  T visit(VarDecls varDecls, P param);

  T visit(DefFunctionWithParam defFunctionWithParam, P param);

  T visit(BodyNode body, P param);

  T visit(DefFunctionWithoutParams defFunctionWithoutParams, P param);

  T visit(CompStat compStat, P param);


  
}
