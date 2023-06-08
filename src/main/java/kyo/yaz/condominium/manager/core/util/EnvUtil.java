package kyo.yaz.condominium.manager.core.util;

import java.util.Optional;

public class EnvUtil {

  private EnvUtil() {
  }

  public static boolean isShowDir() {
    return Optional.ofNullable(System.getenv("SHOW_DIR"))
        .map(Boolean::parseBoolean)
        .orElse(false);
  }

  public static boolean sendNotifications(){
    return Optional.ofNullable(System.getenv("SEND_NOTIFICATIONS"))
        .map(Boolean::parseBoolean)
        .orElse(false);
  }
}
