package kyo.yaz.condominium.manager.ui.views.receipt.debts;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.domain.DebtViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class DebtsView extends BaseDiv {

    private List<DebtViewItem> items = Collections.emptyList();
    private final Grid<DebtViewItem> grid = new Grid<>();

    private final TranslationProvider translationProvider;
    private final DebtForm form;


    @Autowired
    public DebtsView(TranslationProvider translationProvider, DebtForm form) {
        super();
        this.translationProvider = translationProvider;
        this.form = form;
    }


    public void init() {

        addClassName("debts-view");
        configureGrid();
        configureForm();
        add(new H3(Labels.DEBTS), getContent());
        closeEditor();
        setItems(items);
    }

    private com.vaadin.flow.component.Component getContent() {

        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        return content;
    }

    public void setItems(List<DebtViewItem> debtViewItems) {
        items = debtViewItems;
        grid.setItems(items);
        grid.getDataProvider().refreshAll();
    }


    private void configureGrid() {
        grid.addClassNames("debt-grid");
        grid.setAllRowsVisible(true);
        grid.setColumnReorderingAllowed(true);
        //final var editor = grid.getEditor();

        grid.addColumn(item -> item.getAptNumber() + " " + item.getName())
                .setHeader(Labels.Debt.APT_LABEL)
                .setSortable(true)
                .setKey(Labels.Debt.APT_LABEL);

       /* grid.setSelectionMode(Grid.SelectionMode.SINGLE)
                .addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(item -> {
                    if (editor.isOpen())
                        editor.cancel();
                    grid.getEditor().editItem(item);
                }));*/

        final var receiptColumn = grid.addColumn(DebtViewItem::getReceipts).setHeader(Labels.Debt.RECEIPT_LABEL).setSortable(true).setKey(Labels.Debt.RECEIPT_LABEL);
        final var amountColumn = grid.addColumn(DebtViewItem::getAmount).setHeader(Labels.Debt.AMOUNT_LABEL).setSortable(true).setKey(Labels.Debt.AMOUNT_LABEL);

        final var monthsColumn = grid.addColumn(item -> {

            return Optional.ofNullable(item.getMonths())
                    .orElseGet(Collections::emptySet)
                    .stream()
                    .map(Enum::name)
                    .map(translationProvider::translate)
                    .collect(Collectors.joining("\n"));
        }).setHeader(Labels.Debt.MONTHS_LABEL);

        final var previousPaymentAmountColumn = grid.addColumn(DebtViewItem::getPreviousPaymentAmount).setHeader(Labels.Debt.PREVIOUS_AMOUNT_PAYED_LABEL).setSortable(true).setKey(Labels.Debt.PREVIOUS_AMOUNT_PAYED_LABEL);
        final var previousPaymentAmountCurrencyColumn = grid.addColumn(DebtViewItem::getPreviousPaymentAmountCurrency).setHeader(Labels.Debt.PREVIOUS_AMOUNT_CURRENCY_PAYED_LABEL).setSortable(true).setKey(Labels.Debt.PREVIOUS_AMOUNT_CURRENCY_PAYED_LABEL);


        grid.getColumns().forEach(col -> col.setAutoWidth(true));


       /* final var saveButton = new Button(VaadinIcon.CHECK.create(), e -> editor.save());

        final var cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> editor.cancel());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);

        final var actions = new HorizontalLayout(saveButton, cancelButton);

        final var editColumn = grid.addComponentColumn(item -> {
                    final var editButton = new Button(Labels.EDIT);
                    editButton.addClickListener(e -> {
                        if (editor.isOpen())
                            editor.cancel();
                        grid.getEditor().editItem(item);
                    });
                    return new HorizontalLayout(editButton);
                })
                .setTextAlign(ColumnTextAlign.END)
                .setFlexGrow(0);

        editColumn.setEditorComponent(actions);


        final var binder = new Binder<>(DebtViewItem.class);
        editor.setBinder(binder);
        editor.setBuffered(true);


        final var receiptsField = new IntegerField();

        binder.forField(receiptsField)
                //.asRequired("First name must not be empty")
                //.withStatusLabel(firstNameValidationMessage)
                .bind(DebtViewItem::getReceipts, DebtViewItem::setReceipts);
        receiptColumn.setEditorComponent(receiptsField);

        final var amountField = new BigDecimalField();

        binder.forField(amountField)
                .asRequired("Monto no puede estar vacio")
                //.withStatusLabel(firstNameValidationMessage)
                .bind(DebtViewItem::getAmount, DebtViewItem::setAmount);
        amountColumn.setEditorComponent(amountField);

        final var monthComboBox = ViewUtil.monthMultiComboBox();
        monthComboBox.setItemLabelGenerator(m -> translationProvider.translate(m.name()));
        binder.forField(monthComboBox)
                .bind(DebtViewItem::getMonths, DebtViewItem::setMonths);
        monthsColumn.setEditorComponent(monthComboBox);

        final var previousPaymentAmountField = new BigDecimalField();
        binder.forField(previousPaymentAmountField)
                .bind(DebtViewItem::getPreviousPaymentAmount, DebtViewItem::setPreviousPaymentAmount);
        previousPaymentAmountColumn.setEditorComponent(previousPaymentAmountField);
        final var previousPaymentAmountCurrencyComboBox = ViewUtil.currencyComboBox();
        binder.forField(previousPaymentAmountCurrencyComboBox)
                .bind(DebtViewItem::getPreviousPaymentAmountCurrency, DebtViewItem::setPreviousPaymentAmountCurrency);
        previousPaymentAmountCurrencyColumn.setEditorComponent(previousPaymentAmountCurrencyComboBox);
*/

        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(event -> editEntity(event.getValue()));
    }

    private void configureForm() {
        form.setWidth("25em");
        form.setHeightFull();

        form.addListener(DebtForm.SaveEvent.class, event -> {

            final var item = event.getObj();
            final var indexOf = items.indexOf(item);
            if (indexOf > -1) {
                items.set(indexOf, item);
            } else {
                items.add(item);
            }

            setItems(items);
        });

        form.addListener(DebtForm.DeleteEvent.class, event -> resetItem(event.getObj()));
        form.addListener(DebtForm.CloseEvent.class, e -> closeEditor());
    }

    public void editEntity(DebtViewItem item) {
        if (item == null) {
            closeEditor();
        } else {
            form.setItem(item);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void resetItem(DebtViewItem item) {
        item.setAmount(BigDecimal.ZERO);
        item.setReceipts(0);
        item.setMonths(Collections.emptySet());
        item.setPreviousPaymentAmount(BigDecimal.ZERO);
        item.setPreviousPaymentAmountCurrency(null);

        form.setItem(form.defaultItem());
        setItems(items);
    }

    private void closeEditor() {
        form.setItem(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    public List<DebtViewItem> list() {
        return items;
    }
}
