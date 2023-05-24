package kyo.yaz.condominium.manager.ui.views.building;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
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
import java.util.LinkedList;
import java.util.List;

public class ReserveFundView extends BaseDiv {
    private final LinkedList<ReserveFundViewItem> items = new LinkedList<>();
    private final Button addBtn = new Button(Labels.ADD);
    private final Grid<ReserveFundViewItem> grid = new Grid<>(ReserveFundViewItem.class, false);
    private final ReserveFundForm form = new ReserveFundForm();

    private ReserveFundViewItem draggedItem;

    public void init() {
        addClassName("reserve-fund-view");
        configureGrid();
        configureForm();
        add(new HorizontalLayout(new H3(Labels.RESERVE_FUNDS_TITLE), addBtn), getContent());
        closeEditor();
        setItemsGrid();
    }


    private Component getContent() {

        addBtn.setDisableOnClick(true);
        addBtn.addClickListener(click -> addEntity());

        final var content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        return content;
    }

    private void configureForm() {
        form.setWidth(30, Unit.EM);
        form.setHeightFull();
        form.addListener(ReserveFundForm.SaveEvent.class, e -> addItem(e.getObj()));
        form.addListener(ReserveFundForm.DeleteEvent.class, e -> removeItem(e.getObj()));
        form.addListener(ReserveFundForm.CloseEvent.class, e -> closeEditor());
    }


    private void configureGrid() {
        grid.addClassNames("reserve-fund-grid");
        grid.setAllRowsVisible(true);
        grid.setColumnReorderingAllowed(true);

        grid.setDropMode(GridDropMode.BETWEEN);
        grid.setRowsDraggable(true);

        grid.addDragStartListener(e -> draggedItem = e.getDraggedItems().get(0));

        final var dataView = grid.setItems(items);

        grid.addDropListener(e -> {
            final var target = e.getDropTargetItem().orElse(null);
            final var dropLocation = e.getDropLocation();

            boolean itemWasDroppedOntoItself = draggedItem
                    .equals(target);

            if (target == null || itemWasDroppedOntoItself)
                return;

            dataView.removeItem(draggedItem);

            if (dropLocation == GridDropLocation.BELOW) {
                dataView.addItemAfter(draggedItem, target);
            } else {
                dataView.addItemBefore(draggedItem, target);
            }
        });

        grid.addDragEndListener(e -> draggedItem = null);

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

        form.reloadItem();
        final var indexOf = items.indexOf(item);
        if (indexOf > -1) {
            items.set(indexOf, item);
        } else {
            items.add(item);
        }

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
