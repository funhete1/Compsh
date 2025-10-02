import static java.lang.System.*;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.stringtemplate.v4.*;

public class CSHMain {
   public static void main(String[] args) {
      runCompiler(args[0]);
   }
   public static void runCompiler(String sourceFile)
   {
      assert sourceFile != null && !sourceFile.isEmpty();

      try
      {
         // create a CharStream that reads from standard input:
         CharStream input = CharStreams.fromStream(new FileInputStream(sourceFile));
         // create a lexer that feeds off of input CharStream:
         CSHLexer lexer = new CSHLexer(input);
         // create a buffer of tokens pulled from the lexer:
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         // create a parser that feeds off the tokens buffer:
         CSHParser parser = new CSHParser(tokens);
         // begin parsing at program rule:
         ParseTree tree = parser.main();
         if (parser.getNumberOfSyntaxErrors() == 0) {
            // print LISP-style tree:
            // System.out.println(tree.toStringTree(parser));
            CSHSemanticCheck semanticCheck = new CSHSemanticCheck();
            CSHCompiler compiler = new CSHCompiler();
            semanticCheck.visit(tree);
            if (!ErrorHandling.error())
            {
               ST code = compiler.visit(tree);
               String filename = "Output.java";
               try
               {
                  PrintWriter pw = new PrintWriter(new File(filename));
                  pw.print(code.render());
                  pw.close();
               }
               catch(IOException e)
               {
                  err.println("ERROR: unable to write in file "+filename);
                  exit(3);
               }
            }
         }
      }
      catch(IOException e)
      {
         err.println("ERROR: unable to read from file "+sourceFile);
         exit(4);
      }
   }

}