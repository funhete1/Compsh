import statement.*;
import org.stringtemplate.v4.ST;

public class Statement {
  public Statement(String name, StatementType type) {
    assert name != null;
    assert type != null;

    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public StatementType getType() {
    return type;
  }

  public Value getValue() {
    return value;
  }

  public void setValue(Value value) {
    assert value != null;

    this.value = value;
  }

  public Type getValueType() {
    return valueType;
  }

  public void setValueType(Type valueType) {
    this.valueType = valueType;
  }

  public String getVarName() {
    return varName;
  }

  public void setVarName(String varName) {
    this.varName = varName;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  private final String name;
  private final StatementType type;
  private Value value = null;
  private Type valueType = null;
  private String varName = null;
  private String question = null;
}
