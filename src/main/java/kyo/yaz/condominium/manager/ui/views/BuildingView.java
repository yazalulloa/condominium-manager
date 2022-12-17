package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import kyo.yaz.condominium.manager.core.service.BuildingService;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.actions.DeleteEntity;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.form.CreateBuildingForm;
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

@PageTitle(BuildingView.PAGE_TITLE)
@Route(value = "buildings", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class BuildingView extends VerticalLayout implements AbstractView, DeleteEntity<Building> {
    public static final String PAGE_TITLE = "Edificios";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Grid<Building> grid = new Grid<>();

    //private final TextField filterText = new TextField();

    private final Text countOfBuildingText = new Text(null);

    private final Button addBuildingButton = new Button("Añadir edificio");

    private CreateBuildingForm createBuildingForm;
    @Autowired
    private BuildingService service;

    public BuildingView() {
        super();
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
        configureForm();
        add(getToolbar(), getContent());
        closeEditor();
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, createBuildingForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, createBuildingForm);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureGrid() {
        grid.addClassNames("buildings-grid");

        grid.addColumn(Building::id).setHeader(Labels.Building.ID_LABEL);
        grid.addColumn(Building::name).setHeader(Labels.Building.NAME_LABEL);
        grid.addColumn(Building::rif).setHeader(Labels.Building.RIF_LABEL);
        grid.addColumn(building -> ConvertUtil.format(building.reserveFund(), building.reserveFundCurrency())).setHeader(Labels.Building.RESERVE_FUND_LABEL);
        grid.addColumn(Building::mainCurrency).setHeader(Labels.Building.MAIN_CURRENCY_LABEL);
        grid.addColumn(Building::currenciesToShowAmountToPay).setHeader(Labels.Building.SHOW_PAYMENT_IN_CURRENCIES);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setWidthFull();

        final var contextMenu = new BuildingView.BuildingContextMenu(grid, this);
        add(grid, contextMenu);

        grid.asSingleSelect().addValueChangeListener(event -> editEntity(event.getValue()));
    }

    private void configureForm() {
        createBuildingForm = new CreateBuildingForm();
        createBuildingForm.setWidth("25em");
        createBuildingForm.setHeightFull();
        createBuildingForm.addListener(CreateBuildingForm.SaveEvent.class, this::saveEntity);
        createBuildingForm.addListener(CreateBuildingForm.DeleteEvent.class, this::deleteEntity);
        createBuildingForm.addListener(CreateBuildingForm.CloseEvent.class, e -> closeEditor());
    }

    @Override
    public void delete(Building building) {

        service.delete(building)
                .then(refreshData())
                .subscribeOn(Schedulers.parallel())
                .subscribe(this.refreshGridSubscriber());
    }

    private void saveEntity(CreateBuildingForm.SaveEvent event) {
        service.save(ConvertUtil.building(event.getBuilding()))
                .then(refreshData())
                .subscribeOn(Schedulers.parallel())
                .subscribe(this.emptySubscriber());

        closeEditor();
    }

    private void deleteEntity(CreateBuildingForm.DeleteEvent event) {
        delete(ConvertUtil.building(event.getBuilding()));
        closeEditor();
    }

    private static class BuildingContextMenu extends GridContextMenu<Building> {

        public BuildingContextMenu(Grid<Building> target, DeleteEntity<Building> deleteBuilding) {
            super(target);

            addItem("Borrar", e -> e.getItem().ifPresent(deleteBuilding::delete));

            add(new Hr());

            /*GridMenuItem<Building> emailItem = addItem("Email",
                    e -> e.getItem().ifPresent(person -> {
                        // System.out.printf("Email: %s%n", person.getFullName());
                    }));
            GridMenuItem<Building> phoneItem = addItem("Call",
                    e -> e.getItem().ifPresent(person -> {
                        // System.out.printf("Phone: %s%n", person.getFullName());
                    }));

            setDynamicContentHandler(person -> {
                // Do not show context menu when header is clicked
                if (person == null)
                    return false;
                emailItem.setText(String.format("Email: %s", person.getEmail()));
                phoneItem.setText(String.format("Call: %s",
                        person.getAddress().getPhone()));
                return true;
            });*/
        }
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
                .map(list -> {

                    return (Runnable) () -> {
                        countOfBuildingText.setText(String.format("Edificios: %d", list.size()));
                        grid.setItems(list);
                        grid.getDataProvider().refreshAll();
                    };
                })
                .onErrorResume(throwable -> Mono.just(() -> asyncNotification("Error al cargar edificios " + throwable.getMessage())));


        return ratesRunnable
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .and(Mono.empty());
    }

    private HorizontalLayout getToolbar() {
       /* filterText.setPlaceholder("Buscar");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> refreshData());*/

        addBuildingButton.setDisableOnClick(true);
        addBuildingButton.addClickListener(click -> addEntity());


       /* addButton.addClickListener(e -> {
            if (addingRate.get()) {
                Notification.show("Ya se esta buscando tasa de cambio");
            } else {
                addingRate.set(true);
                Notification.show("Buscando Tasa de cambio");

                newRate().doAfterTerminate(() -> uiAsyncAction(() -> {
                            addingRate.set(false);
                            addButton.setEnabled(true);
                        }))
                        .subscribe(this.refreshGridSubscriber());
            }

        });*/


        HorizontalLayout toolbar = new HorizontalLayout(addBuildingButton, countOfBuildingText);
        toolbar.addClassName("toolbar");
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return toolbar;
    }

    public void editEntity(Building obj) {
        if (obj == null) {
            closeEditor();
        } else {
            createBuildingForm.setBuilding(ConvertUtil.viewItem(obj));
            createBuildingForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        createBuildingForm.setBuilding(null);
        createBuildingForm.setVisible(false);
        addBuildingButton.setEnabled(true);
        removeClassName("editing");
    }

    private void addEntity() {
        grid.asSingleSelect().clear();
        editEntity(Building.builder().build());
    }

    @Override
    public Component component() {
        return this;
    }

    @Override
    public Logger logger() {
        return logger;
    }
}
