package kyo.yaz.condominium.manager.ui.views.building;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.component.DragDropDiv;
import kyo.yaz.condominium.manager.ui.views.component.DragDropList;
import kyo.yaz.condominium.manager.ui.views.domain.ReserveFundViewItem;
import kyo.yaz.condominium.manager.ui.views.util.IconUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
@Scope("prototype")
public class ReserveFundView extends BaseDiv {
    //private final LinkedList<ReserveFundViewItem> items = new LinkedList<>();
    private final Button addBtn = new Button(Labels.ADD);
    //private final Grid<ReserveFundViewItem> grid = new Grid<>(ReserveFundViewItem.class, false);
    private final ReserveFundForm form;

    private final DragDropList<ReserveFundViewItem, DragDropDiv<ReserveFundViewItem>> funds = new DragDropList<>();


    //private ReserveFundViewItem draggedItem;

    private final TranslationProvider translationProvider;

    @Autowired
    public ReserveFundView(ReserveFundForm form, TranslationProvider translationProvider) {
        this.form = form;
        this.translationProvider = translationProvider;
    }

    public void init() {
        addClassName("reserve-funds-view");
        configureForm();
        add(new HorizontalLayout(new H3(Labels.RESERVE_FUNDS_TITLE), addBtn), getContent());
        closeEditor();
    }

    public Collection<ReserveFundViewItem> items() {
        return funds.components().stream().map(DragDropDiv::item).collect(Collectors.toCollection(LinkedList::new));
    }


    private Component getContent() {

        addBtn.setDisableOnClick(true);
        addBtn.addClickListener(click -> addEntity());

        final var content = new HorizontalLayout(funds, form);
        content.setFlexGrow(2, funds);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        return content;
    }

    private void configureForm() {
        form.setWidth(30, Unit.EM);
        form.setHeightFull();
        form.addListener(ReserveFundForm.SaveEvent.class, event -> {

            funds.saveOrUpdate(event.getObj(), this::addItem);
            closeEditor();
        });
        form.addListener(ReserveFundForm.DeleteEvent.class, e -> removeItem(e.getObj()));
        form.addListener(ReserveFundForm.CloseEvent.class, e -> closeEditor());
    }

    private DragDropDiv<ReserveFundViewItem> card(ReserveFundViewItem item) {

        final var card = new DragDropDiv<>(item);
        card.addClassName("card");

        final var icon = IconUtil.checkMarkOrCross(item.isActive());

        final var isActive = new Span(new Span(Labels.ReserveFund.ACTIVE_LABEL + ": "), icon);

        final var pay = Optional.ofNullable(item.getPay()).map(BigDecimal::toString)
                .orElse("");

        final var type = Optional.ofNullable(item.getType())
                .map(Enum::name)
                .orElse("");

        final var body = new Div(new Span(item.getName()), new Span(item.getFund().toString()), isActive,
                new Span(Labels.ReserveFund.TYPE_LABEL + ": " + translationProvider.translate(type)),
                new Span("Monto a pagar: " + pay));

        body.addClassName("body");

        final var buttons = new Div(editBtn(new Button(), item), copyBtn(new Button(), item), deleteBtn(new Button(), item));
        buttons.addClassName("buttons");
        card.add(body, buttons);

        return card;
    }

    /*private void configureGrid() {
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
        grid.addColumn(ReserveFundViewItem::getPay).setHeader(Labels.ReserveFund.PERCENTAGE_LABEL).setSortable(true).setKey(Labels.ReserveFund.PERCENTAGE_LABEL);
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
    }*/

   /*    private void addEntity() {
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
    }*/

 /*   private void closeEditor() {
        form.setItem(null);
        form.setVisible(false);
        addBtn.setEnabled(true);
        removeClassName("editing");
    }*/

    /*private void addItem(ReserveFundViewItem item) {

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
    }*/

/*    public void addItems(List<ReserveFundViewItem> items) {
        this.items.addAll(items);
    }

    public Collection<ReserveFundViewItem> list() {
        return items;
    }*/

   /* public void loadItems() {
        grid.setItems(items);
        grid.getDataProvider().refreshAll();
    }*/

    private Button deleteBtn(Button button, ReserveFundViewItem item) {
        button.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(e -> this.removeItem(item));
        button.setIcon(new Icon(VaadinIcon.TRASH));
        return button;
    }

    private Button copyBtn(Button button, ReserveFundViewItem item) {
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

    private Button editBtn(Button button, ReserveFundViewItem item) {
        button.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(e -> editEntity(item));
        button.setIcon(new Icon(VaadinIcon.EDIT));
        return button;
    }

    private DragDropDiv<ReserveFundViewItem> addItem(ReserveFundViewItem item) {

        final var card = card(item);
        card.addClickListener(e -> {
            if (e.getClickCount() == 2) {
                editEntity(item);
            }
        });
        return card;
    }

    private void addEntity() {
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

    private void removeItem(ReserveFundViewItem item) {
        funds.removeItem(item);
        form.setItem(form.defaultItem());
    }


    private void closeEditor() {
        form.setItem(null);
        form.setVisible(false);
        addBtn.setEnabled(true);
        removeClassName("editing");
    }


    public void setItems(Collection<ReserveFundViewItem> collection) {
        funds.removeAll();
        funds.components().clear();
        collection.stream().map(this::addItem).forEach(funds::addComponent);
    }

}
