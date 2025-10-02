import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STGroupCompiletimeMessage;

import statement.*;
import java.io.File;

@SuppressWarnings("CheckReturnValue")
public class CSHCompiler extends CSHBaseVisitor<ST> {

  private final STGroup stg = new STGroupFile("CSH.stg");
  // Type Value
  private final ProgramValue progValue = new ProgramValue();
  private final TextValue textValue = new TextValue();
  private final RealValue realValue = new RealValue();
  private final IntegerValue intValue = new IntegerValue();
  // types
  private final TextType textType = new TextType();
  private final ProgramType programType = new ProgramType();
  private final IntegerType integerType = new IntegerType();
  private final RealType realType = new RealType();
  // statement types
  private final InputType inputType = new InputType();
  private final OutputType outputType = new OutputType();
  private final VarType varType = new VarType();
  private final FunctionType functionType = new FunctionType();
  private final ExprType exprType = new ExprType();
  private final ExecuteType executeType = new ExecuteType();
  private final StoreType storeType = new StoreType();
  private final StringType stringType = new StringType();
  private HashMap<String, Value> varTable = new HashMap<String, Value>();

  @Override
  public ST visitMain(CSHParser.MainContext ctx) {
    ST res = stg.getInstanceOf("main");
    List<ST> lines = new ArrayList<>();
    for (CSHParser.LineStatContext lineCtx : ctx.lineStat()) {
      lines.add(visit(lineCtx));
    }
    res.add("className", "Output"); // maybe pyt the name of the test file being used
    res.add("lines", lines);
    System.out.println(res.render());
    return res;
  }

  @Override
  public ST visitPipeList(CSHParser.PipeListContext ctx) {
    ST res = null;
    return visitChildren(ctx);
    // return res;
  }

  @Override
  public ST visitVariableInit(CSHParser.VariableInitContext ctx) {
    ST res = null;
    return visitChildren(ctx);
    // return res;
  }

  @Override
  public ST visitStatList(CSHParser.StatListContext ctx) {
    ST res = stg.getInstanceOf("statement");

    // Visit
    ST statLeft = visit(ctx.stat(0));
    ST statRight = visit(ctx.stat(1));

    if (ctx.stat(0).statement.getType() instanceof InputType) {
      statLeft = stg.getInstanceOf("stdin");
      statLeft.add("type", ctx.stat(1).statement.getValueType().name());
    }


    // Start from right (Only output and store)
    if (ctx.stat(1).statement.getType() instanceof OutputType) {
      statRight.add("content", statLeft);
      res.add("stat", statRight);
    } else if (ctx.stat(1).statement.getType() instanceof StoreType || 
      (ctx.stat(1).statement.getType() instanceof ExprType && ctx.stat(1).statement.getVarName() != null)) {
      ST var = null;
      if (ctx.stat(0).statement.getType() instanceof InputType && ctx.stat(0).statement.getQuestion() != null) {
        ST stdout = stg.getInstanceOf("stdout");
        stdout.add("content", ctx.stat(0).statement.getQuestion());
        res.add("stat", stdout);  
      }

      if (ctx.stat(1).statement.getValueType() != null && !(ctx.stat(1).statement.getType() instanceof ExprType)) {
        var = stg.getInstanceOf("variableInitWithValue");
        var.add("type", ctx.stat(1).statement.getValueType().name);
        var.add("var", ctx.stat(1).statement.getVarName());
        var.add("stat", statLeft);
      } else {
        var = stg.getInstanceOf("variableSet");
        var.add("var", ctx.stat(1).statement.getVarName());
        var.add("stat", statLeft);
      }
      res.add("stat", var);
    }
    
    return res;
  }

  @Override
  public ST visitStatStdin(CSHParser.StatStdinContext ctx) {
    ctx.statement = new Statement("input", inputType);
    
    if (ctx.STRINGVAL() != null)
      ctx.statement.setQuestion(ctx.STRINGVAL().getText());

    return visitChildren(ctx);
  }

  @Override
  public ST visitStatExpr(CSHParser.StatExprContext ctx) {
    ST res = stg.getInstanceOf("plain");
    res.add("stat", visitChildren(ctx));
    ctx.statement = new Statement("expr", exprType);
    ctx.statement.setValueType(ctx.expr().eType);
    ctx.statement.setVarName(ctx.expr().varName);

    return res;
  }

  @Override
  public ST visitStatExecuteExpression(CSHParser.StatExecuteExpressionContext ctx) {
    //System.out.println("Execute");
    ctx.statement = new Statement("execute", executeType);

    return visitChildren(ctx);
  }

  @Override
  public ST visitStatInternalFunctions(CSHParser.StatInternalFunctionsContext ctx) {
    //System.out.println("Internal execute");
    ctx.statement = new Statement("internal", executeType);

    return visitChildren(ctx);
  }

