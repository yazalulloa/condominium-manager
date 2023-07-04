package kyo.yaz.condominium.manager.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.reflections.util.ClasspathHelper;

public class ReflectionUtil {

  public final static Map<String, Class<?>> PRIMITIVES = new HashMap<>();
  public final static Set<Class<?>> classes = new HashSet<>(Arrays.asList(
      String.class, Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class,
      Double.class, BigDecimal.class, BigInteger.class, UUID.class, Date.class, LocalDate.class,
      LocalDateTime.class, ZonedDateTime.class, OffsetDateTime.class));

  static {
    PRIMITIVES.put("long", long.class);
    PRIMITIVES.put("int", int.class);
    PRIMITIVES.put("short", short.class);
    PRIMITIVES.put("byte", byte.class);
    PRIMITIVES.put("char", char.class);
    PRIMITIVES.put("float", float.class);
    PRIMITIVES.put("double", double.class);
    PRIMITIVES.put("boolean", boolean.class);
  }

  private ReflectionUtil() {
  }

  public static <T> Class<T> parameterizedType(Class<?> clazz, int index) {
    final var genericSuperclass = clazz.getGenericSuperclass();
    if (!(genericSuperclass instanceof ParameterizedType)) {
      return parameterizedType((Class<?>) genericSuperclass, index);
    }

    ParameterizedType type = (ParameterizedType) genericSuperclass;
    Type[] arguments = type.getActualTypeArguments();
    Class<T> argument = (Class<T>) arguments[index];
    return argument;
  }

  public static Class<?> getGenericClass(Field field) {
    return classFromField(field);
  }

  public static Class<?> getGenericClass(Field field, int index) {
    return classFromField(field, index);
  }

  public static void accessFields(Field[] fields, Consumer<Field> consumer) {
    for (Field field : fields) {
      field.setAccessible(true);
      consumer.accept(field);
    }
  }

  public static boolean isGeneric(Class<?> c) {
    boolean hasTypeParameters = hasTypeParameters(c);
    boolean hasGenericSuperclass = hasGenericSuperclass(c);
//      boolean hasGenericSuperinterface = hasGenericSuperinterface(c);
//      boolean isGeneric = hasTypeParameters || hasGenericSuperclass || hasGenericSuperinterface;
    boolean isGeneric = hasTypeParameters || hasGenericSuperclass;

    return isGeneric;
  }

  public static boolean classExists(String str, ClassLoader loader) {
    try {
      Class.forName(str, false, loader);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static Class<?> forName(String str) {
    try {

      final Class<?> aClass = PRIMITIVES.get(str);
      if (aClass != null) {
        return aClass;
      }

      return Class.forName(str);
    } catch (ClassNotFoundException e) {
      final var index = str.lastIndexOf(".");
      final var stringBuilder = new StringBuilder(str);
      stringBuilder.setCharAt(index, '$');
      try {
        return Class.forName(stringBuilder.toString());
      } catch (ClassNotFoundException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  public static boolean hasTypeParameters(Class<?> c) {
    return c.getTypeParameters().length > 0;
  }

  public static boolean hasGenericSuperclass(Class<?> c) {
    Class<?> testClass = c;

    while (testClass != null) {
      Type t = testClass.getGenericSuperclass();

      if (t instanceof ParameterizedType) {
        return true;
      }

      testClass = testClass.getSuperclass();
    }

    return false;
  }

  public static boolean hasGenericSuperinterface(Class<?> c) {
    for (Type t : c.getGenericInterfaces()) {
      if (t instanceof ParameterizedType) {
        return true;
      }
    }

    return false;
  }

  public static boolean isNativeList(Field field) {
    return Collection.class.isAssignableFrom(field.getType()) && classes.contains(getGenericClass(field));
  }

  public static boolean isCustomList(Field field) {
    return Collection.class.isAssignableFrom(field.getType()) && !classes.contains(getGenericClass(field));
  }

  public static boolean isCustomArray(Field field) {
    return field.getType().isArray() && !classes.contains(field.getType().getComponentType());
  }

  public static boolean isCustomObj(Field field) {
    return !(Collection.class.isAssignableFrom(field.getType()) && classes.contains(getGenericClass(field))
        || !isCustomObj(field.getType()));
  }


  public static boolean isCustomObj(Class<?> clazz) {
    return !(clazz.isPrimitive() || clazz.isAnnotation() || clazz.isEnum() || classes.contains(clazz) || (
        clazz.isArray() && clazz.getComponentType() != null && classes.contains(clazz.getComponentType()))
        || Map.class.isAssignableFrom(clazz));
  }

  public static Class<?> classFromType(Type type, int index) {
    if (type instanceof ParameterizedType) {
      Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
      if (arguments.length <= index) {
        return classFromType(type, index - 1);
      } else {
        return classFromType(arguments[index], index);
      }

    } else {
      return (Class<?>) type;
    }
  }

  public static Class<?> classFromField(Field field) {
    return classFromField(field, 0);
  }

  public static Class<?> classFromField(Field field, int index) {
    if (isGeneric(field.getType())) {
      return classFromType(field.getGenericType(), index);
    } else {
      return field.getType();
    }
  }


  public static void accessDeclaredFields(Class<?> clazz, Consumer<Field> consumer) {
    accessFields(clazz.getDeclaredFields(), consumer);
  }

  public static boolean nonInnerClass(Class<?> clazz) {
    return clazz.getEnclosingClass() == null;
  }

  public static boolean isInnerClass(Class<?> clazz) {
    return !nonInnerClass(clazz);
  }

  public static boolean isInnerClass(String str) {

    final var i1 = str.lastIndexOf(".");
    if (i1 <= 0) {
      return false;
    }

    final var i2 = str.lastIndexOf(".", i1 - 1);

    if (i2 <= 0) {
      return false;
    }
    final var innerClass = str.substring(i1 + 1);
    final var enclosingClass = str.substring(i2 + 1, i1);

    return Character.isUpperCase(innerClass.charAt(0)) && Character.isUpperCase(enclosingClass.charAt(0));
  }

  public static URLClassLoader classLoader(String path) throws MalformedURLException {
    URL testClassesURL = Paths.get(path).toUri().toURL();
    return URLClassLoader.newInstance(new URL[]{testClassesURL},
        ClasspathHelper.staticClassLoader());
  }


  public static boolean hasMethod(Class<?> clazz, String methodName) {
    final var methods = clazz.getMethods();
    return Arrays.stream(methods)
        .map(method -> method.getName().equals(methodName))
        .filter(b -> b)
        .findFirst()
        .orElse(false);
  }

  public static boolean isDate(Class<?> clazz) {
    return clazz.equals(ZonedDateTime.class)
        || clazz.equals(LocalDateTime.class)
        || clazz.equals(LocalDate.class)
        || clazz.equals(OffsetDateTime.class);
  }


  public static boolean isInstanceOf(Object obj, Class<?>... classes) {
    return Arrays.stream(classes).anyMatch(clazz -> clazz.isInstance(obj));
  }
}
