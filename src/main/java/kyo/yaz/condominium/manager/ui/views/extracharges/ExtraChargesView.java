package kyo.yaz.condominium.manager.ui.views.extracharges;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.component.DragDropDiv;
import kyo.yaz.condominium.manager.ui.views.component.DragDropList;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ExtraChargesView extends BaseDiv {
    private final ExtraChargeForm form = new ExtraChargeForm();
    private final Button addBtn = new Button(Labels.ADD);

    /*private final LinkedList<ExtraChargeViewItem> items = new LinkedList<>();
    private final Grid<ExtraChargeViewItem> grid = new Grid<>(ExtraChargeViewItem.class, false);*/
    private final DragDropList<ExtraChargeViewItem, DragDropDiv<ExtraChargeViewItem>> extraCharges = new DragDropList<>();


    public void init() {
        addClassName("extra-charges-view");
        configureForm();
        add(new HorizontalLayout(new H3(Labels.EXTRA_CHARGE_TITLE), addBtn), getContent());
        closeEditor();
    }

    public Collection<ExtraChargeViewItem> items() {
        return extraCharges.components().stream().map(DragDropDiv::item).collect(Collectors.toCollection(LinkedList::new));
    }

    private Component getContent() {

        addBtn.setDisableOnClick(true);
        addBtn.addClickListener(click -> addEntity());

        final var content = new HorizontalLayout(extraCharges, form);
        content.setFlexGrow(2, extraCharges);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        return content;
    }

    private void configureForm() {
        form.setHeightFull();

        form.addListener(ExtraChargeForm.SaveEvent.class, event -> {
            final var item = event.getObj();

            extraCharges.saveOrUpdate(item, this::addExtraCharge);

            closeEditor();
        });

        form.addListener(ExtraChargeForm.DeleteEvent.class, event -> removeItem(event.getObj()));
        form.addListener(ExtraChargeForm.CloseEvent.class, e -> closeEditor());
    }

    private DragDropDiv<ExtraChargeViewItem> addExtraCharge(ExtraChargeViewItem item) {

        final var card = card(item);
        card.addClickListener(e -> {
            if (e.getClickCount() == 2) {
                editEntity(item);
            }
        });
        return card;
    }

    private DragDropDiv<ExtraChargeViewItem> card(ExtraChargeViewItem item) {

        final var card = new DragDropDiv<>(item);
        card.addClassName("card");

        final var body = new Div(new Span(item.getAptNumber()), new Span(item.getName()), new Span(ConvertUtil.format(item.getAmount(), item.getCurrency())));
        body.addClassName("body");

        final var buttons = new Div(editBtn(new Button(), item), copyBtn(new Button(), item), deleteBtn(new Button(), item));
        buttons.addClassName("buttons");
        card.add(body, buttons);

        return card;
    }

    private Button deleteBtn(Button button, ExtraChargeViewItem item) {
        button.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(e -> this.removeItem(item));
        button.setIcon(new Icon(VaadinIcon.TRASH));
        return button;
    }

    private Button copyBtn(Button button, ExtraChargeViewItem item) {
        button.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_SUCCESS,
                ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(e -> {
            final var newItem = item.toBuilder().build();
            editEntity(newItem);
        });
        button.setIcon(new Icon(VaadinIcon.COPY));
        return button;
    }

    private Button editBtn(Button button, ExtraChargeViewItem item) {
        button.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(e -> editEntity(item));
        button.setIcon(new Icon(VaadinIcon.EDIT));
        return button;
    }

    private void addEntity() {
        editEntity(form.defaultItem());
    }

    public void editEntity(ExtraChargeViewItem item) {
        if (item == null) {
            closeEditor();
        } else {
            form.setItem(item);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void removeItem(ExtraChargeViewItem item) {
        extraCharges.removeItem(item);
        form.setItem(form.defaultItem());
    }


    private void    closeEditor() {
        form.setItem(null);
        form.setVisible(false);
        addBtn.setEnabled(true);
        removeClassName("editing");
    }

    public void setApartments(List<Apartment> list) {
        form.setApartments(list);
    }

    public void setItems(Collection<ExtraChargeViewItem> collection) {
        extraCharges.removeAll();
        collection.stream().map(this::addExtraCharge).forEach(extraCharges::addComponent);
    }

}
