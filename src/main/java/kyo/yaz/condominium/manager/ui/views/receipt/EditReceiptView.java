package kyo.yaz.condominium.manager.ui.views.receipt;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.security.PermitAll;
import kyo.yaz.condominium.manager.core.mapper.DebtMapper;
import kyo.yaz.condominium.manager.core.mapper.ExpenseMapper;
import kyo.yaz.condominium.manager.core.mapper.ExtraChargeMapper;
import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.service.entity.ReceiptService;
import kyo.yaz.condominium.manager.core.service.entity.CalculateReceiptInfo;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.persistence.domain.Expense.Type;
import kyo.yaz.condominium.manager.persistence.domain.ReserveFund;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.ScrollPanel;
import kyo.yaz.condominium.manager.ui.views.extracharges.ExtraChargesView;
import kyo.yaz.condominium.manager.ui.views.receipt.debts.DebtViewItem;
import kyo.yaz.condominium.manager.ui.views.receipt.debts.DebtsView;
import kyo.yaz.condominium.manager.ui.views.receipt.expenses.ExpenseForm;
import kyo.yaz.condominium.manager.ui.views.receipt.expenses.ExpensesView;
import kyo.yaz.condominium.manager.ui.views.util.AppUtil;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@PageTitle(ReceiptView.PAGE_TITLE)
@PermitAll
@Route(value = "receipts/:receipt_id", layout = MainLayout.class)
public class EditReceiptView extends ScrollPanel implements BeforeEnterObserver {


    private final ExpensesView expensesView;
    private final ExtraChargesView extraChargesView = new ExtraChargesView();

    private final Paragraph commonExpensesTotal = new Paragraph();
    private final Paragraph unCommonExpensesTotal = new Paragraph();
    private final Paragraph commonExpensesTotalPlusReserveFund = new Paragraph();
    private final Paragraph unCommonExpensesTotalPlusReserveFund = new Paragraph();
    private final Div reserveFundsDiv = new Div();
    private final ApartmentService apartmentService;
    private final ReceiptService receiptService;
    private final BuildingService buildingService;
    private final CalculateReceiptInfo calculateReceiptInfo;

    private Long receiptId;
    private final ReceiptForm receiptForm;
    private Receipt receipt;
    private Building building;

    private final DebtsView debtsView;

    @Autowired
    public EditReceiptView(ExpensesView expensesView, ApartmentService apartmentService, ReceiptService receiptService,
                           BuildingService buildingService,
                           CalculateReceiptInfo calculateReceiptInfo, ReceiptForm receiptForm, DebtsView debtsView) {
        super();
        this.expensesView = expensesView;
        this.apartmentService = apartmentService;
        this.receiptService = receiptService;
        this.buildingService = buildingService;
        this.calculateReceiptInfo = calculateReceiptInfo;
        this.receiptForm = receiptForm;
        this.debtsView = debtsView;
    }

    private void init() {
        addClassName("edit-receipt-view");
        receiptForm.init();
        debtsView.init();
        extraChargesView.init();
        expensesView.init();
        addContent();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initData();
    }

    private HorizontalLayout createButtonsLayout() {

        final var saveBtn = new Button(Labels.SAVE);
        final var cancelBtn = new Button(Labels.CANCEL);

        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveBtn.addClickShortcut(Key.ENTER);
        cancelBtn.addClickShortcut(Key.ESCAPE);

        receiptForm.binder().addStatusChangeListener(
                e -> saveBtn.setEnabled(receiptForm.binder().isValid() && !expensesView.items().isEmpty()));

        receiptForm.addListener(ReceiptForm.SaveEvent.class, event -> {
            final var formItem = event.getObj();

            final var build = Optional.ofNullable(receipt)
                    .map(Receipt::toBuilder)
                    .orElseGet(Receipt::builder)
                    .buildingId(formItem.getBuildingId())
                    .year(formItem.getYear())
                    .month(formItem.getMonth())
                    .date(formItem.getDate())
                    .expenses(ConvertUtil.toList(expensesView.items(), ExpenseMapper::to))
                    //.debts(ConvertUtil.toList(debts, DebtMapper::to))
                    .debts(ConvertUtil.toList(debtsView.debts(), DebtMapper::to))
                    .extraCharges(ConvertUtil.toList(extraChargesView.items(), ExtraChargeMapper::to))
                    .rate(formItem.getRate())
                    .build();

            calculateReceiptInfo.save(build)
                    .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                    .subscribe(singleObserver(receipt -> {
                        logger().info("Navigating");
                        uiAsyncAction(this::navigateBack);

                    }, this::showError));
        });

        saveBtn.addClickListener(event -> receiptForm.validateAndSave());

        cancelBtn.addClickListener(event -> navigateBack());

        expensesView.form().addListener(ExpenseForm.SaveEvent.class, event -> {
            saveBtn.setEnabled(receiptForm.binder().isValid() && !expensesView.items().isEmpty());
        });

        expensesView.addListener(ExpensesView.LoadExpensesEvent.class, event -> loadReserveFunds());

        return new HorizontalLayout(saveBtn, cancelBtn);
    }

