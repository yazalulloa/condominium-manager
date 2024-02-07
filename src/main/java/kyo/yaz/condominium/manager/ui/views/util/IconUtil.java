package kyo.yaz.condominium.manager.ui.views.util;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class IconUtil {

  public static Icon trash() {
    final var icon = VaadinIcon.TRASH.create();
    icon.setColor(Color.RED.color());

    return icon;
  }

  public static Icon cross() {
    final var icon = VaadinIcon.CLOSE_SMALL.create();
    icon.setColor(Color.RED.color());
    return icon;
  }

  public static Icon checkMark() {
    final var icon = VaadinIcon.CHECK.create();
    icon.setColor(Color.LIGHT_GREEN.color());
    return icon;
  }

  public static Icon checkMarkOrCross(boolean bool) {
    return bool ? checkMark() : cross();
  }
}
