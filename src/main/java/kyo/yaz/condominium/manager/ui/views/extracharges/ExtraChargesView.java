package kyo.yaz.condominium.manager.ui.views.extracharges;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.domain.ExtraChargeViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ExtraChargesView extends BaseDiv {
    private final Set<ExtraChargeViewItem> items = new LinkedHashSet<>();
    private final Grid<ExtraChargeViewItem> grid = new Grid<>(ExtraChargeViewItem.class, false);
    private final ExtraChargeForm form = new ExtraChargeForm();
    private final Button addBtn = new Button(Labels.ADD);


    public void init() {
        addClassName("extra-charges-view");
        configureGrid();
        configureForm();
        add(new H3(Labels.EXTRA_CHARGE_TITLE), addBtn, getContent());
        closeEditor();
        setItemsGrid();
    }

    public Set<ExtraChargeViewItem> items() {
        return items;
    }

    private Component getContent() {

        addBtn.setDisableOnClick(true);
        addBtn.addClickListener(click -> addEntity());

        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        return content;
    }

    private void configureForm() {
        form.setWidth("25em");
        form.setHeightFull();

        form.addListener(ExtraChargeForm.SaveEvent.class, event -> {

            items.add(event.getObj());
            setItemsGrid();
        });

        form.addListener(ExtraChargeForm.DeleteEvent.class, event -> removeItem(event.getObj()));
        form.addListener(ExtraChargeForm.CloseEvent.class, e -> closeEditor());
    }

    private void configureGrid() {
        grid.addClassNames("extra-charge-grid");
        grid.setColumnReorderingAllowed(true);
        grid.setAllRowsVisible(true);
        grid.addColumn(ExtraChargeViewItem::getAptNumber).setHeader(Labels.ExtraCharge.APT_LABEL).setSortable(true).setKey(Labels.ExtraCharge.APT_LABEL);
        grid.addColumn(ExtraChargeViewItem::getDescription).setHeader(Labels.ExtraCharge.DESCRIPTION_LABEL);
        grid.addColumn(ExtraChargeViewItem::getAmount).setHeader(Labels.ExtraCharge.AMOUNT_LABEL).setSortable(true).setKey(Labels.ExtraCharge.AMOUNT_LABEL);
        grid.addColumn(ExtraChargeViewItem::getCurrency).setHeader(Labels.ExtraCharge.CURRENCY_LABEL).setSortable(true).setKey(Labels.ExtraCharge.CURRENCY_LABEL);

        grid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> this.removeItem(item));
                            button.setIcon(new Icon(VaadinIcon.TRASH));
                        }))
                .setHeader(Labels.DELETE)
                .setTextAlign(ColumnTextAlign.END)
                .setFlexGrow(0);

        grid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_SUCCESS,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> {
                                final var newItem = item.toBuilder().build();
                                editEntity(newItem);
                            });
                            button.setIcon(new Icon(VaadinIcon.COPY));
                        }))
                .setHeader(Labels.COPY)
                .setTextAlign(ColumnTextAlign.END)
                .setFlexGrow(0);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event -> editEntity(event.getValue()));
        loadItems();

    }

    private void addEntity() {
        grid.asSingleSelect().clear();
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
        items.remove(item);
        form.setItem(form.defaultItem());
        setItemsGrid();
    }

    public void setItemsGrid() {
        uiAsyncAction(this::loadItems);
    }

    public void loadItems() {
        grid.setItems(items);
        grid.getDataProvider().refreshAll();
    }

    private void closeEditor() {
        form.setItem(null);
        form.setVisible(false);
        addBtn.setEnabled(true);
        removeClassName("editing");
    }

    public void setApartments(List<String> list) {
        form.setApartments(list);
    }

    public void setItems(Collection<ExtraChargeViewItem> collection) {
        items.addAll(collection);
    }

}
