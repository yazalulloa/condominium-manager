package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
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
import com.vaadin.flow.server.VaadinSession;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kyo.yaz.condominium.manager.core.mapper.DebtMapper;
import kyo.yaz.condominium.manager.core.mapper.ExpenseMapper;
import kyo.yaz.condominium.manager.core.mapper.ExtraChargeMapper;
import kyo.yaz.condominium.manager.core.service.entity.*;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.domain.DebtViewItem;
import kyo.yaz.condominium.manager.ui.views.domain.ExpenseViewItem;
import kyo.yaz.condominium.manager.ui.views.domain.ExtraChargeViewItem;
import kyo.yaz.condominium.manager.ui.views.form.DebtForm;
import kyo.yaz.condominium.manager.ui.views.form.ExpenseForm;
import kyo.yaz.condominium.manager.ui.views.form.ExtraChargeForm;
import kyo.yaz.condominium.manager.ui.views.form.ReceiptForm;
import kyo.yaz.condominium.manager.ui.views.util.AppUtil;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@PageTitle(ReceiptView.PAGE_TITLE)
@Route(value = "receipts/:receipt_id", layout = MainLayout.class)
public class EditReceiptView extends BaseVerticalLayout implements BeforeEnterObserver {


    /* private final H4 idField = new H4();
     private final H4 createdAtField = new H4();*/
    private final Grid<ExpenseViewItem> expenseGrid = new Grid<>();
    private final Grid<DebtViewItem> debtGrid = new Grid<>();
    private final Grid<ExtraChargeViewItem> extraChargeGrid = new Grid<>();
    private final Button saveBtn = new Button(Labels.SAVE);
    private final Button cancelBtn = new Button(Labels.CANCEL);
    private final Set<ExpenseViewItem> expenses = new LinkedHashSet<>();
    private final Set<DebtViewItem> debts = new LinkedHashSet<>();
    private final ExtraChargeForm extraChargeForm = new ExtraChargeForm();
    private final Set<ExtraChargeViewItem> extraCharges = new LinkedHashSet<>();
    private final ApartmentService apartmentService;
    private final ReceiptService receiptService;
    private final BuildingService buildingService;
    private final RateService rateService;
    private final SaveReceipt saveReceipt;
    private ExpenseForm expenseForm;
    private DebtForm debtForm;
    private Long receiptId;
    private ReceiptForm receiptForm;
    private Receipt receipt;

    @Autowired
    public EditReceiptView(ApartmentService apartmentService, ReceiptService receiptService, BuildingService buildingService, RateService rateService, SaveReceipt saveReceipt) {
        super();
        this.apartmentService = apartmentService;
        this.receiptService = receiptService;
        this.buildingService = buildingService;
        this.rateService = rateService;
        this.saveReceipt = saveReceipt;
    }

