package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import kyo.yaz.condominium.manager.core.service.ApartmentService;
import kyo.yaz.condominium.manager.core.service.BuildingService;
import kyo.yaz.condominium.manager.core.service.ReceiptService;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.repository.RateBlockingRepository;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.domain.DebtViewItem;
import kyo.yaz.condominium.manager.ui.views.domain.ExpenseViewItem;
import kyo.yaz.condominium.manager.ui.views.domain.ExtraChargeViewItem;
import kyo.yaz.condominium.manager.ui.views.form.DebtForm;
import kyo.yaz.condominium.manager.ui.views.form.ExpenseForm;
import kyo.yaz.condominium.manager.ui.views.form.ExtraChargeForm;
import kyo.yaz.condominium.manager.ui.views.form.ReceiptForm;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@PageTitle(ReceiptView.PAGE_TITLE)
@Route(value = "receipts/:receipt_id", layout = MainLayout.class)
public class EditReceiptView extends VerticalLayout implements BeforeEnterObserver, AbstractView {


    private final Text idField = new Text(Labels.Receipt.ID_LABEL);
    private final Grid<ExpenseViewItem> expenseGrid = new Grid<>(ExpenseViewItem.class, false);
    private final Grid<DebtViewItem> debtGrid = new Grid<>(DebtViewItem.class, false);
    private final Text createdAtField = new Text(Labels.Receipt.CREATED_AT_LABEL);

    private ExpenseForm expenseForm;
    private DebtForm debtForm;
    private Long receiptId;

    private final Button saveBtn = new Button(Labels.SAVE);

    private final Button cancelBtn = new Button(Labels.CANCEL);

    private final Set<ExpenseViewItem> expenses = new HashSet<>();
    private final Set<DebtViewItem> debts = new HashSet<>();



    private ExtraChargeForm extraChargeForm;
    private final Grid<ExtraChargeViewItem> extraChargeGrid = new Grid<>(ExtraChargeViewItem.class, false);


    private final Set<ExtraChargeViewItem> extraCharges = new HashSet<>();

    private final ApartmentService apartmentService;
    private final ReceiptService receiptService;
    private final BuildingService buildingService;
    private final RateBlockingRepository rateRepository;

    private ReceiptForm receiptForm;

    @Autowired
    public EditReceiptView(ApartmentService apartmentService, ReceiptService receiptService, BuildingService buildingService, RateBlockingRepository rateRepository) {
        super();
        this.apartmentService = apartmentService;
        this.receiptService = receiptService;
        this.buildingService = buildingService;
        this.rateRepository = rateRepository;
        init();
    }

    private void init() {
        addClassName("edit-receipt-view");
        setSizeFull();

        add(getContent(), createButtonsLayout());
        configureGrids();
        configureListeners();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initData();
    }

    private HorizontalLayout createButtonsLayout() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveBtn.addClickShortcut(Key.ENTER);
        cancelBtn.addClickShortcut(Key.ESCAPE);

        receiptForm.binder().addStatusChangeListener(e -> saveBtn.setEnabled(receiptForm.binder().isValid() && !expenses.isEmpty()));

        receiptForm.addListener(ReceiptForm.SaveEvent.class, event -> {
            final var formItem = event.getObj();

            final var build = Receipt.builder()
                    .buildingId(formItem.getBuildingId())
                    .year(formItem.getYear())
                    .month(formItem.getMonth())
                    .date(formItem.getDate())
                    .expenses(ConvertUtil.toList(expenses, ConvertUtil::expense))
                    .debts(ConvertUtil.toList(debts, ConvertUtil::debt))
                    .rate(formItem.getRate())
                    .build();

            receiptService.save(build)
                    .subscribeOn(Schedulers.parallel())
                    .subscribe(receipt -> {
                        logger().info("Navigating");
                        uiAsyncAction(this::navigateBack);

                    }, this::showError);
        });

        saveBtn.addClickListener(event -> receiptForm.validateAndSave());

