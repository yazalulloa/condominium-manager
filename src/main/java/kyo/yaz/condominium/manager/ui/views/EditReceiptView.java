package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import kyo.yaz.condominium.manager.core.service.ApartmentService;
import kyo.yaz.condominium.manager.core.service.BuildingService;
import kyo.yaz.condominium.manager.core.service.ReceiptService;
import kyo.yaz.condominium.manager.core.service.SequenceService;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import kyo.yaz.condominium.manager.persistence.repository.RateBlockingRepository;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.domain.DebtViewItem;
import kyo.yaz.condominium.manager.ui.views.domain.ExpenseViewItem;
import kyo.yaz.condominium.manager.ui.views.form.DebtForm;
import kyo.yaz.condominium.manager.ui.views.form.ExpenseForm;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Month;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@PageTitle(ReceiptView.PAGE_TITLE)
@Route(value = "receipts/:receipt_id/edit", layout = MainLayout.class)
public class EditReceiptView extends VerticalLayout implements BeforeEnterObserver, AbstractView {

    private final ComboBox<String> buildingComboBox = new ComboBox<>(Labels.Receipt.BUILDING_LABEL);
    private final Text idField = new Text(Labels.Receipt.ID_LABEL);
    private final Grid<ExpenseViewItem> expenseGrid = new Grid<>(ExpenseViewItem.class, false);
    private final Grid<DebtViewItem> debtGrid = new Grid<>(DebtViewItem.class, false);
    private final Text createdAtField = new Text(Labels.Receipt.CREATED_AT_LABEL);
    private final ComboBox<Integer> yearPicker = ViewUtil.yearPicker();
    private final ComboBox<Month> monthPicker = ViewUtil.monthPicker();

    private final ComboBox<Rate> rateComboBox = new ComboBox<>(Labels.Receipt.RATE_LABEL);
    private final DatePicker datePicker = ViewUtil.datePicker(Labels.Receipt.RECEIPT_DATE_LABEL);
    private ExpenseForm expenseForm;
    private DebtForm debtForm;
    private Long receiptId;

    private final Button saveBtn = new Button(Labels.SAVE);

    private final Button cancelBtn = new Button(Labels.CANCEL);

    private final Set<ExpenseViewItem> expenses = new HashSet<>();
    private final Set<DebtViewItem> debts = new HashSet<>();
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private ApartmentService apartmentService;
    @Autowired
    private ReceiptService receiptService;
    @Autowired
    private SequenceService sequenceService;
    @Autowired
    private RateBlockingRepository rateRepository;

    public EditReceiptView() {
        super();
        init();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initData();
    }

    private void init() {
        addClassName("edit-receipt-view");
        setSizeFull();

        add(getContent(), createButtonsLayout());
        configureGrids();
        configureListeners();
        //checkForVisibility();
    }

    private HorizontalLayout createButtonsLayout() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveBtn.addClickShortcut(Key.ENTER);
        cancelBtn.addClickShortcut(Key.ESCAPE);

        saveBtn.setEnabled(false);
        saveBtn.addClickListener(event -> {

            saveBtn.setEnabled(false);
            final var buildingId = buildingComboBox.getValue();
            final var year = yearPicker.getValue();
            final var month = monthPicker.getValue();
            final var date = datePicker.getValue();

            Optional.ofNullable(receiptId)
                    .map(Mono::just)
                    .orElseGet(() -> sequenceService.nextSequence(Sequence.Type.RECEIPT))
                    .map(id -> {
                        final var rate = rateComboBox.getValue();
                        return Receipt.builder()
                                .id(id)
                                .buildingId(buildingId)
                                .year(year)
                                .month(month)
                                .date(date)
                                .expenses(ConvertUtil.toList(expenses, ConvertUtil::expense))
                                .debts(ConvertUtil.toList(debts, ConvertUtil::debt))
                                .createdAt(receiptId == null ? ZonedDateTime.now() : null)
                                .updatedAt(receiptId != null ? ZonedDateTime.now() : null)
                                .rateId(rate.id())
                                .rate(rate.rate())
                                .roundedRate(rate.roundedRate())
                                .source(rate.source())
                                .dateOfRate(rate.dateOfRate())
                                .build();
                    })
                    .flatMap(receiptService::save)
                    //.doOnSuccess(r -> ui(ui -> ui.navigate(ReceiptView.class)))
                    //.map(receipt -> (Runnable) () -> ui(ui -> ui.navigate(ReceiptView.class)))
                    //.doOnSuccess(this::uiAsyncAction)
                    .subscribeOn(Schedulers.parallel())
                    .subscribe(receipt -> {
                        logger().info("Navigating");
                        uiAsyncAction(() -> ui(ui -> ui.navigate(ReceiptView.class)));

                    }, this::showError);

        });

