public class IntegerType extends Type {
  public IntegerType() {
    super("integer");
  }

  public boolean isNumeric() {
    return true;
  }

  public boolean conformsTo(Type other) {
    return super.conformsTo(other) || other.name().equals("real");
  }

}