        cancelBtn.addClickListener(event -> navigateBack());

        return new HorizontalLayout(saveBtn, cancelBtn);
    }

    private void navigateBack() {
        ui(ui -> ui.navigate(ReceiptView.class));
    }

    private Component receiptInfo() {
        receiptForm = new ReceiptForm(rateRepository);

        receiptForm.buildingComboBox().addValueChangeListener(event -> {

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

        return new VerticalLayout(new HorizontalLayout(idField, createdAtField), receiptForm,
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

        configureExtraChargeGridGrid();
    }

    private void configureExtraChargeGridGrid() {
        extraChargeGrid.addClassNames("extra_charge-grid");
        extraChargeGrid.setColumnReorderingAllowed(true);
        extraChargeGrid.addColumn(ExtraChargeViewItem::getAptNumber).setHeader(Labels.ExtraCharge.APT_LABEL).setSortable(true).setKey(Labels.ExtraCharge.APT_LABEL);
        extraChargeGrid.addColumn(ExtraChargeViewItem::getDescription).setHeader(Labels.ExtraCharge.DESCRIPTION_LABEL);
        extraChargeGrid.addColumn(ExtraChargeViewItem::getAmount).setHeader(Labels.ExtraCharge.AMOUNT_LABEL).setSortable(true).setKey(Labels.ExtraCharge.AMOUNT_LABEL);
        extraChargeGrid.addColumn(ExtraChargeViewItem::getCurrency).setHeader(Labels.ExtraCharge.CURRENCY_LABEL).setSortable(true).setKey(Labels.ExtraCharge.CURRENCY_LABEL);


        extraChargeGrid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> this.removeExtraCharge(item));
                            button.setIcon(new Icon(VaadinIcon.TRASH));
                        }))
                .setHeader(Labels.DELETE)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        extraChargeGrid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_SUCCESS,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> {
                                final var newItem = item.toBuilder().build();
                                extraChargeForm.setExtraCharge(newItem);
                            });
                            button.setIcon(new Icon(VaadinIcon.TRASH));
                        }))
                .setHeader(Labels.COPY)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        extraChargeGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        extraChargeGrid.addSelectionListener(selection -> {

            selection.getFirstSelectedItem()
                    .ifPresent(extraChargeForm::setExtraCharge);
        });

        setExtraChargesInGrid();
    }

    private void removeExtraCharge(ExtraChargeViewItem item) {
        extraCharges.remove(item);
        setExtraChargesInGrid();
    }

    private void setExtraChargesInGrid() {

        uiAsyncAction(() -> {
            extraChargeGrid.setItems(extraCharges);
            extraChargeGrid.getDataProvider().refreshAll();
        });
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
        uiAsyncAction(() -> {
            saveBtn.setEnabled(receiptForm.binder().isValid() && !expenses.isEmpty());
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
            final var buildingId = receiptForm.buildingComboBox().getValue();
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

    private void initData() {

        final var receiptMono = Mono.justOrEmpty(receiptId)
                .flatMap(receiptService::find)
                .map(ConvertUtil::formItem)
                .map(item -> (Runnable) () -> {
                    receiptForm.setItem(item);
                    receiptForm.disable();
                })
                .defaultIfEmpty(() -> {
                });

        final var setBuildingId = Mono.justOrEmpty(receiptId)
                .map(Object::toString)
                .map(id -> (Runnable) () -> receiptForm.buildingComboBox().setValue(id))
                .switchIfEmpty(buildingService.buildingIds()
                        .map(buildingIds -> () -> receiptForm.buildingComboBox().setItems(buildingIds)));

        Mono.zip(receiptMono, setBuildingId)
                .doOnSuccess(t -> uiAsyncAction(t.getT1(), t.getT2()))
                .ignoreElement()
                .and(Mono.empty())
                .subscribeOn(Schedulers.parallel())
                .subscribe(emptySubscriber());
    }
}

