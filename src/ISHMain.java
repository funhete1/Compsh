import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;


public class ISHMain {
    public static void main(String[] args) throws Exception {
        String filename = args[0];
        CharStream input = CharStreams.fromFileName(filename);
        ISHLexer lexer = new ISHLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ISHParser parser = new ISHParser(tokens);
        ParseTree tree = parser.prog();

        ISHInterpreter interpreter = new ISHInterpreter();
        interpreter.visit(tree);
    }
}