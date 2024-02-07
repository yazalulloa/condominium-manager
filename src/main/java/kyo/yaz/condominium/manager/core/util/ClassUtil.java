package kyo.yaz.condominium.manager.core.util;

import java.util.Arrays;

public class ClassUtil {

  private ClassUtil() {
  }

  public static boolean isInstanceOf(Object obj, Class<?>... classes) {
    return Arrays.stream(classes).anyMatch(clazz -> clazz.isInstance(obj));
  }
}
