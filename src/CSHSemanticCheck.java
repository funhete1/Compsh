import org.antlr.v4.runtime.ParserRuleContext;
import java.util.ArrayList;
import java.util.List;
import statement.*;

public class CSHSemanticCheck extends CSHBaseVisitor<Boolean> {
  private final TextType textType = new TextType();
  private final ProgramType programType = new ProgramType();
  private final IntegerType integerType = new IntegerType();
  private final RealType realType = new RealType();

  @Override
  public Boolean visitStatList(CSHParser.StatListContext ctx) {
    Boolean res = true;

    List<CSHParser.StatContext> allStats = new ArrayList<>();

    for (int i = 0; i < ctx.stat().size(); i++) {
      res &= visit(ctx.stat(i));
      allStats.add(ctx.stat(i));
    }

    // O ultimo stat tem de ser sempre stdout, sderr, store
    // Quando store verficar se expr anterior é do tipo da var do store

    CSHParser.StatContext lastStat = allStats.get(allStats.size() - 1);
    if (!(lastStat.statement.getType() instanceof OutputType 
      || lastStat.statement.getType() instanceof StoreType
      || lastStat.statement.getType() instanceof ExprType && lastStat.statement.getVarName() != null)) {
      ErrorHandling.printError(ctx, "Last statement must be stdout, sderr, or store!");
      res = false;
    }

    if (lastStat.statement.getType() instanceof StoreType) {
      if (allStats.size() < 2) {
        ErrorHandling.printError(ctx, "'store' must receive a value from a pipe.");
        return false;
      }
      CSHParser.StatContext previousStat = allStats.get(allStats.size() - 2);

      if (previousStat.statement == null || previousStat.statement.getValueType() == null) {
        ErrorHandling.printError(ctx, "Cannot store: previous statement has no output value.");
        return false;
      }

      if (previousStat.statement.getType() instanceof InputType) {
        // allow types: text, integer and real
        if (previousStat.statement.getValueType() instanceof ProgramType) {
          ErrorHandling.printError(ctx, "Type mismatch: store variable types do not match!");
          res = false;  
        }
      } else if (!previousStat.statement.getValueType().equals(lastStat.statement.getValueType())) {
        ErrorHandling.printError(ctx, "Type mismatch: store variable types do not match!");
        res = false;
      }
    }

    return res;
  }

  @Override
  public Boolean visitStatStdin(CSHParser.StatStdinContext ctx) {
    visitChildren(ctx);
    ctx.statement = new Statement("stdin", new InputType());
    ctx.statement.setValueType(textType);

    return true;
  }

  @Override
  public Boolean visitStatExpr(CSHParser.StatExprContext ctx) {
    visitChildren(ctx);

    ctx.statement = new Statement("expression", new ExprType());
    ctx.statement.setValueType(ctx.expr().eType);
    ctx.statement.setVarName(ctx.expr().varName);

    return true;
  }

  @Override
  public Boolean visitStatExecuteExpression(CSHParser.StatExecuteExpressionContext ctx) {
    visitChildren(ctx);
    ctx.statement = new Statement("execute", new FunctionType());
    ctx.statement.setValueType(programType);

    return true;
  }

  @Override
  public Boolean visitStatInternalFunctions(CSHParser.StatInternalFunctionsContext ctx) {
    visitChildren(ctx);
    ctx.statement = new Statement("function", new FunctionType());

    return true;
  }

  @Override
  public Boolean visitStatStdout(CSHParser.StatStdoutContext ctx) {
    visitChildren(ctx);
    ctx.statement = new Statement("stdout", new OutputType());
    return true;
  }

  @Override
  public Boolean visitStatStderr(CSHParser.StatStderrContext ctx) {
    visitChildren(ctx);
    ctx.statement = new Statement("stderr", new OutputType());
    return true;
  }

  @Override
  public Boolean visitStatStore(CSHParser.StatStoreContext ctx) {
    visitChildren(ctx);
    ctx.statement = new Statement("store", new StoreType());
    ctx.statement.setVarName(ctx.store().varName);
    ctx.statement.setValueType(ctx.store().eType);

    return true;
  }

  @Override
  public Boolean visitExprInt(CSHParser.ExprIntContext ctx) {
    ctx.eType = integerType;
    return true;
  }

  @Override
  public Boolean visitExprAddSub(CSHParser.ExprAddSubContext ctx) {
    Boolean res = visit(ctx.e1) && visit(ctx.e2);

    String op = ctx.op.getText();
    // Allows concat string with any other type.
    if (op.equals("+") && ctx.e1.eType instanceof TextType || ctx.e2.eType instanceof TextType) {
      ctx.eType = textType;
    } else {
      res &= checkNumericType(ctx, ctx.e1.eType) && checkNumericType(ctx, ctx.e2.eType);
      ctx.eType = (res) ? fetchType(ctx.e1.eType, ctx.e2.eType) : ctx.e1.eType;
    }

    return res;
  }

  @Override
  public Boolean visitExprStr(CSHParser.ExprStrContext ctx) {
    ctx.eType = textType;
    return true;
  }

