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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
import kyo.yaz.condominium.manager.core.service.entity.SaveReceipt;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.ScrollPanel;
import kyo.yaz.condominium.manager.ui.views.domain.DebtViewItem;
import kyo.yaz.condominium.manager.ui.views.extracharges.ExtraChargesView;
import kyo.yaz.condominium.manager.ui.views.util.AppUtil;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle(ReceiptView.PAGE_TITLE)
@PermitAll
@Route(value = "receipts/:receipt_id", layout = MainLayout.class)
public class EditReceiptView extends ScrollPanel implements BeforeEnterObserver {


    private final ExpensesView expensesView;
    private final ExtraChargesView extraChargesView = new ExtraChargesView();

    private final Div reserveFundsDiv = new Div();
    private final ApartmentService apartmentService;
    private final ReceiptService receiptService;
    private final BuildingService buildingService;
    private final SaveReceipt saveReceipt;

    private Long receiptId;
    private final ReceiptForm receiptForm;
    private Receipt receipt;
    private Building building;

    private final ReceiptDebtsView receiptDebtsView;

    @Autowired
    public EditReceiptView(ExpensesView expensesView, ApartmentService apartmentService, ReceiptService receiptService, BuildingService buildingService,
                           SaveReceipt saveReceipt, ReceiptForm receiptForm, ReceiptDebtsView receiptDebtsView) {
        super();
        this.expensesView = expensesView;
        this.apartmentService = apartmentService;
        this.receiptService = receiptService;
        this.buildingService = buildingService;
        this.saveReceipt = saveReceipt;
        this.receiptForm = receiptForm;
        this.receiptDebtsView = receiptDebtsView;
    }

    private void init() {
        addClassName("edit-receipt-view");
        setSizeFull();
        receiptForm.init();
        receiptDebtsView.init();
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

        receiptForm.binder().addStatusChangeListener(e -> saveBtn.setEnabled(receiptForm.binder().isValid() && !expensesView.items().isEmpty()));

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
                    .debts(ConvertUtil.toList(receiptDebtsView.list(), DebtMapper::to))
                    .extraCharges(ConvertUtil.toList(extraChargesView.items(), ExtraChargeMapper::to))
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
                    receiptDebtsView.setVisible(false);
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
        receiptDebtsView.setVisible(false);

        final var debtsLayout = new VerticalLayout(new H3(Labels.DEBTS), receiptDebtsView);

        add(receiptForm, createButtonsLayout(), reserveFundsDiv, expensesView, new Hr(), debtsLayout, new Hr(), extraChargesView, new Hr());
    }

    private void navigateBack() {
        navigate(ReceiptView.class);
    }

    private Single<Runnable> setAptNumbers(String buildingId) {
        final var buildingSingle = buildingService.get(buildingId);
        final var listSingle = apartmentService.apartmentsByBuilding(buildingId);

        return Single.zip(buildingSingle, listSingle, (building, list) -> {
            return () -> {
                this.building = building;
                final var aptNumbers = list.stream().map(Apartment::apartmentId)
                        .map(Apartment.ApartmentId::number)
                        .collect(Collectors.toCollection(LinkedList::new));

                final var debtList = Optional.ofNullable(receipt)
                        .map(Receipt::debts)
                        .orElseGet(Collections::emptyList);

                final var debtViewItems = list.stream()
                        .map(apartment -> {

                            final var debtViewItem = debtList.stream().filter(debt -> debt.aptNumber().equals(apartment.apartmentId().number()))
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

                receiptDebtsView.setItems(debtViewItems);
                receiptDebtsView.setVisible(true);
                extraChargesView.setApartments(aptNumbers);
                extraChargesView.setVisible(true);
                loadReserveFunds();
            };
        });
    }

    private void loadReserveFunds() {
        Optional.ofNullable(building)
                .map(Building::reserveFunds)
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(reserveFunds -> {
                    reserveFundsDiv.setVisible(true);
                    reserveFundsDiv.removeAll();

                    reserveFunds.forEach(reserveFund -> {
                        if (reserveFund.active() && DecimalUtil.greaterThanZero(reserveFund.percentage())) {
                            final var percentageToPay = DecimalUtil.greaterThanZero(expensesView.totalCommon()) ? DecimalUtil.percentageOf(reserveFund.percentage(), expensesView.totalCommon()) : expensesView.totalCommon();
                            reserveFundsDiv.add(new Paragraph(reserveFund.name() + ": " + reserveFund.fund() + " Porcentaje: " + reserveFund.percentage() + "% Monto a pagar: " + percentageToPay));
                        }

                    });

                    new Paragraph();

                });

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

                        receiptDebtsView.setItems(ConvertUtil.toList(receipt.debts(), DebtMapper::to));
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

        Single.zip(setBuildingIdSingle, receiptSingle, (setBuildingId, setReceipt) -> List.of(this::init, setBuildingId, setReceipt, this::loadReserveFunds))
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }
}

