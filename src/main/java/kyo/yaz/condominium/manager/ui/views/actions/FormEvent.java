package kyo.yaz.condominium.manager.ui.views.actions;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public abstract class FormEvent<T extends Component, S> extends ComponentEvent<T> {
    private final S obj;

    protected FormEvent(T source, S obj) {
        super(source, false);
        this.obj = obj;
    }

    public S getObj() {
        return obj;
    }
}
