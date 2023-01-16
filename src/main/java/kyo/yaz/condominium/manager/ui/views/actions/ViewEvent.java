package kyo.yaz.condominium.manager.ui.views.actions;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public abstract class ViewEvent<T extends Component, S> extends ComponentEvent<T> {
    private final S obj;

    public ViewEvent(T source, S obj) {
        super(source, false);
        this.obj = obj;
    }

    public ViewEvent(T source) {
        this(source, null);
    }

    public S getObj() {
        return obj;
    }
}
