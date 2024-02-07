package kyo.yaz.condominium.manager.ui.views.util;

public class AppUtil {

  public static final String DFLT_EMAIL_SUBJECT = "AVISO DE COBRO";

  public static Runnable emptyRunnable() {
    return () -> {
    };
  }

  public static boolean isNumeric(String strNum) {
    if (strNum == null) {
      return false;
    }
    try {
      double d = Double.parseDouble(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }
}
