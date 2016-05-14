package com.github.gv2011.hprops;

public final class Property {

  private final String key;
  private final String value;

  public Property(final String key, final String value) {
    super();
    this.key = key;
    this.value = value;
  }

  public String key() {
    return key;
  }

  public String value() {
    return value;
  }


}
