package kyo.yaz.condominium.manager.core.util;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.icon.IconFactory;
import java.util.Locale;

@JsModule("./icons/my-icons-icons.js")
public enum MyIconsIcons implements IconFactory {
  ICONS8_TELEGRAM;

  public Icon create() {
    return new Icon(this.name().toLowerCase(Locale.ENGLISH).replace('_', '-').replaceAll("^-", ""));
  }

  public static final class Icon extends com.vaadin.flow.component.icon.Icon {

    Icon(String icon) {
      super("my-icons-icons", icon);
    }
  }
}
