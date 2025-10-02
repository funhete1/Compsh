public class TextValue extends Value {
  public TextValue() {
    val = null;
  }

  public TextValue(String res) {
    val = res;
  }

  @Override
  public Type type() {
    return type;
  }

  @Override
  public void setStringValue(String val) {
    this.val = val;
  }

  @Override
  public String stringValue() {
    return val;
  }

  @Override
  public String toString() {
    return "" + val;
  }

  private String val;

  private static TextType type = new TextType();
}