  @Override
  public Boolean visitExprReal(CSHParser.ExprRealContext ctx) {
    ctx.eType = realType;
    return true;
  }

  @Override
  public Boolean visitExprVar(CSHParser.ExprVarContext ctx) {
    String id = ctx.VAR().getText();

    if (!variableExists(ctx, id)) {
      return false;
    }

    ctx.eType = CSHParser.symbolTable.get(id);
    ctx.varName = id;

    return true;
  }

  @Override
  public Boolean visitExprNegPos(CSHParser.ExprNegPosContext ctx) {
    Boolean res = visit(ctx.e) && checkNumericType(ctx, ctx.e.eType);
    if (res) {
      ctx.eType = ctx.e.eType;
    }

    return res;
  }

  @Override
  public Boolean visitExprMultDivMod(CSHParser.ExprMultDivModContext ctx) {
    Boolean res = visit(ctx.e1) && checkNumericType(ctx, ctx.e1.eType) && visit(ctx.e2)
        && checkNumericType(ctx, ctx.e2.eType);
    if (res) {
      ctx.eType = fetchType(ctx.e1.eType, ctx.e2.eType);
    }

    return res;
  }

  @Override
  public Boolean visitExprParent(CSHParser.ExprParentContext ctx) {
    Boolean res = visit(ctx.e);
    if (res)
      ctx.eType = ctx.e.eType;

    return res;
  }

  @Override
  public Boolean visitExprConversion(CSHParser.ExprConversionContext ctx) {
    Boolean res = visit(ctx.expr());
    if (!res) {
      return false;
    }

    String target = ctx.getChild(0).getText(); // "text", "integer", "real"
    Type inner = ctx.expr().eType;

    switch (target) {
      case "text":
        ctx.eType = textType;
        return true;

      case "integer":
        if (inner instanceof IntegerType || inner instanceof RealType || inner instanceof TextType) {
          ctx.eType = integerType;
          return true;
        }
        break;

      case "real":
        if (inner instanceof RealType || inner instanceof IntegerType || inner instanceof TextType) {
          ctx.eType = realType;
          return true;
        }
        break;
    }

    ErrorHandling.printError(ctx, "Invalid type conversion: " + inner.name() + " to " + target);
    return false;
  }

  @Override
  public Boolean visitVariableInitializer(CSHParser.VariableInitializerContext ctx) {
    String id = ctx.VAR().getText();

    if (CSHParser.symbolTable.containsKey(id)) {
      ErrorHandling.printError(ctx, "variable " + id + " already exists!");
      return null;
    } else {
      Type type = null;
      switch (ctx.varType().getText()) {
        case "text":
          type = textType;
          break;
        case "program":
          type = programType;
          break;
        case "integer":
          type = integerType;
          break;
        case "real":
          type = realType;
          break;
      }
      VariableSymbol symbol = new VariableSymbol(id, type);
      CSHParser.symbolTable.put(symbol.name, symbol.type);
      ctx.eType = type;
      ctx.varName = id;
    }

    return visitChildren(ctx);
  }

  @Override
  public Boolean visitStoreVar(CSHParser.StoreVarContext ctx) {
    visitChildren(ctx);
    String varName = null;

    if (ctx.VAR() != null) {
      varName = ctx.VAR().getText();
    } else if (ctx.variableInitializer() != null) {
      varName = ctx.variableInitializer().VAR().getText();
    }

    if (varName == null) {
      ErrorHandling.printError(ctx, "Store target variable is not defined.");
      return false;
    }

    if (!CSHParser.symbolTable.containsKey(varName)) {
      ErrorHandling.printError(ctx, "Variable '" + varName + "' is not declared.");
      return false;
    }

    ctx.eType = CSHParser.symbolTable.get(varName);

    // Verificar se está dentro de um pipe com valor anterior
    ParserRuleContext stat = (ParserRuleContext) ctx.getParent(); // stat
    ParserRuleContext statList = (ParserRuleContext) stat.getParent(); // statList

    if (statList instanceof CSHParser.StatListContext) {
      CSHParser.StatListContext sl = (CSHParser.StatListContext) statList;
      if (sl.stat().size() == 1) {
        ErrorHandling.printError(ctx, "'store' must receive a value from a pipe.");
        return false;
      }
    }

    return true;
  }

  private Boolean checkNumericType(ParserRuleContext ctx, Type t) {
    Boolean res = true;
    if (!t.isNumeric()) {
      ErrorHandling.printError(ctx, "Numeric operator applied to a non-numeric operand!");
      res = false;
    }
    return res;
  }

  private Type fetchType(Type t1, Type t2) {
    Type res = null;
    if (t1.isNumeric() && t2.isNumeric()) {
      if (t1 instanceof RealType || t2 instanceof RealType) {
        res = t1;
      } else {
        res = t1;
      }
    } else {
      res = t1;
    }

    return res;
  }

  private Boolean variableExists(ParserRuleContext ctx, String id) {
    if (!CSHParser.symbolTable.containsKey(id)) {
      ErrorHandling.printError(ctx, "variable " + id + " not declared!");
      return false;
    }

    return true;
  }

}
