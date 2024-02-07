package kyo.yaz.condominium.manager.ui.views.apartment;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import jakarta.annotation.security.PermitAll;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.mapper.ApartmentMapper;
import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.component.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;


@PageTitle(ApartmentView.PAGE_TITLE)
@PermitAll
@Route(value = "apartments", layout = MainLayout.class)
public class ApartmentView extends BaseVerticalLayout {

  public static final String PAGE_TITLE = "Apartamentos";

  private final Grid<Apartment> grid = new Grid<>();

  private final MultiSelectComboBox<String> buildingComboBox = new MultiSelectComboBox<>();

  private final DeleteDialog deleteDialog = new DeleteDialog();
  private final TextField filterText = new TextField();

  private final Text queryCountText = new Text(null);
  private final Text totalCountText = new Text(null);
  private final Button addBtn = new Button(Labels.ADD);
  private final ApartmentService apartmentService;
  private final ProgressLayout progressLayout = new ProgressLayout();
  private final BuildingService buildingService;  private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);
  private final ApartmentForm form = new ApartmentForm();
  @Autowired
  public ApartmentView(ApartmentService apartmentService, BuildingService buildingService) {
    super();
    this.apartmentService = apartmentService;
    this.buildingService = buildingService;
    init();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    initData();
  }

  private void init() {
    addClassName("apartment-view");
    setSizeFull();
    configureGrid();
    configureForm();

    add(getToolbar(), progressLayout, getContent(), footer());
    closeEditor();
  }

  private Component footer() {

    final var footer = new Div(gridPaginator, totalCountText);
    footer.addClassName("footer");
    return footer;
  }

  private Component getContent() {
    final var content = new HorizontalLayout(grid, form);
    content.setFlexGrow(2, grid);
    content.setFlexGrow(1, form);
    content.addClassNames("content");
    content.setSizeFull();
    return content;
  }

  private void configureGrid() {
    grid.addClassNames("apartments-grid");

    grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
    grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
    grid.addComponentColumn(this::card);

    grid.setPageSize(gridPaginator.itemsPerPage());
    grid.setSizeFull();

    final var contextMenu = grid.addContextMenu();
    contextMenu.setDynamicContentHandler(Objects::nonNull);

    final var deleteMenu = contextMenu.addItem(Labels.DELETE);
    deleteMenu.addComponentAsFirst(VaadinIcon.TRASH.create());
    deleteMenu.addMenuItemClickListener(e -> e.getItem().ifPresent(this::deleteDialog));

    grid.asSingleSelect().addValueChangeListener(event -> editEntity(event.getValue()));
  }

  private Component card(Apartment apartment) {
    final var div = new Div();
    div.addClassName("card");

    final var header = new Div(new Span(apartment.apartmentId().buildingId()),
        new Span(apartment.apartmentId().number()));
    header.addClassName("header");

    final var body = new Div(new Span(apartment.name()));
    body.addClassName("body");

    final var idDoc = apartment.idDoc();

    if (idDoc != null && !idDoc.isEmpty()) {
      body.add(new Span(idDoc));
    }

    final var emails = String.join(",", apartment.emails());
    body.add(new Span(emails));

    final var amountToPay = Labels.Apartment.ALIQUOT_LABEL + ": %s".formatted(apartment.amountToPay());
    body.add(amountToPay);

    final var buttons = new Div(deleteBtn(new Button(), apartment));
    buttons.addClassName("buttons");

    div.add(header, body, buttons);
    return div;
  }

  private void configureForm() {
    form.setWidth(30, Unit.EM);
    form.setHeightFull();
    form.addListener(ApartmentForm.SaveEvent.class, this::saveEntity);
    form.addListener(ApartmentForm.DeleteEvent.class, this::deleteEntity);
    form.addListener(ApartmentForm.CloseEvent.class, e -> closeEditor());
  }

  private void updateGrid() {
    refreshData()
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }

  private void initData() {

    Single.zip(paging(), buildingService.buildingIds(), (paging, buildingIds) ->
            (Runnable) () -> {

              form.setBuildingIds(buildingIds);
              buildingComboBox.setItems(buildingIds);
              setItems(paging);
              progressLayout.setVisible(false);
              gridPaginator.init();
            })
        .doOnSuccess(this::uiAsyncAction)
        .ignoreElement()
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }

  private void setCountText(long queryCount, long totalCount) {
    queryCountText.setText(String.format("Apartamentos: %d", queryCount));
    gridPaginator.set(queryCount, totalCount);
    totalCountText.setText(String.format("Total de Apartamentos: %d", totalCount));
  }

  private Single<Paging<Apartment>> paging() {
    return apartmentService.paging(buildingComboBox.getValue(), filterText.getValue(), gridPaginator.currentPage(),
            gridPaginator.itemsPerPage())
        .doOnSubscribe(d -> {
          uiAsyncAction(() -> {
            progressLayout.setProgressText("Buscando apartamentos");
            progressLayout.setVisible(true);
            progressLayout.progressBar().setIndeterminate(true);
          });
        })
        .doOnTerminate(() -> uiAsyncAction(() -> progressLayout.setVisible(false)));
  }

  private void setItems(Paging<Apartment> paging) {
    grid.setPageSize(gridPaginator.itemsPerPage());
    grid.setItems(paging.results());

    setCountText(paging.queryCount(), paging.totalCount());

    grid.getDataProvider().refreshAll();
  }

  private Completable refreshData() {

    return paging()
        .map(paging -> (Runnable) () -> setItems(paging))
        .doOnSuccess(this::uiAsyncAction)
        .ignoreElement();
  }

  private Component getToolbar() {
    filterText.setPlaceholder("Buscar");
    filterText.setClearButtonVisible(true);
    filterText.setValueChangeMode(ValueChangeMode.LAZY);
    filterText.addValueChangeListener(e -> {
      if (!gridPaginator.goToFirstPage()) {
        updateGrid();
      }
    });
    addBtn.setDisableOnClick(true);
    addBtn.addClickListener(click -> addEntity());

    //buildingComboBox.setHelperText(Labels.Apartment.BUILDING_LABEL);
    buildingComboBox.setPlaceholder(Labels.Apartment.BUILDING_LABEL);
    buildingComboBox.setClearButtonVisible(true);
    buildingComboBox.setAutoOpen(true);

    final var asyncSubject = BehaviorSubject.create();

    buildingComboBox.addValueChangeListener(o -> {
      logger().info("emit event {}", o.getValue());
      asyncSubject.onNext(o.getValue());
    });

    final var subscribed = asyncSubject
        .debounce(1000, TimeUnit.MILLISECONDS)
        .distinctUntilChanged()
        .subscribe(o -> {
          logger().info("consume event {}", o);
          if (!gridPaginator.goToFirstPage()) {
            updateGrid();
          }
        }, this::showError);

    compositeDisposable.add(subscribed);

       /* final var disposable = Flowable.create(emitter -> {
                    buildingComboBox.addValueChangeListener(o -> {
                        logger().info("emit event {}", o.getValue());
                        emitter.onNext(o.getValue());
                    });
                }, BackpressureStrategy.LATEST)
                .throttleLast(1000, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    logger().info("consume event {}", o);
                    if (!gridPaginator.goToFirstPage()) {
                        updateGrid();
                    }
                }, this::showError);

        compositeDisposable.add(disposable);*/


        /*buildingComboBox.addValueChangeListener(o -> {
            if (!gridPaginator.goToFirstPage()) {
                updateGrid();
            }
        });*/

    final var toolbar = new Div(filterText, buildingComboBox, addBtn, queryCountText);
    toolbar.addClassName("toolbar");
    return toolbar;
  }

  public void editEntity(Apartment apartment) {
    if (apartment == null) {
      closeEditor();
    } else {
      form.setItem(ApartmentMapper.to(apartment));
      form.setVisible(true);
      addClassName("editing");
    }
  }

  private void closeEditor() {
    form.setItem(null);
    form.setVisible(false);
    addBtn.setEnabled(true);
    removeClassName("editing");
  }

  private void addEntity() {
    grid.asSingleSelect().clear();
    editEntity(Apartment.builder().build());
  }

  private void saveEntity(ApartmentForm.SaveEvent event) {
    apartmentService.save(ApartmentMapper.to(event.getObj()))
        .ignoreElement()
        .andThen(refreshData())
        .andThen(updateBuildingCount(event.getObj().getBuildingId()))
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());

    closeEditor();
  }

  private void deleteDialog(Apartment apartment) {
    deleteDialog.setText(
        Labels.ASK_CONFIRMATION_DELETE_APT.formatted(apartment.apartmentId().number(), apartment.name()));
    deleteDialog.setDeleteAction(() -> delete(apartment));
    deleteDialog.open();
  }

  public void delete(Apartment obj) {

    apartmentService.delete(obj)
        .andThen(refreshData())
        .andThen(updateBuildingCount(obj.apartmentId().buildingId()))
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }

  private Button deleteBtn(Button button, Apartment item) {
    button.addThemeVariants(ButtonVariant.LUMO_ICON,
        ButtonVariant.LUMO_ERROR,
        ButtonVariant.LUMO_TERTIARY);
    button.addClickListener(e -> this.deleteDialog(item));
    button.setIcon(new Icon(VaadinIcon.TRASH));
    return button;
  }

  private void deleteEntity(ApartmentForm.DeleteEvent event) {
    deleteDialog(ApartmentMapper.to(event.getObj()));
    closeEditor();
  }

  private Completable updateBuildingCount(String buildingId) {
    return apartmentService.countByBuilding(buildingId)
        .flatMapCompletable(count -> buildingService.updateAptCount(buildingId, count));
  }

  private static class ApartmentContextMenu extends GridContextMenu<Apartment> {

    public ApartmentContextMenu(Grid<Apartment> target, Consumer<Apartment> deleteApartment) {
      super(target);

      addItem("Borrar", e -> e.getItem().ifPresent(deleteApartment));

      //add(new Hr());
    }
  }




}

