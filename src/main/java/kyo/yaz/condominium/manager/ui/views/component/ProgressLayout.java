package kyo.yaz.condominium.manager.ui.views.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

public class ProgressLayout extends VerticalLayout {

    private final ProgressBar progressBar = new ProgressBar();
    private final Div progressBarLabel = new Div();

    public ProgressLayout() {
        super();
        add(progressBarLabel, progressBar);
    }

    public ProgressBar progressBar() {
        return progressBar;
    }

    public void setProgressText(String text) {
        progressBarLabel.setText(text);
    }
}
