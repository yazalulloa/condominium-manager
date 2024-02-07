package kyo.yaz.condominium.manager.ui.views.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import kyo.yaz.condominium.manager.ui.views.domain.ProgressState;

public class ProgressLayout extends Div {


  private final ProgressBar progressBar = new ProgressBar();
  private final Div progressBarLabelText = new Div();
  private final Div progressBarLabelValue = new Div();
  private final Div progressBarSubLabel = new Div();

  public ProgressLayout() {
    super();
    setWidthFull();
    progressBarSubLabel.getStyle().set("font-size", "var(--lumo-font-size-xs)");

    final var progressBarLabel = new FlexLayout();
    progressBarLabel.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    progressBarLabel.add(progressBarLabelText, progressBarLabelValue);

    add(progressBarLabel, progressBar, progressBarSubLabel);
  }

  @Override
  public void setText(String text) {
    super.setText(text);
  }

  public ProgressBar progressBar() {
    return progressBar;
  }

  public void setProgressText(String text) {
    setProgressText(text, null);
  }

  public void setProgressText(String text, String endText) {
    setProgressText(text, endText, null);
  }

  public void setProgressText(String text, String endText, String subText) {
    progressBarLabelText.setText(text);
    progressBarLabelValue.setText(endText);
    setSubText(subText);
  }

  public void setSubText(String text) {
    progressBarSubLabel.setText(text);
  }

  public void setState(ProgressState state) {

    progressBar.setIndeterminate(state.indeterminate());

    if (!state.indeterminate()) {
      progressBar.setValue(state.value());
      progressBar.setMax(state.max());
      progressBar.setMin(state.min());
    }

    setProgressText(state.text(), state.endText(), state.subText());

    setVisible(state.visible());
  }
}
