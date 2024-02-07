package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.security.PermitAll;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import kyo.yaz.condominium.manager.core.domain.BcvUsdRateResult;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.service.SaveNewBcvRate;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.component.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.IconUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;


@PageTitle(RateView.PAGE_TITLE)
@PermitAll
@Route(value = "rates", layout = MainLayout.class)
public class RateView extends BaseVerticalLayout {

  public static final String PAGE_TITLE = "Tasas de cambio";

  private final Grid<Rate> grid = new Grid<>();
  private final AtomicBoolean addingRate = new AtomicBoolean(false);

  private final Button refreshBtn = new Button(VaadinIcon.REFRESH.create());
  private final Text queryCountText = new Text(null);
  private final Text totalCountText = new Text(null);
  private final DeleteDialog deleteDialog = new DeleteDialog();  private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);
  private final RateService rateService;
  private final SaveNewBcvRate saveNewBcvRate;
  private final ProgressLayout progressLayout = new ProgressLayout();
  @Autowired
  public RateView(RateService rateService, SaveNewBcvRate saveNewBcvRate) {
    super();
    this.rateService = rateService;
    this.saveNewBcvRate = saveNewBcvRate;
    init();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    initData();
  }

  private void init() {
    addClassName("rates-view");
    setSizeFull();
    configureGrid();

    add(getToolbar(), progressLayout, grid, footer());
  }

  private void initData() {

    refreshData()
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }

  private Component footer() {
    final var footer = new Div(gridPaginator, totalCountText);
    footer.addClassName("footer");
    return footer;
  }

  private void configureGrid() {
    grid.addClassNames("rates-grid");
    grid.addComponentColumn(this::card);
    grid.setItemDetailsRenderer(new ComponentRenderer<>(rate -> {
      final var div = new Div();

      Optional.ofNullable(rate.hashes())
          .map(s -> s.stream().map(Object::toString)
              .collect(Collectors.joining(",")))
          .ifPresent(hashes -> div.add(new Span("Hashes: " + hashes)));

      Optional.ofNullable(rate.etags())
          .map(s -> String.join(",", s))
          .ifPresent(hashes -> div.add(new Span("Etags: " + hashes)));

      Optional.ofNullable(rate.lastModified())
          .ifPresent(lastModified -> div.add(new Span("Last Modified: " + lastModified)));

      return div;
    }));
    grid.setPageSize(gridPaginator.itemsPerPage());
    grid.setSizeFull();
  }

  private void deleteDialog(Rate item) {
    deleteDialog.setText(Labels.ASK_CONFIRMATION_DELETE_RATE.formatted(item.rate(), item.dateOfRate(),
        item.fromCurrency() + "->" + item.toCurrency()));
    deleteDialog.setDeleteAction(() -> delete(item));
    deleteDialog.open();
  }

  private Component card(Rate rate) {

    final var body = new Div(new Span(ConvertUtil.format(rate.rate(), rate.toCurrency())),
        new Span(rate.dateOfRate().toString()),
        new Span(rate.source().name()));

    body.addClassName("body");

    final var footer = new Div(new Span(rate.rate().toString()),
        new Span(String.format("%s -> %s", rate.fromCurrency().name(), rate.toCurrency().name())),
        new Span(DateUtil.formatVe(rate.createdAt())),
        new Span("ID: " + rate.id())

    );

        /*if (rate.hash() != null) {
            footer.add(new Span("Hash: " + rate.hash()));
        }*/

    footer.addClassName("footer");

    final var deleteBtn = new Button(IconUtil.trash());
    deleteBtn.addClickListener(v -> {

      deleteDialog(rate);
    });

    final var buttons = new Div(deleteBtn);
    buttons.addClassName("buttons");

    final var div = new Div(body, footer, buttons);
    div.addClassName("card");
    return div;
  }

  public void delete(Rate rate) {
    rateService.delete(rate)
        .andThen(refreshData())
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }

  private Component getToolbar() {
    final var addButton = new Button(Labels.ADD);
    addButton.setDisableOnClick(true);
    addButton.addClickListener(e -> {
      if (addingRate.get()) {
        Notification.show("Ya se esta buscando tasa de cambio");
      } else {
        addingRate.set(true);
        Notification.show("Buscando Tasa de cambio");

        newRate().doAfterTerminate(() -> uiAsyncAction(() -> {
              addingRate.set(false);
              addButton.setEnabled(true);
            }))
            .subscribe(completableObserver());
      }

    });

    refreshBtn.setDisableOnClick(true);
    refreshBtn.addClickListener(v -> updateGrid());

    final var toolbar = new Div(addButton, refreshBtn, queryCountText);
    toolbar.addClassName("toolbar");
    return toolbar;
  }

  private Single<Paging<Rate>> paging() {
    return rateService.paging(gridPaginator.currentPage(), gridPaginator.itemsPerPage())
        .doOnSubscribe(d -> {
          uiAsyncAction(() -> {
            refreshBtn.setEnabled(false);
            progressLayout.setProgressText("Buscando...");
            progressLayout.progressBar().setIndeterminate(true);
            progressLayout.setVisible(true);
          });
        });
  }

  private void setCountText(long queryCount, long totalCount) {
    queryCountText.setText(String.format("Tasas de cambio: %d", queryCount));
    gridPaginator.set(queryCount, totalCount);
    totalCountText.setText(String.format("Total de Tasas de cambio: %d", totalCount));
  }

  private Completable refreshData() {

    return paging()
        .map(paging -> (Runnable) () -> {
          refreshBtn.setEnabled(true);
          progressLayout.setVisible(false);
          setItems(paging);
          gridPaginator.init();
        })
        .doOnSuccess(this::uiAsyncAction)
        .ignoreElement();
  }

  private void setItems(Paging<Rate> paging) {
    grid.setPageSize(gridPaginator.itemsPerPage());
    grid.setItems(paging.results());

    setCountText(paging.queryCount(), paging.totalCount());

    grid.getDataProvider().refreshAll();
  }

  private Completable newRate() {

      /*  return Flowable.range(0, 10)
                .map(i -> saveNewBcvRate.saveNewRate())
                .toList()
                .toFlowable()
                .flatMap(Single::merge)
                .reduce(Boolean::logicalOr)*/

    return saveNewBcvRate.saveNewRate()
        .doOnSuccess(result -> logger().info("RATE_RESULT {}", result.state()))
        .map(result -> result.state() == BcvUsdRateResult.State.NEW_RATE)
        .doOnSuccess(bool -> asyncNotification(bool ? "Nueva tasa de cambio encontrada" : "Tasa ya guardada"))
        .flatMapCompletable(b -> b ? refreshData() : Completable.complete())
        .subscribeOn(Schedulers.io());

  }

  private void updateGrid() {
    refreshData()
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }




}
