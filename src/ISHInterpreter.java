import java.util.*;

public class ISHInterpreter extends ISHBaseVisitor<Object> {

    Map<String, Integer> memory = new HashMap<>();

    @Override
    public Object visitProg(ISHParser.ProgContext ctx) {
        Object res = visitChildren(ctx);
        return res;
    }

    @Override 
    public Object visitStatement(ISHParser.StatementContext ctx) { 
        return visitChildren(ctx); 
    }

    @Override
    public Object visitAssignStat(ISHParser.AssignStatContext ctx) {
        // Obter mensagem do input
        String prompt = ctx.inputExpr().STRING().getText();
        prompt = prompt.substring(1, prompt.length() - 1); // remover aspas

        System.out.print(prompt);
        Scanner sc = new Scanner(System.in);
        int value = sc.nextInt();

        // Obter nome da variável
        String id = ctx.storeExpr().ID().getText();
        memory.put(id, value);
        return null;
    }

    @Override
    public Object visitOutStat(ISHParser.OutStatContext ctx) {
        Object result = visit(ctx.expr());
        System.out.println(result);

        return result;
    }

    @Override
    public Object visitAddSubExpr(ISHParser.AddSubExprContext ctx) {
        int left = (int) visit(ctx.exprAddSub());
        int right = (int) visit(ctx.exprAtom());
        String op = ctx.op.getText();

        return op.equals("+") ? left + right : left - right;
    }

    @Override
    public Object visitToAtomFromAdd(ISHParser.ToAtomFromAddContext ctx) {
        return visit(ctx.exprAtom());
    }

    @Override
    public Object visitInt(ISHParser.IntContext ctx) {
        return Integer.parseInt(ctx.INT().getText());
    }

    @Override
    public Object visitVar(ISHParser.VarContext ctx) {
        String id = ctx.ID().getText();
        return memory.getOrDefault(id, 0);
    }

    @Override
    public Object visitParens(ISHParser.ParensContext ctx) {
        return visit(ctx.exprAddSub());
    }
}
