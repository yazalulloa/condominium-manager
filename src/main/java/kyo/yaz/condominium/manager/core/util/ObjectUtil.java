package kyo.yaz.condominium.manager.core.util;

import java.util.Optional;

public class ObjectUtil {

  public static boolean aBoolean(Boolean aBoolean) {
    return Optional.ofNullable(aBoolean).orElse(false);
  }


}