    private void addContent() {

        receiptForm.buildingComboBox().addValueChangeListener(event -> {
            if (receiptId == null) {
                final var value = event.getValue();

                if (value == null) {
                    debtsView.setVisible(false);
                    extraChargesView.setVisible(false);
                } else {
                    setAptNumbers(value)
                            .doOnSuccess(this::uiAsyncAction)
                            .ignoreElement()
                            .subscribeOn(Schedulers.io())
                            .subscribe(completableObserver());
                }
            }
        });

        extraChargesView.setVisible(false);
        debtsView.setVisible(false);


        add(receiptForm, createButtonsLayout(), new H3(Labels.TOTAL_EXPENSES), new Div(commonExpensesTotal, unCommonExpensesTotal,
                        commonExpensesTotalPlusReserveFund, unCommonExpensesTotalPlusReserveFund), new Hr(),
                new H3(Labels.RESERVE_FUNDS_TITLE), reserveFundsDiv, new Hr(), expensesView, new Hr(), debtsView, new Hr(),
                extraChargesView, new Hr(),
                createButtonsLayout());
    }

    private void navigateBack() {
        navigate(ReceiptView.class);
    }

    private Single<Runnable> setAptNumbers(String buildingId) {
        final var buildingSingle = buildingService.get(buildingId);
        final var listSingle = apartmentService.aptNumbers(buildingId);

        return Single.zip(buildingSingle, listSingle, (building, list) -> {
            return () -> {
                this.building = building;

                debtsView.setCurrency(building.debtCurrency());

                final var debtList = Optional.ofNullable(receipt)
                        .map(Receipt::debts)
                        .orElseGet(Collections::emptyList);

                final var debtViewItems = list.stream()
                        .map(apartment -> {

                            final var debtViewItem = debtList.stream()
                                    .filter(debt -> debt.aptNumber().equals(apartment.apartmentId().number()))
                                    .findFirst()
                                    .map(DebtMapper::to)
                                    .orElse(DebtViewItem.builder()
                                            .aptNumber(apartment.apartmentId().number())
                                            .build());

                            return debtViewItem.toBuilder()
                                    .name(apartment.name())
                                    .build();
                        })
                        .collect(Collectors.toCollection(LinkedList::new));

                debtsView.setItems(debtViewItems);
                debtsView.setVisible(true);
                extraChargesView.setApartments(list);
                extraChargesView.setVisible(true);
                loadReserveFunds();
            };
        });
    }

