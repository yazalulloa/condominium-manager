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
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.mapper.ApartmentMapper;
import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.actions.DeleteEntity;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.form.CreateApartmentForm;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;


@PageTitle(ApartmentView.PAGE_TITLE)
@Route(value = "apartments", layout = MainLayout.class)
public class ApartmentView extends BaseVerticalLayout implements DeleteEntity<Apartment> {
    public static final String PAGE_TITLE = "Apartamentos";

    private final Grid<Apartment> grid = new Grid<>();

    private final MultiSelectComboBox<String> buildingComboBox = new MultiSelectComboBox<>();

    private final TextField filterText = new TextField();

    private final Text queryCountText = new Text(null);
    private final Text totalCountText = new Text(null);
    private final Button addApartmentButton = new Button(Labels.ADD);
    private final ApartmentService service;    private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);
    private final BuildingService buildingService;
    private CreateApartmentForm createApartmentForm;
    @Autowired
    public ApartmentView(ApartmentService service, BuildingService buildingService) {
        super();
        this.service = service;
        this.buildingService = buildingService;
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

        final var verticalLayout = new VerticalLayout(totalCountText);

        final var footer = new HorizontalLayout(gridPaginator, verticalLayout);

        footer.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        footer.setFlexGrow(2, gridPaginator);
        footer.setFlexGrow(1, verticalLayout);

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
                .andThen(refreshData())
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }

    private void updateGrid() {
        refreshData()
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }

    private void initData() {

        Single.zip(service.countAll(), buildingService.buildingIds(), (count, buildingIds) ->
                        (Runnable) () -> {

                            init();
                            createApartmentForm.setBuildingIds(buildingIds);
                            buildingComboBox.setItems(buildingIds);
                            setCountText(count, count);
                        })
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }

    private void setCountText(long queryCount, long totalCount) {
        queryCountText.setText(String.format("Apartamentos: %d", queryCount));
        gridPaginator.set(queryCount);
        totalCountText.setText(String.format("Total de Apartamentos: %d", totalCount));
    }

    private Single<Paging<Apartment>> pagingMono() {
        return service.paging(buildingComboBox.getValue(), filterText.getValue(), gridPaginator.currentPage(), gridPaginator.itemsPerPage());
    }

    private Completable refreshData() {

        return pagingMono()
                .map(paging -> (Runnable) () -> {

                    grid.setPageSize(gridPaginator.itemsPerPage());
                    grid.setItems(paging.results());

                    setCountText(paging.queryCount(), paging.totalCount());

                    grid.getDataProvider().refreshAll();
                })
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement();
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
            createApartmentForm.setApartment(ApartmentMapper.to(apartment));
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

    private void saveEntity(CreateApartmentForm.SaveEvent event) {
        service.save(ApartmentMapper.to(event.getObj()))
                .ignoreElement()
                .andThen(refreshData())
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());

        closeEditor();
    }

    private void deleteEntity(CreateApartmentForm.DeleteEvent event) {
        delete(ApartmentMapper.to(event.getObj()));
        closeEditor();
    }

    private static class ApartmentContextMenu extends GridContextMenu<Apartment> {

        public ApartmentContextMenu(Grid<Apartment> target, DeleteEntity<Apartment> deleteApartment) {
            super(target);

            addItem("Borrar", e -> e.getItem().ifPresent(deleteApartment::delete));

            //add(new Hr());
        }
    }




}

