package kyo.yaz.condominium.manager.ui.views.building;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.security.PermitAll;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.util.ObjectUtil;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.component.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.IconUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@PageTitle(BuildingView.PAGE_TITLE)
@PermitAll
@Route(value = "buildings", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class BuildingView extends BaseDiv {
    public static final String PAGE_TITLE = "Edificios";

    private final Grid<Building> grid = new Grid<>();
    private final Text countOfBuildingText = new Text(null);
    private final Button addBuildingButton = new Button("Añadir edificio");
    private final DeleteDialog deleteDialog = new DeleteDialog();

    private final BuildingService service;

    @Autowired
    public BuildingView(BuildingService service) {
        super();
        this.service = service;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        addClassName("building-view");
        setSizeFull();
        configureGrid();

        refreshListData()
                .doOnSuccess(r -> uiAsyncAction(this::init, r))
                .ignoreElement()
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }

    private void init() {
        final var layout = new VerticalLayout(getToolbar(), grid);
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private HorizontalLayout getToolbar() {

        addBuildingButton.setDisableOnClick(true);
        addBuildingButton.addClickListener(click -> editEntity());

        final var toolbar = new HorizontalLayout(addBuildingButton, countOfBuildingText);
        toolbar.addClassName("toolbar");
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return toolbar;
    }

    private void configureGrid() {
        grid.addClassNames("building-grid");
        // grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.addComponentColumn(this::createCard);


        grid.addItemDoubleClickListener(e -> editEntity(e.getItem().id()));
    }

    private Component createCard(Building building) {

        final var name = new Span(building.name());
        name.addClassName("name");

        final var id = new Span(building.id());
        id.addClassName("id");

        final var rif = new Span(building.rif());
        rif.addClassName("rif");


        final var header = new Div(name, id, rif);
        header.addClassName("header");

        final var deleteIcon = IconUtil.trash();
        deleteIcon.addClassName("icon");
        final var deleteBtn = new Button(deleteIcon);
        deleteBtn.addClickListener(v -> deleteDialog(building));

        final var editBuildingIcon = VaadinIcon.EDIT.create();
        editBuildingIcon.addClassName("icon");
        editBuildingIcon.setColor("#13b931");
        final var editBtn = new Button(editBuildingIcon);
        editBtn.addClickListener(v -> editEntity(building.id()));



        final var card = new Div();
        card.addClassName("card");
        final var buildingInfo = new Div(header, description(building));
        buildingInfo.addClassName("info");

        final var buttonLayout = new Div(deleteBtn, editBtn);
        buttonLayout.addClassName("buttons");

        card.add(buildingInfo, buttonLayout);

        return card;
    }

    private Component description(Building building) {
        final var mainCurrency = new Span(Labels.Building.MAIN_CURRENCY_LABEL + ": " + building.mainCurrency().name());
        mainCurrency.addClassName("main-currency");

        final var debtCurrency = new Span(Labels.Building.DEBT_CURRENCY_LABEL + ": " + building.debtCurrency().name());
        debtCurrency.addClassName("debt-currency");

        final var currenciesToShowAmountToPay = new Span(Labels.Building.SHOW_PAYMENT_IN_CURRENCIES + ": " + building.currenciesToShowAmountToPay().toString());
        currenciesToShowAmountToPay.addClassName("currencies-to-show-amount-to-pay");

        final var extraCharges = new Span("Cargos extra: " + building.extraCharges().size());
        extraCharges.addClassName("extra-charges");

        final var fixedPayText = new Span(Labels.Building.FIXED_PAY_LABEL + ": ");
        final Component component = building.fixedPay() ? new Span(ConvertUtil.format(building.fixedPayAmount(), building.mainCurrency())) : IconUtil.cross();

        final var fixedPay = new Span(fixedPayText, component);
        fixedPay.addClassName("fixed-pay");

        final var receiptEmailFrom = new Span(Labels.Building.RECEIPT_EMAIL_FROM_LABEL + ": " + building.emailConfig());
        receiptEmailFrom.addClassName("receipt-email-from");

        final var roundUpPaymentIcon = IconUtil.checkMarkOrCross(ObjectUtil.aBoolean(building.roundUpPayments()));

        final var roundUpPayments = new Span(new Span(Labels.Building.ROUND_UP_PAYMENTS_LABEL + ": "), roundUpPaymentIcon);
        roundUpPayments.addClassName("round-up-payments");


        final var amountOfApts = new Span(Labels.Building.AMOUNT_OF_APTS + ": " + building.amountOfApts());
        amountOfApts.addClassName("amount-of-apts");

        final var description = new Div(mainCurrency, debtCurrency, currenciesToShowAmountToPay, extraCharges, fixedPay, receiptEmailFrom, roundUpPayments, amountOfApts);
        description.addClassName("body");

        return description;
    }

    private void deleteDialog(Building building) {
        deleteDialog.setText(Labels.ASK_CONFIRMATION_DELETE_BUILDING.formatted(building.id()));
        deleteDialog.setDeleteAction(() -> delete(building));
        deleteDialog.open();
    }


    public void delete(Building building) {

        service.delete(building)
                .andThen(refreshData())
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }

    private Single<List<Building>> listOfBuildings() {
        return service.list(null);
    }

    private Single<Runnable> refreshListData() {
        return listOfBuildings()
                .map(list -> () -> {
                    logger().info("BUILDINGS: " + list.size());
                    countOfBuildingText.setText(String.format("Edificios: %d", list.size()));
                    grid.setItems(list);
                    grid.getDataProvider().refreshAll();
                });
    }

    private Completable refreshData() {

        return refreshListData()
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement();
    }


    private void editEntity() {
        editEntity("new");
    }

    private void editEntity(String id) {
        grid.asSingleSelect().clear();
        ui(ui -> ui.navigate(EditBuildingView.class, new RouteParameters("building_id", id)));
        //editEntity(Apartment.builder().build());
    }
}
