package kyo.yaz.condominium.manager.ui.views.util;

public enum Color {

  RED("#F70900"), GREEN("#13b931"), LIGHT_GREEN("#1aff1a");

  private final String color;


  Color(String color) {
    this.color = color;
  }

  public String color() {
    return color;
  }
}