    private void init() {
        addClassName("edit-receipt-view");
        setSizeFull();
        configureGrids();
        getContent();
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

            final var build = Optional.ofNullable(receipt)
                    .map(Receipt::toBuilder)
                    .orElseGet(Receipt::builder)
                    .buildingId(formItem.getBuildingId())
                    .year(formItem.getYear())
                    .month(formItem.getMonth())
                    .date(formItem.getDate())
                    .expenses(ConvertUtil.toList(expenses, ExpenseMapper::to))
                    .debts(ConvertUtil.toList(debts, DebtMapper::to))
                    .extraCharges(ConvertUtil.toList(extraCharges, ExtraChargeMapper::to))
                    .rate(formItem.getRate())
                    .build();

            saveReceipt.save(build)
                    .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                    .subscribe(singleObserver(receipt -> {
                        logger().info("Navigating");
                        uiAsyncAction(this::navigateBack);

                    }, this::showError));
        });

        saveBtn.addClickListener(event -> receiptForm.validateAndSave());

        cancelBtn.addClickListener(event -> navigateBack());

        return new HorizontalLayout(saveBtn, cancelBtn);
    }

    private void getContent() {

        receiptForm = new ReceiptForm(rateService);

        receiptForm.buildingComboBox().addValueChangeListener(event -> {
            if (receiptId == null) {
                final var value = event.getValue();

                if (value == null) {
                    debtForm.setVisible(false);
                    debtForm.clearAptNumbers();
                    extraChargeForm.setVisible(false);
                } else {
                    setAptNumbers(value)
                            .doOnSuccess(this::uiAsyncAction)
                            .ignoreElement()
                            .subscribeOn(Schedulers.io())
                            .subscribe(completableObserver());
                }
            }
        });

        expenseForm = new ExpenseForm();
        debtForm = new DebtForm(aptNumber -> {
            final var buildingId = receiptForm.buildingComboBox().getValue();
            return apartmentService.read(buildingId, aptNumber).map(Apartment::name);
        });
        debtForm.setVisible(false);
        extraChargeForm.setVisible(false);

        expenseForm.setWidth(25, Unit.PERCENTAGE);
        final var expensesLayout = new HorizontalLayout(new VerticalLayout(new H3(Labels.EXPENSES), expenseGrid), expenseForm);
        final var debtsLayout = new HorizontalLayout(new VerticalLayout(new H3(Labels.DEBTS), debtGrid), debtForm);
        debtForm.setWidth(25, Unit.PERCENTAGE);
        final var extraChargesLayout = new HorizontalLayout(new VerticalLayout(new H3(Labels.EXTRA_CHARGE_TITLE), extraChargeGrid), extraChargeForm);
        extraChargeForm.setWidth(25, Unit.PERCENTAGE);

        add(receiptForm, createButtonsLayout(), expensesLayout, debtsLayout, extraChargesLayout);

        setSizeFull();
    }

    private void navigateBack() {
        ui(ui -> ui.navigate(ReceiptView.class));
    }

    private Single<Runnable> setAptNumbers(String buildingId) {
        return apartmentService.aptNumbers(buildingId)
                .map(list -> () -> {
                    debtForm.setAptNumbers(list);
                    debtForm.setVisible(true);
                    extraChargeForm.setApartments(list);
                    extraChargeForm.setVisible(true);
                });
    }

    private void configureGrids() {
        expenseGrid.addClassNames("expenses-grid");
        expenseGrid.setAllRowsVisible(true);
        expenseGrid.setColumnReorderingAllowed(true);
        expenseGrid.addColumn(ExpenseViewItem::getDescription).setHeader(Labels.Expense.DESCRIPTION_LABEL);
        expenseGrid.addColumn(item -> ConvertUtil.format(item.getAmount(), item.getCurrency())).setHeader(Labels.Expense.AMOUNT_LABEL).setSortable(true).setKey(Labels.Expense.AMOUNT_LABEL);
        expenseGrid.addColumn(ExpenseViewItem::getType).setHeader(Labels.Expense.TYPE_LABEL).setSortable(true).setKey(Labels.Expense.TYPE_LABEL);

        expenseGrid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, expense) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> this.removeExpense(expense));
                            button.setIcon(new Icon(VaadinIcon.TRASH));
                        }))
                .setHeader(Labels.DELETE)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        expenseGrid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_SUCCESS,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> {
                                final var newItem = item.toBuilder().build();
                                expenseForm.setExpense(newItem);
                            });
                            button.setIcon(new Icon(VaadinIcon.COPY));
                        }))
                .setHeader(Labels.COPY)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        expenseGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        expenseGrid.setItems(expenses);
        expenseGrid.setSizeFull();

        expenseGrid.addSelectionListener(selection -> {

            selection.getFirstSelectedItem()
                    .ifPresent(expenseForm::setExpense);
        });

        debtGrid.addClassNames("debt-grid");
        debtGrid.setAllRowsVisible(true);
        debtGrid.setColumnReorderingAllowed(true);
        debtGrid.addColumn(item -> item.getAptNumber() + " " + item.getName()).setHeader(Labels.Debt.APT_LABEL).setSortable(true).setKey(Labels.Debt.APT_LABEL);
        debtGrid.addColumn(DebtViewItem::getReceipts).setHeader(Labels.Debt.RECEIPT_LABEL).setSortable(true).setKey(Labels.Debt.RECEIPT_LABEL);
        debtGrid.addColumn(DebtViewItem::getAmount).setHeader(Labels.Debt.AMOUNT_LABEL).setSortable(true).setKey(Labels.Debt.AMOUNT_LABEL);
        debtGrid.addColumn(item -> {

            return Optional.ofNullable(item.getMonths())
                    .orElseGet(Collections::emptySet)
                    .stream()
                    .map(Enum::name)
                    .collect(Collectors.joining("\n"));
        }).setHeader(Labels.Debt.MONTHS_LABEL);
        debtGrid.addColumn(DebtViewItem::getPreviousPaymentAmount).setHeader(Labels.Debt.PREVIOUS_AMOUNT_PAYED_LABEL).setSortable(true).setKey(Labels.Debt.PREVIOUS_AMOUNT_PAYED_LABEL);
        debtGrid.addColumn(DebtViewItem::getPreviousPaymentAmountCurrency).setHeader(Labels.Debt.PREVIOUS_AMOUNT_CURRENCY_PAYED_LABEL).setSortable(true).setKey(Labels.Debt.PREVIOUS_AMOUNT_CURRENCY_PAYED_LABEL);

        debtGrid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, debt) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> this.removeDebt(debt));
                            button.setIcon(new Icon(VaadinIcon.TRASH));
                        }))
                .setHeader(Labels.DELETE)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        debtGrid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_SUCCESS,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> {
                                final var newItem = item.toBuilder().build();
                                debtForm.setDebt(newItem);
                            });
                            button.setIcon(new Icon(VaadinIcon.COPY));
                        }))
                .setHeader(Labels.COPY)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        debtGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        debtGrid.setItems(debts);
        debtGrid.setSizeFull();

        debtGrid.addSelectionListener(selection -> {

            selection.getFirstSelectedItem()
                    .ifPresent(debtForm::setDebt);
        });

        configureExtraChargeGridGrid();
    }

    private void configureExtraChargeGridGrid() {
        extraChargeGrid.addClassNames("extra-charge-grid");
        extraChargeGrid.setAllRowsVisible(true);
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
                            button.setIcon(new Icon(VaadinIcon.COPY));
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

        extraChargeGrid.setItems(extraCharges);
        extraChargeGrid.setSizeFull();
    }

    private void removeExtraCharge(ExtraChargeViewItem item) {
        extraCharges.remove(item);
        setExtraChargesInGrid();
    }


    private void removeExpense(ExpenseViewItem expense) {
        expenses.remove(expense);
        setExpensesInGrid();
    }

    private void removeDebt(DebtViewItem debt) {
        debts.remove(debt);
        setDebtsInGrid();
    }

    private void setExtraChargesInGrid() {

        uiAsyncAction(() -> {
            extraChargeGrid.setItems(extraCharges);
            extraChargeGrid.getDataProvider().refreshAll();
        });
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

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        receiptId = event.getRouteParameters().get("receipt_id")
                .map(s -> {

                    switch (s) {
                        case "new": {
                            return null;
                        }
                        case "file", "copy": {
                            final var obj = VaadinSession.getCurrent().getAttribute("receipt");
                            if (obj instanceof Receipt) {
                                receipt = (Receipt) obj;
                            }
                        }
                        default: {
                            try {
                                return Long.parseLong(s);
                            } catch (Exception e) {
                                return null;
                            }
                        }
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

        extraChargeForm.addListener(ExtraChargeForm.SaveEvent.class, event -> {
            extraCharges.add(event.getObj());
            setExtraChargesInGrid();
        });
    }

    private Maybe<Runnable> initReceipt() {

        return Maybe.fromOptional(Optional.ofNullable(receipt).map(Receipt::buildingId))
                .flatMapSingle(this::setAptNumbers)
                .map(runnable -> {
                    return (Runnable) () -> {
                        debts.addAll(ConvertUtil.toList(receipt.debts(), DebtMapper::to));
                        expenses.addAll(ConvertUtil.toList(receipt.expenses(), ExpenseMapper::to));
                        extraCharges.addAll(ConvertUtil.toList(receipt.extraCharges(), ExtraChargeMapper::to));

                        setExpensesInGrid();
                        setExtraChargesInGrid();
                        setDebtsInGrid();

                        receiptForm.setItem(ConvertUtil.formItem(receipt));
                        receiptForm.buildingComboBox().setEnabled(receipt.createdAt() == null);
                        receiptForm.buildingComboBox().setValue(receipt.buildingId());

                        //idField.setText(Labels.Receipt.ID_LABEL + "" + receipt.id());
                        // createdAtField.setText(Labels.Receipt.CREATED_AT_LABEL + " " + receipt.createdAt().withZoneSameInstant(DateUtil.VE_ZONE));

                        runnable.run();
                    };
                });
    }

    private void initData() {


        final var setBuildingIdSingle = buildingService.buildingIds()
                .map(buildingIds -> (Runnable) () -> receiptForm.buildingComboBox().setItems(buildingIds));

        final var receiptSingle = Maybe.fromOptional(Optional.ofNullable(receiptId))
                .flatMap(receiptService::find)
                .flatMap(receipt -> {

                    EditReceiptView.this.receipt = receipt;
                    return initReceipt();
                })
                .switchIfEmpty(initReceipt())
                .switchIfEmpty(Single.fromCallable(AppUtil::emptyRunnable));

        Single.zip(setBuildingIdSingle, receiptSingle, (setBuildingId, setReceipt) -> List.of(this::init, setBuildingId, setReceipt))
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }
}

