package kyo.yaz.condominium.manager.core.util;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CachedResultSupplier<T> implements Supplier<T> {

  private final Supplier<T> supplier;
  private final List<T> cache;

  public CachedResultSupplier(Supplier<T> supplier) {
    this.supplier = supplier;
    this.cache = new ArrayList<>(1);
  }

  @Override
  public T get() {
    if (cache.isEmpty()) {
      cache.add(supplier.get());
    }
    return cache.get(0);
  }
}
