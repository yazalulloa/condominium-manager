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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.service.SaveNewBcvRate;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.domain.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicBoolean;


@PageTitle(RateView.PAGE_TITLE)
@Route(value = "rates", layout = MainLayout.class)
public class RateView extends BaseVerticalLayout {

    public static final String PAGE_TITLE = "Tasas de cambio";

    private final Grid<Rate> grid = new Grid<>();
    private final AtomicBoolean addingRate = new AtomicBoolean(false);
    private final Text queryCountText = new Text(null);
    private final Text totalCountText = new Text(null);    private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);
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
        addClassName("list-view");
        setSizeFull();
        configureGrid();

        add(getToolbar(), grid, footer());
    }

    private void initData() {
        rateService.countAll()
                .map(count -> (Runnable) () -> {
                    init();
                    setCountText(count, count);
                })
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
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

    private void configureGrid() {
        grid.addClassNames("rates-grid");

        grid.setColumnReorderingAllowed(true);
        grid.addColumn(Rate::id).setHeader(Labels.Rate.ID_LABEL).setSortable(true).setKey(Labels.Rate.ID_LABEL);
        grid.addColumn(Rate::rate).setHeader(Labels.Rate.RATE_LABEL).setSortable(true).setKey(Labels.Rate.RATE_LABEL);
        grid.addColumn(Rate::dateOfRate).setHeader(Labels.Rate.DATE_OF_RATE_LABEL).setSortable(true).setKey(Labels.Rate.DATE_OF_RATE_LABEL);
        grid.addColumn(Rate::source).setHeader(Labels.Rate.SOURCE_LABEL).setSortable(true).setKey(Labels.Rate.SOURCE_LABEL);
        grid.addColumn(rate -> String.format("%s -> %s", rate.fromCurrency().name(), rate.toCurrency().name())).setHeader(Labels.Rate.CURRENCIES_LABEL);
        grid.addColumn(rate -> DateUtil.formatVe(rate.createdAt())).setHeader(Labels.Rate.CREATED_AT_LABEL).setSortable(true).setKey(Labels.Rate.CREATED_AT_LABEL);

        grid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> {

                                deleteDialog.setText(Labels.ASK_CONFIRMATION_DELETE_RATE.formatted(item.rate(), item.dateOfRate(), item.fromCurrency() + "->" + item.toCurrency()));
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
        grid.setPageSize(gridPaginator.itemsPerPage());
        grid.setSizeFull();

        //final var contextMenu = new RateContextMenu(grid, this);
        add(grid);

        /*grid.setItems(query -> {
           return rateRepository.findAllBy(PageRequest.of(query.getPage(), query.getPageSize()) ).collectList().block().stream();
        });*/
    }

    public void delete(Rate rate) {
        rateService.delete(rate)
                .andThen(refreshData())
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }

    private HorizontalLayout getToolbar() {
        final var addButton = new Button("Add");
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

        HorizontalLayout toolbar = new HorizontalLayout(addButton, queryCountText);
        toolbar.addClassName("toolbar");
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return toolbar;
    }

    private Single<Paging<Rate>> pagingMono() {
        return rateService.paging(gridPaginator.currentPage(), gridPaginator.itemsPerPage());
    }

    private void setCountText(long queryCount, long totalCount) {
        queryCountText.setText(String.format("Tasas de cambio: %d", queryCount));
        gridPaginator.set(queryCount);
        totalCountText.setText(String.format("Total de Tasas de cambio: %d", totalCount));
    }

    private Completable refreshData() {

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
                .ignoreElement();
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
