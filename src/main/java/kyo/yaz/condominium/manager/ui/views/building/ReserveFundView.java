package kyo.yaz.condominium.manager.ui.views.building;

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
import kyo.yaz.condominium.manager.ui.views.domain.ReserveFundViewItem;
import kyo.yaz.condominium.manager.ui.views.util.IconUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ReserveFundView extends BaseDiv {
    private final Set<ReserveFundViewItem> items = new LinkedHashSet<>();
    private final Button addBtn = new Button(Labels.ADD);
    private final Grid<ReserveFundViewItem> grid = new Grid<>(ReserveFundViewItem.class, false);
    private final ReserveFundForm form = new ReserveFundForm();


    public void init() {
        addClassName("reserve-fund-view");
        configureGrid();
        configureForm();
        add(new H3(Labels.RESERVE_FUNDS_TITLE), addBtn, getContent());
        closeEditor();
        setItemsGrid();
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
        form.addListener(ReserveFundForm.SaveEvent.class, e -> addItem(e.getObj()));
        form.addListener(ReserveFundForm.DeleteEvent.class, e -> removeItem(e.getObj()));
        form.addListener(ReserveFundForm.CloseEvent.class, e -> closeEditor());
    }

    private void configureGrid() {
        grid.addClassNames("reserve-fund-grid");
        grid.setColumnReorderingAllowed(true);
        grid.setAllRowsVisible(true);
        grid.addColumn(ReserveFundViewItem::getName).setHeader(Labels.ReserveFund.NAME_LABEL).setSortable(true).setKey(Labels.ReserveFund.NAME_LABEL);
        grid.addColumn(ReserveFundViewItem::getFund).setHeader(Labels.ReserveFund.FUND_LABEL).setSortable(true).setKey(Labels.ReserveFund.FUND_LABEL);
        grid.addColumn(ReserveFundViewItem::getPercentage).setHeader(Labels.ReserveFund.PERCENTAGE_LABEL).setSortable(true).setKey(Labels.ReserveFund.PERCENTAGE_LABEL);
        grid.addComponentColumn(reserveFund -> IconUtil.checkMarkOrCross(reserveFund.isActive())).setHeader(Labels.ReserveFund.ACTIVE_LABEL).setSortable(true).setKey(Labels.ReserveFund.ACTIVE_LABEL);

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

    public void editEntity(ReserveFundViewItem item) {
        if (item == null) {
            closeEditor();
        } else {
            form.setItem(item);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.setItem(null);
        form.setVisible(false);
        addBtn.setEnabled(true);
        removeClassName("editing");
    }

    private void addItem(ReserveFundViewItem item) {
        items.add(item);
        form.reloadItem();
        setItemsGrid();
    }

    private void removeItem(ReserveFundViewItem item) {
        items.remove(item);
        form.reloadItem();
        setItemsGrid();
    }

    public void addItems(List<ReserveFundViewItem> items) {
        this.items.addAll(items);
    }

    public Collection<ReserveFundViewItem> list() {
        return items;
    }

    public void setItemsGrid() {
        uiAsyncAction(this::loadItems);
    }

    public void loadItems() {
        grid.setItems(items);
        grid.getDataProvider().refreshAll();
    }
}