    private void loadReserveFunds() {
        final var fundList = Optional.ofNullable(building)
                .map(Building::reserveFunds)
                .filter(CollectionUtils::isNotEmpty);

        fundList.ifPresent(reserveFunds -> {
            reserveFundsDiv.setVisible(true);
            reserveFundsDiv.removeAll();

            reserveFunds.forEach(reserveFund -> {
                final var reserveFundPay = reserveFund.pay();
                if (reserveFund.active() && DecimalUtil.greaterThanZero(reserveFundPay)) {

                    final var isFixedPay = reserveFund.type() == ReserveFund.Type.FIXED_PAY;
                    final var total = expensesView.totalCommon();
                    final var amountToPay = isFixedPay ? reserveFundPay :
                            DecimalUtil.greaterThanZero(total) ? DecimalUtil.percentageOf(reserveFundPay,
                                    total) : total;

                    final var amountToPayStr = "Monto a pagar: " + amountToPay;
                    final var last = isFixedPay ? amountToPayStr : "Porcentaje: " + reserveFundPay + "% " + amountToPayStr;
                    reserveFundsDiv.add(new Paragraph(reserveFund.name() + ": " + reserveFund.fund() + " " + last));
                }

            });

        });

        final var totalUnCommon = new AtomicReference<>(expensesView.totalUnCommon());
        final var totalCommon = new AtomicReference<>(expensesView.totalCommon());

        fundList.stream()
                .flatMap(Collection::stream)
                .filter(reserveFund -> reserveFund.active() && DecimalUtil.greaterThanZero(reserveFund.pay())
                        && reserveFund.addToExpenses())
                .forEach(reserveFund -> {

                    final var total = expensesView.totalCommon();

                    final var pay = reserveFund.type() == ReserveFund.Type.FIXED_PAY ? reserveFund.pay()
                            : DecimalUtil.greaterThanZero(total) ? DecimalUtil.percentageOf(
                            reserveFund.pay(), total) : total;

                    if (reserveFund.expenseType() == Type.UNCOMMON) {
                        totalUnCommon.set(totalUnCommon.get().add(pay));

                    } else {
                        totalCommon.set(totalCommon.get().add(pay));
                    }
                });

        commonExpensesTotal.setText("Gastos comunes total: %s".formatted(expensesView.totalCommon()));
        unCommonExpensesTotal.setText("Gastos no comunes total: %s".formatted(expensesView.totalUnCommon()));
        commonExpensesTotalPlusReserveFund.setText(
                "Gastos comunes total + fondos de reserva: %s".formatted(totalCommon.get()));
        unCommonExpensesTotalPlusReserveFund.setText(
                "Gastos no comunes total + fondos de reserva: %s".formatted(totalUnCommon.get()));

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        receiptId = event.getRouteParameters().get("receipt_id")
                .map(s -> {

                    final var obj = VaadinSession.getCurrent().getAttribute("receipt");
                    if (obj instanceof Receipt) {
                        receipt = (Receipt) obj;
                    }
                    switch (s) {
                        case "new", "file", "copy" -> {
                            return null;
                        }
                        default -> {
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


    private Maybe<Runnable> initReceipt() {

        return Maybe.fromOptional(Optional.ofNullable(receipt).map(Receipt::buildingId))
                .flatMapSingle(this::setAptNumbers)
                .map(runnable -> {
                    return () -> {
                        //debtsView.setItems(ConvertUtil.toList(receipt.debts(), DebtMapper::to));
                        expensesView.setItems(ConvertUtil.toList(receipt.expenses(), ExpenseMapper::to));
                        extraChargesView.setItems(ConvertUtil.toList(receipt.extraCharges(), ExtraChargeMapper::to));

                        receiptForm.setItem(ConvertUtil.formItem(receipt));
                        receiptForm.buildingComboBox().setEnabled(receipt.createdAt() == null);
                        receiptForm.buildingComboBox().setValue(receipt.buildingId());

                        runnable.run();
                    };
                });
    }

    private void initData() {

        final var setBuildingIdSingle = buildingService.buildingIds()
                .map(buildingIds -> (Runnable) () -> receiptForm.buildingComboBox().setItems(buildingIds));

        final var loadFromId = Maybe.fromOptional(Optional.ofNullable(receiptId))
                .flatMap(receiptService::find)
                .flatMap(receipt -> {

                    EditReceiptView.this.receipt = receipt;
                    return initReceipt();
                });

        final var receiptSingle = initReceipt()
                .switchIfEmpty(loadFromId)
                .switchIfEmpty(Single.fromCallable(AppUtil::emptyRunnable));

        Single.zip(setBuildingIdSingle, receiptSingle,
                        (setBuildingId, setReceipt) -> List.of(this::init, setBuildingId, setReceipt, this::loadReserveFunds))
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }
}

