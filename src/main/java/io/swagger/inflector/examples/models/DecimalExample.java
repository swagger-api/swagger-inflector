package io.swagger.inflector.examples.models;

import java.math.BigDecimal;

public class DecimalExample extends AbstractExample {
  private BigDecimal value;

  public DecimalExample(BigDecimal value) {
    this.value = value;
  }
  
  public String asString() {
    if(value == null) {
      return null;
    }
    return value.toPlainString();
  }

  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }
}
