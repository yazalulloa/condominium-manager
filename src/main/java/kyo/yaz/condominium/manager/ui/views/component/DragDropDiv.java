package kyo.yaz.condominium.manager.ui.views.component;

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.html.Div;

public class DragDropDiv<T> extends Div implements DragSource<Div>, DropTarget<Div>, HasStyle {

  private final T item;

  public DragDropDiv(T item) {
    this.item = item;
    setDraggable(true);
    setActive(true);
    setDropEffect(DropEffect.MOVE);
    setEffectAllowed(EffectAllowed.MOVE);
  }

  public T item() {
    return item;
  }


}
