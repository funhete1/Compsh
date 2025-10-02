public abstract class Type {
  protected Type(String name) {
    assert name != null;
    this.name = name;
  }

  public String name() {
    return name;
  }

  public boolean conformsTo(Type... others) {
    for (Type type : others) {
      if (name.equals(type.name())) {
        return true;
      }
    }
    return false;
  }

  public boolean isNumeric() {
    return false;
  }

  protected final String name;
}
