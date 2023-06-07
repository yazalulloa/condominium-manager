package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.security.PermitAll;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.service.SaveNewBcvRate;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.component.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.IconUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicBoolean;


@PageTitle(RateView.PAGE_TITLE)
@PermitAll
@Route(value = "rates", layout = MainLayout.class)
public class RateView extends BaseVerticalLayout {

    public static final String PAGE_TITLE = "Tasas de cambio";

    private final Grid<Rate> grid = new Grid<>();
    private final AtomicBoolean addingRate = new AtomicBoolean(false);
    private final Text queryCountText = new Text(null);
    private final Text totalCountText = new Text(null);
    private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);
    private final DeleteDialog deleteDialog = new DeleteDialog();
    private final RateService rateService;
    private final SaveNewBcvRate saveNewBcvRate;

    @Autowired
    public RateView(RateService rateService, SaveNewBcvRate saveNewBcvRate) {
        super();
        this.rateService = rateService;
        this.saveNewBcvRate = saveNewBcvRate;
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
        gridPaginator.init();

        add(getToolbar(), grid, footer());
    }

    private void initData() {


        paging()
                .map(paging -> (Runnable) () -> {
                    setItems(paging);
                    init();
                })
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
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
        grid.setPageSize(gridPaginator.itemsPerPage());
        grid.setSizeFull();
    }

    private void deleteDialog(Rate item) {
        deleteDialog.setText(Labels.ASK_CONFIRMATION_DELETE_RATE.formatted(item.rate(), item.dateOfRate(), item.fromCurrency() + "->" + item.toCurrency()));
        deleteDialog.setDeleteAction(() -> delete(item));
        deleteDialog.open();
    }

    private Component card(Rate rate) {


        final var body = new Div(new Span(ConvertUtil.format(rate.rate(), rate.toCurrency())), new Span(rate.dateOfRate().toString()),
                new Span(rate.source().name()));

        body.addClassName("body");

        final var footer = new Div(new Span(rate.rate().toString()), new Span(String.format("%s -> %s", rate.fromCurrency().name(), rate.toCurrency().name())),
                new Span(DateUtil.formatVe(rate.createdAt())));

        footer.addClassName("footer");

        final var deleteBtn = new Button(IconUtil.trash());
        deleteBtn.addClickListener(v -> deleteDialog(rate));

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

        final var toolbar = new Div(addButton, queryCountText);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private Single<Paging<Rate>> paging() {
        return rateService.paging(gridPaginator.currentPage(), gridPaginator.itemsPerPage());
    }

    private void setCountText(long queryCount, long totalCount) {
        queryCountText.setText(String.format("Tasas de cambio: %d", queryCount));
        gridPaginator.set(queryCount);
        totalCountText.setText(String.format("Total de Tasas de cambio: %d", totalCount));
    }

    private Completable refreshData() {

        return paging()
                .map(paging -> (Runnable) () -> setItems(paging))
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

        return saveNewBcvRate.saveNewRate()
                .doOnSuccess(bool -> logger().info("NEW_RATE_SAVED {}", bool))
                .doOnSuccess(bool -> asyncNotification(bool ? "Nueva tasa de cambio encontrada" : "Tasa ya guardada"))
                .ignoreElement()
                .andThen(refreshData())
                .subscribeOn(Schedulers.io());

    }

    private void updateGrid() {
        refreshData()
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }


}
