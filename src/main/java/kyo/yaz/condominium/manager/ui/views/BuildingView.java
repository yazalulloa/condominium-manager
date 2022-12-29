package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParameters;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.actions.DeleteEntity;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.domain.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;

@PageTitle(BuildingView.PAGE_TITLE)
@Route(value = "buildings", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class BuildingView extends VerticalLayout implements AbstractView, DeleteEntity<Building> {
    public static final String PAGE_TITLE = "Edificios";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Grid<Building> grid = new Grid<>();
    private final Text countOfBuildingText = new Text(null);
    private final Button addBuildingButton = new Button("Añadir edificio");
    private final DeleteDialog deleteDialog = new DeleteDialog();

    private final BuildingService service;

    @Autowired
    public BuildingView(BuildingService service) {
        super();
        this.service = service;
        init();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        refreshData()
                .subscribeOn(Schedulers.parallel())
                .subscribe(this.refreshGridSubscriber());
    }

    private void init() {
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        add(getToolbar(), getContent());
    }

    private Component getContent() {
        return grid;
    }

    private void configureGrid() {
        grid.addClassNames("buildings-grid");

        grid.addColumn(Building::id).setHeader(Labels.Building.ID_LABEL);
        grid.addColumn(Building::name).setHeader(Labels.Building.NAME_LABEL);
        grid.addColumn(Building::rif).setHeader(Labels.Building.RIF_LABEL);
        grid.addColumn(building -> ConvertUtil.format(building.reserveFund(), building.reserveFundCurrency())).setHeader(Labels.Building.RESERVE_FUND_LABEL);
        grid.addColumn(Building::mainCurrency).setHeader(Labels.Building.MAIN_CURRENCY_LABEL);
        grid.addColumn(Building::currenciesToShowAmountToPay).setHeader(Labels.Building.SHOW_PAYMENT_IN_CURRENCIES);
        grid.addColumn(building -> {

            final var fixedPay = Optional.ofNullable(building.fixedPay()).orElse(false);

            if (fixedPay) {

                return ConvertUtil.format(building.fixedPayAmount(), building.fixedPayCurrency());
            } else {
                return Labels.DEACTIVATED;
            }
        }).setHeader(Labels.Building.FIXED_PAY_LABEL);

        grid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> {

                                deleteDialog.setHeaderTitle(Labels.ASK_CONFIRMATION_DELETE_BUILDING.formatted(item.id()));
                                deleteDialog.setDeleteAction(() -> delete(item));
                                deleteDialog.open();
                            });
                            button.setIcon(new Icon(VaadinIcon.TRASH));
                        }))
                .setHeader(Labels.DELETE)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setWidthFull();

        grid.addSelectionListener(selection -> {

            selection.getFirstSelectedItem()
                    .ifPresent(building -> editEntity(building.id()));
        });


        add(grid);
    }

    @Override
    public void delete(Building building) {

        service.delete(building)
                .then(refreshData())
                .subscribeOn(Schedulers.parallel())
                .subscribe(this.refreshGridSubscriber());
    }

    private Subscriber<Void> refreshGridSubscriber() {
        return ViewUtil.emptySubscriber(throwable -> {
            asyncNotification("Error Refreshing Grid" + throwable.getMessage());
            logger.error("ERROR", throwable);
        });
    }

    private Mono<List<Building>> listOfBuildings() {
        return service.list(null);
    }

    private Mono<Void> refreshData() {

        final var ratesRunnable = listOfBuildings()
                .map(list -> (Runnable) () -> {
                    countOfBuildingText.setText(String.format("Edificios: %d", list.size()));
                    grid.setItems(list);
                    grid.getDataProvider().refreshAll();
                })
                .doOnError(throwable -> logger.error("ERROR", throwable))
                .onErrorResume(throwable -> Mono.just(() -> asyncNotification("Error al cargar edificios " + throwable.getMessage())));


        return ratesRunnable
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .and(Mono.empty());
    }

    private HorizontalLayout getToolbar() {

        addBuildingButton.setDisableOnClick(true);
        addBuildingButton.addClickListener(click -> editEntity());

        HorizontalLayout toolbar = new HorizontalLayout(addBuildingButton, countOfBuildingText);
        toolbar.addClassName("toolbar");
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return toolbar;
    }


    @Override
    public Component component() {
        return this;
    }

    @Override
    public Logger logger() {
        return logger;
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
