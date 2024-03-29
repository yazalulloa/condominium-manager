package kyo.yaz.condominium.manager.core.domain;

public enum ReceiptEmailFrom {

  KYOTAIDOSHIN("kyotaidoshin@gmail.com"), RODRIGUEZULLOA15("rodriguezulloa15@gmail.com");

  public static final ReceiptEmailFrom[] values = values();
  private final String email;

  ReceiptEmailFrom(String email) {
    this.email = email;
  }

  public String email() {
    return email;
  }
}
