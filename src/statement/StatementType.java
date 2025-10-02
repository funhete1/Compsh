package statement;

public abstract class StatementType {
  protected StatementType(String name) {
    assert name != null;
    this.name = name;
  }

  public String name() {
    return name;
  }

  protected final String name;
}
