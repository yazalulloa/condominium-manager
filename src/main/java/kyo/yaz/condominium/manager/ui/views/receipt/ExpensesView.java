package kyo.yaz.condominium.manager.ui.views.receipt;

import com.vaadin.flow.component.Component;
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
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.domain.ExpenseViewItem;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;

public class ExpensesView extends BaseDiv {

    private final LinkedList<ExpenseViewItem> items = new LinkedList<>();
    private final Grid<ExpenseViewItem> grid = new Grid<>(ExpenseViewItem.class, false);
    private final ExpenseForm form = new ExpenseForm();
    private final Button addBtn = new Button(Labels.ADD);


    private BigDecimal totalCommon = BigDecimal.ZERO;
    private BigDecimal totalUnCommon = BigDecimal.ZERO;


    private ExpenseViewItem draggedItem;

    public void init() {
        addClassName("expenses-view");
        configureGrid();
        configureForm();
        add(new H3(Labels.EXPENSES), addBtn, getContent());
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

    public Collection<ExpenseViewItem> items() {
        return items;
    }


    public BigDecimal totalCommon() {
        return totalCommon;
    }

    public BigDecimal totalUnCommon() {
        return totalUnCommon;
    }

    public ExpenseForm form() {
        return form;
    }

    private void configureForm() {
        form.setWidth("25em");
        form.setHeightFull();

        form.addListener(ExpenseForm.SaveEvent.class, event -> {

            final var item = event.getObj();
            final var indexOf = items.indexOf(item);
            if (indexOf > -1) {
                items.set(indexOf, item);
            } else {
                items.add(item);
            }

            setItemsGrid();
        });

        form.addListener(ExpenseForm.DeleteEvent.class, event -> removeItem(event.getObj()));
        form.addListener(ExpenseForm.CloseEvent.class, e -> closeEditor());
    }

    private void configureGrid() {
        grid.addClassNames("expenses-grid");
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

        grid.addColumn(ExpenseViewItem::getDescription).setHeader(Labels.Expense.DESCRIPTION_LABEL);
        grid.addColumn(item -> ConvertUtil.format(item.getAmount(), item.getCurrency())).setHeader(Labels.Expense.AMOUNT_LABEL).setSortable(true).setKey(Labels.Expense.AMOUNT_LABEL);
        grid.addColumn(ExpenseViewItem::getType).setHeader(Labels.Expense.TYPE_LABEL).setSortable(true).setKey(Labels.Expense.TYPE_LABEL);

        grid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, expense) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> this.removeItem(expense));
                            button.setIcon(new Icon(VaadinIcon.TRASH));
                        }))
                .setHeader(Labels.DELETE)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        grid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_SUCCESS,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> {
                                final var newItem = item.toBuilder().build();
                                form.setItem(newItem);
                            });
                            button.setIcon(new Icon(VaadinIcon.COPY));
                        }))
                .setHeader(Labels.COPY)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event -> editEntity(event.getValue()));
        loadItems();

    }


    private void addEntity() {
        grid.asSingleSelect().clear();
        editEntity(form.defaultItem());
    }

    public void editEntity(ExpenseViewItem item) {
        if (item == null) {
            closeEditor();
        } else {
            form.setItem(item);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void removeItem(ExpenseViewItem item) {
        items.remove(item);
        form.setItem(form.defaultItem());
        setItemsGrid();
    }

    public void setItemsGrid() {
        uiAsyncAction(this::loadItems);
    }

    public void loadItems() {
        calculateTotal();
        grid.setItems(items);
        grid.getDataProvider().refreshAll();

        fireEvent(new LoadExpensesEvent(this));
    }

    private void closeEditor() {
        form.setItem(null);
        form.setVisible(false);
        addBtn.setEnabled(true);
        removeClassName("editing");
    }

    private void calculateTotal() {
        totalCommon = items.stream().filter(i -> i.getType() == Expense.Type.COMMON && !i.isReserveFund())
                .map(ExpenseViewItem::getAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        totalUnCommon = items.stream().filter(i -> i.getType() == Expense.Type.UNCOMMON)
                .map(ExpenseViewItem::getAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public static class LoadExpensesEvent extends ViewEvent<ExpensesView, Void> {

        public LoadExpensesEvent(ExpensesView source) {
            super(source);
        }
    }

    public void setItems(Collection<ExpenseViewItem> collection) {
        collection.stream().filter(i -> !i.isReserveFund())
                .forEach(items::add);
        calculateTotal();
    }
}
