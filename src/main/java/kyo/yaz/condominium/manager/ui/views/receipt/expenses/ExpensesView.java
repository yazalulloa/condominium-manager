package kyo.yaz.condominium.manager.ui.views.receipt.expenses;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.component.DragDropDiv;
import kyo.yaz.condominium.manager.ui.views.component.DragDropList;
import kyo.yaz.condominium.manager.ui.views.domain.ExpenseViewItem;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;


@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExpensesView extends BaseDiv {

    private final DragDropList<ExpenseViewItem, DragDropDiv<ExpenseViewItem>> expenses = new DragDropList<>();

    private final Button addBtn = new Button(Labels.ADD);

    private BigDecimal totalCommon = BigDecimal.ZERO;
    private BigDecimal totalUnCommon = BigDecimal.ZERO;

    private final ExpenseForm form;
    private final TranslationProvider translationProvider;

    @Autowired
    public ExpensesView(ExpenseForm form, TranslationProvider translationProvider) {
        this.form = form;
        this.translationProvider = translationProvider;
    }

    public void init() {
        addClassName("expenses-view");
        configureForm();
        add(new HorizontalLayout(new H3(Labels.EXPENSES), addBtn), getContent());
        closeEditor();
        setItemsGrid();
    }

    private Component getContent() {

        addBtn.setDisableOnClick(true);
        addBtn.addClickListener(click -> addEntity());

        final var content = new HorizontalLayout(expenses, form);
        content.setFlexGrow(2, expenses);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        return content;
    }

    public Collection<ExpenseViewItem> items() {
        return expenses.components().stream().map(DragDropDiv::item).collect(Collectors.toCollection(LinkedList::new));
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
        form.setHeightFull();

        form.addListener(ExpenseForm.SaveEvent.class, event -> {


            final var item = event.getObj();

            expenses.saveOrUpdate(item, this::addExpense);

            setItemsGrid();
        });

        form.addListener(ExpenseForm.DeleteEvent.class, event -> removeItem(event.getObj()));
        form.addListener(ExpenseForm.CloseEvent.class, e -> closeEditor());
    }


    private DragDropDiv<ExpenseViewItem> card(ExpenseViewItem item) {

        final var card = new DragDropDiv<>(item);
        card.addClassName("card");
        final var body = new Div(new Span(item.getDescription()), new Span(ConvertUtil.format(item.getAmount(), item.getCurrency())),
                new Span(translationProvider.translate(item.getType().name())));
        body.addClassName("body");

        final var buttons = new Div(editBtn(new Button(), item), copyBtn(new Button(), item), deleteBtn(new Button(), item));
        buttons.addClassName("buttons");
        card.add(body, buttons);

        return card;
    }

    private Button deleteBtn(Button button, ExpenseViewItem item) {
        button.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(e -> this.removeItem(item));
        button.setIcon(new Icon(VaadinIcon.TRASH));
        return button;
    }

    private Button copyBtn(Button button, ExpenseViewItem item) {
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

    private Button editBtn(Button button, ExpenseViewItem item) {
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
        expenses.removeItem(item);
        form.setItem(form.defaultItem());
        setItemsGrid();
    }

    public void setItemsGrid() {
        uiAsyncAction(this::loadItems);
    }


    public void loadItems() {
        calculateTotal();
        fireEvent(new LoadExpensesEvent(this));
    }

    private void closeEditor() {
        form.setItem(null);
        form.setVisible(false);
        addBtn.setEnabled(true);
        removeClassName("editing");
    }

    private void calculateTotal() {
        totalCommon = BigDecimal.ZERO;
        totalUnCommon = BigDecimal.ZERO;
        for (ExpenseViewItem item : items()) {
            if (!item.isReserveFund()) {
                switch (item.getType()) {
                    case COMMON -> totalCommon = totalCommon.add(item.getAmount());
                    case UNCOMMON -> totalUnCommon = totalUnCommon.add(item.getAmount());
                }
            }
        }
    }

    public static class LoadExpensesEvent extends ViewEvent<ExpensesView, Void> {

        public LoadExpensesEvent(ExpensesView source) {
            super(source);
        }
    }

    private DragDropDiv<ExpenseViewItem> addExpense(ExpenseViewItem item) {

        final var card = card(item);
        card.addClickListener(e -> {
            if (e.getClickCount() == 2) {
                editEntity(item);
            }
        });
        return card;
    }

    public void setItems(Collection<ExpenseViewItem> collection) {


        expenses.removeAll();
        collection.stream().filter(i -> !i.isReserveFund())
                .map(this::addExpense).forEach(expenses::addComponent);

        calculateTotal();
    }
}
