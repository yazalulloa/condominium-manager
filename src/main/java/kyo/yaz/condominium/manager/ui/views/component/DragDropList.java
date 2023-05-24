package kyo.yaz.condominium.manager.ui.views.component;

import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.html.Div;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class DragDropList<S, T extends DragDropDiv<S>> extends Div {

    private final List<T> components = new LinkedList<>();

    public void addComponent(T component) {
        components.add(component);
        setListeners(component);
        add(component);
    }

    private Optional<T> find(S item) {
        return components.stream().filter(i -> i.item() == item)
                .findFirst();
    }

    public void removeItem(S item) {
        find(item)
                .ifPresent(component -> {
                    log.info("removeItem {}", item);
                    components.remove(component);
                    remove(component);
                });

    }

    public List<T> components() {
        return components;
    }

    private void setListeners(T card) {

        card.addDragStartListener(event -> {
            // Highlight suitable drop targets in the UI
            components().forEach(target -> {

                if (target != card) {
                    target.addClassName("possible-drop-zone");
                }
            });
        });

        card.addDragEndListener(event -> {
            components().forEach(target -> target.removeClassName("possible-drop-zone"));
            final var dropEffect = event.getDropEffect();
            final var component = event.getComponent();
            // log.info("SUCCESS {} {}, {}", event.isSuccessful(), dropEffect, component.toString());
            // NOTE: The following is always FALSE for Edge and Safari !!!
            if (event.isSuccessful()) {

                //final var dropEffect = event.getDropEffect();
            }
        });

        card.addDropListener(event -> {


            final var target = (T) event.getComponent();
            final var dragData = event.getDragData().orElse(null);
            final var source = (T) event.getDragSourceComponent().orElse(null);

            //log.info("{} {} {} {} {}", event.getDropEffect(), source, dragData, target, event.getEffectAllowed());

            // log.info("\nSOURCE {}\nTARGET {}", source.item(), target.item());
            // move the dragged component to inside the drop target component
            if (event.getDropEffect() == DropEffect.MOVE) {
                change(source, target);

                // the drag source is available only if the dragged component is from
                // the same UI as the drop target
                //event.getDragSourceComponent().ifPresent(box::add);

                event.getDragData().ifPresent(data -> {
                    // the server side drag data is available if it has been set and the
                    // component was dragged from the same UI as the drop target
                });
            }
        });
    }

    public void change(T source, T target) {
        if (source == target) {
            return;
        }

        final var index = components.indexOf(target);
        if (index < 0) {
            log.info("target not found");
            return;
        }

        components.remove(source);
        components.add(index, source);

        remove(source);
        addComponentAtIndex(index, source);
    }

    public void saveOrUpdate(S item, Function<S, T> consumer) {
        final var optional = find(item);

        final var newComponent = consumer.apply(item);
        if (optional.isPresent()) {
            final var component = optional.get();
            final var index = indexOf(component);
            remove(component);
            components.remove(component);
            components.add(newComponent);
            addComponentAtIndex(index, newComponent);
        } else {
            addComponent(newComponent);
        }

    }
}
