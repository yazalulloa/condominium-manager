package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.service.ApartmentService;
import kyo.yaz.condominium.manager.core.service.BuildingService;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.actions.DeleteEntity;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.form.CreateApartmentForm;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@PageTitle(ApartmentView.PAGE_TITLE)
@Route(value = "apartments", layout = MainLayout.class)
public class ApartmentView extends VerticalLayout implements AbstractView, DeleteEntity<Apartment> {
    public static final String PAGE_TITLE = "Apartamentos";

    private final Grid<Apartment> grid = new Grid<>();

    private final MultiSelectComboBox<String> buildingComboBox = new MultiSelectComboBox<>();

    private final TextField filterText = new TextField();

    private final Text queryCountText = new Text(null);
    private final Text totalCountText = new Text(null);

    private CreateApartmentForm createApartmentForm;

    private final Button addApartmentButton = new Button("Añadir apartamento");
    @Autowired
    private ApartmentService service;

    @Autowired
    private BuildingService buildingService;

    private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);

    public ApartmentView() {
        super();
        init();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initData();
    }

    private void init() {
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        configureForm();



        add(getToolbar(), getContent(), footer());
        closeEditor();
    }

    private Component footer() {
          /*gridPaginator.setPadding(true);
        gridPaginator.setJustifyContentMode(JustifyContentMode.END);
        gridPaginator.setAlignItems(Alignment.END);*/

        final var verticalLayout = new VerticalLayout(totalCountText);
        //verticalLayout.setAlignItems(Alignment.CENTER);

        final var footer = new HorizontalLayout(gridPaginator, verticalLayout);

        footer.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        footer.setFlexGrow(2, gridPaginator);
        footer.setFlexGrow(1, verticalLayout);
        //footer.setPadding(true);

        return footer;
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, createApartmentForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, createApartmentForm);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureGrid() {
        grid.addClassNames("apartments-grid");
        grid.setColumnReorderingAllowed(true);


        grid.addColumn(apartment -> apartment.apartmentId().buildingId()).setHeader(Labels.Apartment.BUILDING_LABEL).setSortable(true).setKey(Labels.Apartment.BUILDING_LABEL);
        grid.addColumn(apartment -> apartment.apartmentId().number()).setHeader(Labels.Apartment.NUMBER_LABEL).setSortable(true).setKey(Labels.Apartment.NUMBER_LABEL);
        grid.addColumn(Apartment::name).setHeader(Labels.Apartment.NAME_LABEL).setSortable(true).setKey(Labels.Apartment.NAME_LABEL);
        grid.addColumn(Apartment::idDoc).setHeader(Labels.Apartment.ID_DOC_LABEL).setSortable(true).setKey(Labels.Apartment.ID_DOC_LABEL);
        grid.addColumn(apartment -> String.join("\n", apartment.emails())).setHeader(Labels.Apartment.EMAILS_LABEL).setKey(Labels.Apartment.EMAILS_LABEL);
        grid.addColumn(Apartment::paymentType).setHeader(Labels.Apartment.PAYMENT_TYPE_LABEL).setKey(Labels.Apartment.PAYMENT_TYPE_LABEL);
        grid.addColumn(Apartment::amountToPay).setHeader(Labels.Apartment.AMOUNT_LABEL).setSortable(true).setKey(Labels.Apartment.AMOUNT_LABEL);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setPageSize(gridPaginator.itemsPerPage());
        grid.setSizeFull();

        final var contextMenu = new ApartmentView.ApartmentContextMenu(grid, this);
        add(grid, contextMenu);

        grid.asSingleSelect().addValueChangeListener(event -> editEntity(event.getValue()));
    }

    private void configureForm() {
        createApartmentForm = new CreateApartmentForm();
        createApartmentForm.setWidth("25em");
        createApartmentForm.setHeightFull();
        createApartmentForm.addListener(CreateApartmentForm.SaveEvent.class, this::saveEntity);
        createApartmentForm.addListener(CreateApartmentForm.DeleteEvent.class, this::deleteEntity);
        createApartmentForm.addListener(CreateApartmentForm.CloseEvent.class, e -> closeEditor());
    }

    @Override
    public void delete(Apartment obj) {

        service.delete(obj)
                .then(refreshData())
                .subscribeOn(Schedulers.parallel())
                .subscribe(this.emptySubscriber());
    }

    private void updateGrid() {
        refreshData()
                .subscribeOn(Schedulers.parallel())
                .subscribe(this.emptySubscriber());
    }

    private void initData() {
        final var countMono = service.countAll()
                .map(count -> (Runnable) () -> setCountText(count, count));

        final var setBuildingsIds = buildingService.buildingIds()
                .map(buildingIds -> (Runnable) () -> {
                    createApartmentForm.setBuildingIds(buildingIds);
                    buildingComboBox.setItems(buildingIds);
                });

        final var list = List.of(countMono, setBuildingsIds);

        Flux.fromIterable(list)
                .flatMap(m -> m)
                .collectList()
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .and(Mono.empty())
                .subscribeOn(Schedulers.parallel())
                .subscribe(emptySubscriber());
    }

    private void setCountText(long queryCount, long totalCount) {
        queryCountText.setText(String.format("Apartamentos: %d", queryCount));
        gridPaginator.set(queryCount);
        totalCountText.setText(String.format("Total de Apartamentos: %d", totalCount));
    }

    private Mono<Paging<Apartment>> pagingMono() {
        return service.paging(buildingComboBox.getValue(), filterText.getValue(), gridPaginator.currentPage(), gridPaginator.itemsPerPage());
    }

    private Mono<Void> refreshData() {


        return pagingMono()
                .map(paging -> (Runnable) () -> {

                    grid.setPageSize(gridPaginator.itemsPerPage());
                    grid.setItems(paging.results());

                    setCountText(paging.queryCount(), paging.totalCount());

                    //grid.setItems(query -> service.fetchPage(filterText.getValue(), query.getPage(), query.getPageSize()));
                    //grid.setItems(list);
                    grid.getDataProvider().refreshAll();
                })
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .and(Mono.empty());
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Buscar");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> {
            if (!gridPaginator.goToFirstPage()) {
                updateGrid();
            }
        });
        addApartmentButton.setDisableOnClick(true);
        addApartmentButton.addClickListener(click -> addEntity());


        //buildingComboBox.setHelperText(Labels.Apartment.BUILDING_LABEL);
        buildingComboBox.setPlaceholder(Labels.Apartment.BUILDING_LABEL);
        buildingComboBox.setClearButtonVisible(true);
        buildingComboBox.setAutoOpen(true);
        buildingComboBox.addValueChangeListener(o -> {
            if (!gridPaginator.goToFirstPage()) {
                updateGrid();
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(filterText, buildingComboBox, addApartmentButton, queryCountText);
        toolbar.addClassName("toolbar");
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return toolbar;
    }

    public void editEntity(Apartment apartment) {
        if (apartment == null) {
            closeEditor();
        } else {
            createApartmentForm.setApartment(ConvertUtil.viewItem(apartment));
            createApartmentForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        createApartmentForm.setApartment(null);
        createApartmentForm.setVisible(false);
        addApartmentButton.setEnabled(true);
        removeClassName("editing");
    }

    private void addEntity() {
        grid.asSingleSelect().clear();
        editEntity(Apartment.builder().build());
    }

    @Override
    public Component component() {
        return this;
    }

    @Override
    public Logger logger() {
        return log;
    }

    private void saveEntity(CreateApartmentForm.SaveEvent event) {
        service.save(ConvertUtil.apartment(event.getApartment()))
                .then(refreshData())
                .subscribeOn(Schedulers.parallel())
                .subscribe(this.emptySubscriber());

        closeEditor();
    }

    private void deleteEntity(CreateApartmentForm.DeleteEvent event) {
        delete(ConvertUtil.apartment(event.getApartment()));
        closeEditor();
    }

    private static class ApartmentContextMenu extends GridContextMenu<Apartment> {

        public ApartmentContextMenu(Grid<Apartment> target, DeleteEntity<Apartment> deleteApartment) {
            super(target);

            addItem("Borrar", e -> e.getItem().ifPresent(deleteApartment::delete));

            add(new Hr());

            /*GridMenuItem<Apartment> emailItem = addItem("Email",
                    e -> e.getItem().ifPresent(person -> {
                        // System.out.printf("Email: %s%n", person.getFullName());
                    }));
            GridMenuItem<Apartment> phoneItem = addItem("Call",
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
}