        cancelBtn.addClickListener(event -> {
            ui(ui -> ui.navigate(ReceiptView.class));
            //binder.readBean(null);
        });


        //binder.addStatusChangeListener(e -> saveBtn.setEnabled(binder.isValid()));

        return new HorizontalLayout(saveBtn, cancelBtn);
    }

    private void checkForVisibility() {

        final var editReceipt = receiptId != null;

        idField.setVisible(editReceipt);
        createdAtField.setVisible(editReceipt);

    }

    private void enableSaveBtn() {
        final var bool = buildingComboBox.getValue() != null
                && !expenses.isEmpty()
                && datePicker.getValue() != null
                && rateComboBox.getValue() != null;

        saveBtn.setEnabled(bool);
    }

    private Component receiptInfo() {
        buildingComboBox.setAllowCustomValue(false);
        buildingComboBox.addValueChangeListener(event -> {
            enableSaveBtn();

            final var value = event.getValue();

            if (value == null) {
                debtForm.setVisible(false);
                debtForm.clearAptNumbers();
            } else {
                apartmentService.aptNumbers(value)
                        .map(list -> (Runnable) () -> {
                            debtForm.setAptNumbers(value, list);
                            debtForm.setVisible(true);
                        })
                        .doOnSuccess(this::uiAsyncAction)
                        .ignoreElement()
                        .and(Mono.empty())
                        .subscribeOn(Schedulers.parallel())
                        .subscribe(this.emptySubscriber());
            }

        });

        datePicker.addValueChangeListener(e -> {
            enableSaveBtn();
        });

        final DataProvider<Rate, String> dataProvider = DataProvider.fromFilteringCallbacks(query -> {
            int offset = query.getOffset();

            // The number of items to load
            int limit = query.getLimit();

            return rateRepository.findAll(PageRequest.of(offset, limit))
                    .stream();

        }, query -> (int) rateRepository.count());

        rateComboBox.setItems(dataProvider);

        rateComboBox.setItemLabelGenerator(rate -> rate.rate() + " " + rate.dateOfRate() + " " + rate.source() + " " + rate.fromCurrency());

        return new VerticalLayout(new HorizontalLayout(idField, createdAtField), new HorizontalLayout(buildingComboBox, yearPicker, monthPicker, datePicker, rateComboBox),
                new H3(Labels.EXPENSES),
                expenseGrid, new H3(Labels.DEBTS), debtGrid);
    }

    private void configureGrids() {
        expenseGrid.addClassNames("expenses-grid");
        expenseGrid.setColumnReorderingAllowed(true);
        expenseGrid.addColumn(ExpenseViewItem::getDescription).setHeader(Labels.Expense.DESCRIPTION_LABEL);
        expenseGrid.addColumn(ExpenseViewItem::getAmount).setHeader(Labels.Expense.AMOUNT_LABEL).setSortable(true).setKey(Labels.Expense.AMOUNT_LABEL);
        expenseGrid.addColumn(ExpenseViewItem::getCurrency).setHeader(Labels.Expense.CURRENCY_LABEL).setSortable(true).setKey(Labels.Expense.CURRENCY_LABEL);
        expenseGrid.addColumn(ExpenseViewItem::getType).setHeader(Labels.Expense.TYPE_LABEL).setSortable(true).setKey(Labels.Expense.TYPE_LABEL);

        expenseGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        expenseGrid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, expense) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> this.removeExpense(expense));
                            button.setIcon(new Icon(VaadinIcon.TRASH));
                        }))
                .setHeader(Labels.DELETE);

        debtGrid.addClassNames("expenses-grid");
        debtGrid.setColumnReorderingAllowed(true);
        debtGrid.addColumn(DebtViewItem::getAptNumber).setHeader(Labels.Debt.APT_NUMBER_LABEL).setSortable(true).setKey(Labels.Debt.APT_NUMBER_LABEL);
        debtGrid.addColumn(DebtViewItem::getName).setHeader(Labels.Debt.NAME_LABEL).setSortable(true).setKey(Labels.Debt.NAME_LABEL);
        debtGrid.addColumn(DebtViewItem::getReceipts).setHeader(Labels.Debt.RECEIPT_LABEL).setSortable(true).setKey(Labels.Debt.RECEIPT_LABEL);
        debtGrid.addColumn(DebtViewItem::getAmount).setHeader(Labels.Debt.AMOUNT_LABEL).setSortable(true).setKey(Labels.Debt.AMOUNT_LABEL);
        debtGrid.addColumn(DebtViewItem::getCurrency).setHeader(Labels.Debt.CURRENCY_LABEL).setSortable(true).setKey(Labels.Debt.CURRENCY_LABEL);
        debtGrid.addColumn(DebtViewItem::getMonths).setHeader(Labels.Debt.MONTHS_LABEL);
        debtGrid.addColumn(DebtViewItem::getPreviousPaymentAmount).setHeader(Labels.Debt.PREVIOUS_AMOUNT_PAYED_LABEL).setSortable(true).setKey(Labels.Debt.PREVIOUS_AMOUNT_PAYED_LABEL);
        debtGrid.addColumn(DebtViewItem::getPreviousPaymentAmountCurrency).setHeader(Labels.Debt.PREVIOUS_AMOUNT_CURRENCY_PAYED_LABEL).setSortable(true).setKey(Labels.Debt.PREVIOUS_AMOUNT_CURRENCY_PAYED_LABEL);

        debtGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        debtGrid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, debt) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> this.removeDebt(debt));
                            button.setIcon(new Icon(VaadinIcon.TRASH));
                        }))
                .setHeader(Labels.DELETE);
    }

    private void removeExpense(ExpenseViewItem expense) {
        expenses.remove(expense);
        setExpensesInGrid();
    }

    private void removeDebt(DebtViewItem debt) {
        debts.remove(debt);
        setDebtsInGrid();
    }

    private void setExpensesInGrid() {
        enableSaveBtn();
        uiAsyncAction(() -> {
            expenseGrid.setItems(expenses);
            expenseGrid.getDataProvider().refreshAll();
        });
    }

    private void setDebtsInGrid() {
        uiAsyncAction(() -> {
            debtGrid.setItems(debts);
            debtGrid.getDataProvider().refreshAll();
        });
    }

    private VerticalLayout forms() {
        expenseForm = new ExpenseForm();
        debtForm = new DebtForm(aptNumber -> {
            final var buildingId = buildingComboBox.getValue();
            return apartmentService.read(buildingId, aptNumber).map(Apartment::name);
        });
        debtForm.setVisible(false);
        return new VerticalLayout(new H3(Labels.NEW_EXPENSE), expenseForm, new H3(Labels.NEW_DEBT), debtForm);
    }

    private Component getContent() {
        final var receiptInfo = receiptInfo();
        final var forms = forms();
        forms.setWidth(30, Unit.PERCENTAGE);
        HorizontalLayout content = new HorizontalLayout(receiptInfo, forms);
        content.setFlexGrow(2, receiptInfo);
        content.setFlexGrow(1, forms);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        receiptId = event.getRouteParameters().get("receipt_id")
                .map(s -> {
                    try {
                        return Long.parseLong(s);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    private void initData() {

        final var setBuildingsIds = buildingService.buildingIds()
                .map(buildingIds -> (Runnable) () -> {
                    buildingComboBox.setItems(buildingIds);
                });

        final var list = List.of(setBuildingsIds);

        Flux.fromIterable(list)
                .flatMap(m -> m)
                .collectList()
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .and(Mono.empty())
                .subscribeOn(Schedulers.parallel())
                .subscribe(emptySubscriber());
    }

    private void configureListeners() {
        expenseForm.addListener(ExpenseForm.SaveEvent.class, event -> {

            expenses.add(event.getObj());
            setExpensesInGrid();
        });

        debtForm.addListener(DebtForm.SaveEvent.class, event -> {

            debts.add(event.getObj());
            setDebtsInGrid();
        });

    }

    @Override
    public Component component() {
        return this;
    }

    @Override
    public Logger logger() {
        return log;
    }
}

