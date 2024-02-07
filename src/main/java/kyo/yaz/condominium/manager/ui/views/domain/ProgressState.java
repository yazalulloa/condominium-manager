package kyo.yaz.condominium.manager.ui.views.domain;

public interface ProgressState {

  String text();

  String endText();

  String subText();

  boolean indeterminate();

  double min();

  double max();

  double value();

  boolean visible();
}
