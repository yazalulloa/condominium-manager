package kyo.yaz.condominium.manager.ui.views.util;

public class AppUtil {

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
