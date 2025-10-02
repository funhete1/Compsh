import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class ProgramValue extends Value {
  public ProgramValue() {
    this.commands = new LinkedList<String>();
    expressionOutput = "";
    standardOutput = "";
    standardError = "";
    exitValue = 0;
  }

  @Override
  public Type type() {
    return type;
  }

  public int getNumCommands() {
    return commands.size();
  }

  public String getCommand() {
    try {
      if (commands.size() == 0)
        throw new IOException("No commands Inserted");
      return commands.poll();
    } catch (IOException e) {
      UpdateErrChannels("No commands Inserted yet", 1);
      return this.toString();
    }
  }

  public String storeCommand(String command) {
    try {
      if (command == null)
        throw new IllegalArgumentException("Can't store null commnad");
      commands.add(command);
      return null;
    } catch (IllegalArgumentException e) {
      UpdateErrChannels("Can't store null command", 1);
      return this.toString();
    }
  }

  public void UpdateErrChannels(String errCh, int exitVal) {
    standardError = errCh;
    exitValue = exitVal;
  }

  public void stdoutChannel(String stdout) {
    expressionOutput = stdout;
  }

  @Override
  public String toString() {
    String res;
    if (expressionOutput.equals("")) {
      res = "ProgramResult{" +
          ", standardOutput:" + standardOutput + "\n" +
          ", standardErro:'" + standardError + "\n" +
          ", exitValue:" + exitValue +
          '}';
    } else {
      res = "ProgramResult{" +
          "expressionResult: " + expressionOutput + "\n" +
          ", standardOutput: " + standardOutput + "\n" +
          ", standardErro: " + standardError + "\n" +
          ", exitValue: " + exitValue +
          '}';
    }
    return res;
  }

  // the Exppression channel maybe should be a class since it can hold multiple
  // types inside of(maybe with a class it could have sub channels associated)
  protected Queue<String> commands;
  private static ProgramType type = new ProgramType();
  private String expressionOutput; // Corresponds to '$' - general expression output (text, integer, real, etc.)
  private String standardOutput; // Corresponds to '!' - standard output (text)
  private String standardError; // Corresponds to '&' - standard error (text)
  private int exitValue; // Corresponds to '?' - exit value (integer)
}
