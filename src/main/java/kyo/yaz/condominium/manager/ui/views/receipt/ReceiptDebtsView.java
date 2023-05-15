package kyo.yaz.condominium.manager.ui.views.receipt;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.domain.DebtViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class ReceiptDebtsView extends BaseDiv {

    private List<DebtViewItem> list = Collections.emptyList();
    private final Grid<DebtViewItem> grid = new Grid<>();

    private final TranslationProvider translationProvider;

    @Autowired
    public ReceiptDebtsView(TranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
    }


    private String gridKeys() {
        return grid.getColumns().stream().map(Grid.Column::getKey)
                .collect(Collectors.joining(", "));
    }

    public void init() {

        setSizeFull();
        grid.addClassNames("debt-grid");
        grid.setAllRowsVisible(true);
        grid.setColumnReorderingAllowed(true);
        final var editor = grid.getEditor();

        grid.addColumn(item -> item.getAptNumber() + " " + item.getName())
                .setHeader(Labels.Debt.APT_LABEL)
                .setSortable(true)
                .setKey(Labels.Debt.APT_LABEL);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE)
                .addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(item -> {
                    if (editor.isOpen())
                        editor.cancel();
                    grid.getEditor().editItem(item);
                }));

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


        final var saveButton = new Button(VaadinIcon.CHECK.create(), e -> editor.save());

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


        grid.setSizeFull();
        add(grid);
    }

    public void setItems(List<DebtViewItem> debtViewItems) {
        list = debtViewItems;
        grid.setItems(list);
        grid.getDataProvider().refreshAll();
    }

    public List<DebtViewItem> list() {
        return list;
    }
}