  @Override
  public ST visitStatStdout(CSHParser.StatStdoutContext ctx) {
    visitChildren(ctx);

    ST res = stg.getInstanceOf("stdout");
    ctx.statement = new Statement("output", outputType);

    return res;
  }

  @Override
  public ST visitStatStderr(CSHParser.StatStderrContext ctx) {
    //System.out.println("Sterr");
    visitChildren(ctx);

    ST res = stg.getInstanceOf("stderr");
    ctx.statement = new Statement("error", outputType);

    return res;
  }

  @Override
  public ST visitStatStore(CSHParser.StatStoreContext ctx) {
    //System.out.println("Store");
    
    ST res = visitChildren(ctx);
    ctx.statement = new Statement("store", storeType);
    ctx.statement.setVarName(ctx.store().varName);
    ctx.statement.setValueType(ctx.store().eType);

    return res;
  }

  @Override
  public ST visitExprInt(CSHParser.ExprIntContext ctx) {
    visitChildren(ctx);

    ST res = stg.getInstanceOf("plain");
    res.add("stat", ctx.INT().getText());    
    ctx.eType = integerType;

    return res;
  }

  @Override
  public ST visitExprAddSub(CSHParser.ExprAddSubContext ctx) {
    visitChildren(ctx);

    ST res = stg.getInstanceOf("expr3");
    res.add("e1", ctx.e1.getText());
    res.add("op", ctx.op.getText());
    res.add("e2", ctx.e2.getText());
    ctx.eType = ctx.e1.eType;

    return res;
  }

  @Override
  public ST visitExprParent(CSHParser.ExprParentContext ctx) {
    visitChildren(ctx);

    ST res = stg.getInstanceOf("parentheses");
    res.add("stat", ctx.e.getText());    
    ctx.eType = ctx.e.eType;

    return res;
  }

  @Override
  public ST visitExprStr(CSHParser.ExprStrContext ctx) {
    visitChildren(ctx);

    ST res = stg.getInstanceOf("plain");
    res.add("stat", ctx.STRINGVAL().getText());    
    ctx.eType = textType;

    return res;
  }

  @Override
  public ST visitExprVar(CSHParser.ExprVarContext ctx) {
    visitChildren(ctx);

    ST res = stg.getInstanceOf("plain");
    String varName = ctx.VAR().getText();
    res.add("stat", varName);

    ctx.eType = CSHParser.symbolTable.get(varName);
    ctx.varName = varName;

    return res;
  }

  @Override
  public ST visitExprNegPos(CSHParser.ExprNegPosContext ctx) {
    visitChildren(ctx);

    ST res = stg.getInstanceOf("expr2");
    res.add("stat", ctx.e.getText());
    res.add("signal", ctx.op.getText());    
    ctx.eType = ctx.e.eType;

    return res;
  }

  @Override
  public ST visitExprReal(CSHParser.ExprRealContext ctx) {
    visitChildren(ctx);

    ST res = stg.getInstanceOf("plain");
    res.add("stat", ctx.REAL().getText());    
    ctx.eType = realType;

    return res;
  }

  @Override
  public ST visitExprConversion(CSHParser.ExprConversionContext ctx) {
    ST res = visitChildren(ctx);

    // TODO:

    return res;
  }

  @Override
  public ST visitExprMultDivMod(CSHParser.ExprMultDivModContext ctx) {
    visitChildren(ctx);

    ST res = stg.getInstanceOf("expr3");
    res.add("e1", ctx.e1.getText());
    res.add("op", ctx.op.getText());
    res.add("e2", ctx.e2.getText());
    ctx.eType = ctx.e1.eType;

    return res;
  }

  @Override
  public ST visitInternalFunctionsStat(CSHParser.InternalFunctionsStatContext ctx) {
    visitChildren(ctx);
    ST res = stg.getInstanceOf("NL");

    return res;
  }

  @Override
  public ST visitVariableInitializer(CSHParser.VariableInitializerContext ctx) {
    visitChildren(ctx);
    ST res = stg.getInstanceOf("variableInit");
    ctx.varName = ctx.VAR().getText();
    Value value = null;
    Type type = null;
    switch (ctx.varType().getText()) {
      case "text":
        value = textValue;
        type = textType;
        break;
      case "program":
        value = progValue;
        type = programType;
        break;
      case "integer":
        value = intValue;
        type = integerType;
        break;
      case "real":
        value = realValue;
        type = realType;
        break;
    }

    res.add("var", ctx.varName);
    res.add("type", value.type().name());
    varTable.put(ctx.varName, value);
    CSHParser.symbolTable.put(ctx.varName, type);
    //System.out.println("init var:" + ctx.varName + " of type" + value.getClass().toString());

    return res;
  }

  @Override
  public ST visitVarType(CSHParser.VarTypeContext ctx) {
    ST res = null;
    return visitChildren(ctx);
    // return res;
  }

  @Override
  public ST visitExecuteStringVal(CSHParser.ExecuteStringValContext ctx) {
    visitChildren(ctx);
    ctx.cmd = ctx.command.getText();
    ST res = stg.getInstanceOf("execute");
    res.add("content", ctx.cmd);

    return res;
  }

  @Override
  public ST visitExcuteSecondGrammar(CSHParser.ExcuteSecondGrammarContext ctx) {
    visitChildren(ctx);
    ST res = stg.getInstanceOf("executeIsh");
    res.add("filename", ctx.STRINGVAL());
    return res;
  }

  @Override
  public ST visitStoreVar(CSHParser.StoreVarContext ctx) {
    if (ctx.VAR() != null) {
      ctx.varName = ctx.VAR().getText();
      ctx.eType = null;
    } else {
      ctx.varName = ctx.variableInitializer().varName;
      ctx.eType = ctx.variableInitializer().eType;
    }

    return visitChildren(ctx);
  }

  @Override public ST visitLoopExpr(CSHParser.LoopExprContext ctx) { 
    ST res = stg.getInstanceOf("plain");
    res.add("stat", visitChildren(ctx));

    return res;
  }

  @Override public ST visitLoopTail(CSHParser.LoopTailContext ctx) { 
    ST res = stg.getInstanceOf("whileTail");

    for (CSHParser.LineStatContext lineStatCtx : ctx.lineStat()) {
      res.add("stats", visit(lineStatCtx));
    }

    res.add("condition", visit(ctx.condition()));

    return res;
  }
	
	@Override public ST visitLoopHead(CSHParser.LoopHeadContext ctx) { 
    ST res = stg.getInstanceOf("whileHead");

    for (CSHParser.LineStatContext lineStatCtx : ctx.lineStat()) {
      res.add("stats", visit(lineStatCtx));
    }

    res.add("condition", visit(ctx.condition()));

    return res;
  }
	
	@Override public ST visitLoopMiddle(CSHParser.LoopMiddleContext ctx) { 
    ST res = stg.getInstanceOf("whileMiddle");

    for (CSHParser.LineStatContext lineStatCtx : ctx.lineStat()) {
      res.add("stats1", visit(lineStatCtx));
    }

    for (CSHParser.LineStat2Context lineStatCtx : ctx.lineStat2()) {
      res.add("stats2", visit(lineStatCtx));
    }

    res.add("condition", visit(ctx.condition()));

    return res;
  }

  @Override public ST visitCondition(CSHParser.ConditionContext ctx) { 
    ST res = null;
    ST op = visit(ctx.op);

    if (Arrays.asList("==", "!=").contains(ctx.op.op) && ctx.e1.eType instanceof TextType) {
      String not = (ctx.op.op == "!=") ? "!" : "";
      res = stg.getInstanceOf("equals");

      res.add("not", not);
      res.add("e1", visit(ctx.e1));
      res.add("e2", visit(ctx.e2));  
    } else {
      res = stg.getInstanceOf("expr3");
      res.add("e1", visit(ctx.e1));
      res.add("op", op);
      res.add("e2", visit(ctx.e2));
    }

    return res;
  }

  @Override public ST visitOperator(CSHParser.OperatorContext ctx) { 
    ST res = stg.getInstanceOf("plain");
    res.add("stat", ctx.op);

    return res;
  }

  @Override public ST visitDecisionExpr(CSHParser.DecisionExprContext ctx) { 
    return visitChildren(ctx); 
  }

  @Override public ST visitDecisionSimple(CSHParser.DecisionSimpleContext ctx) { 
    ST res = stg.getInstanceOf("decisionIf");

    for (CSHParser.LineStatContext lineStatCtx : ctx.lineStat()) {
      res.add("stats", visit(lineStatCtx));
    }

    res.add("condition", visit(ctx.condition()));

    return res;
  }
	
	@Override public ST visitDecisionWithElse(CSHParser.DecisionWithElseContext ctx) { 
    ST res = stg.getInstanceOf("decisionIfElse");

    for (CSHParser.LineStatContext lineStatCtx : ctx.lineStat()) {
      res.add("stats1", visit(lineStatCtx));
    }

    for (CSHParser.LineStat2Context lineStatCtx : ctx.lineStat2()) {
      res.add("stats2", visit(lineStatCtx));
    }

    res.add("condition", visit(ctx.condition()));

    return res;
  }

  protected String newVarName() {
    varCount++;
    return "v" + varCount;
  }

  protected int varCount = 0;
}
