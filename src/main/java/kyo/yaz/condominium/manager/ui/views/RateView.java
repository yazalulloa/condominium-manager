package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import kyo.yaz.condominium.manager.core.service.GetBcvUsdRate;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.repository.RateRepository;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.actions.DeleteEntity;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@PageTitle(RateView.PAGE_TITLE)
@Route(value = "rates", layout = MainLayout.class)
public class RateView extends VerticalLayout implements DeleteEntity<Rate>, AbstractView {

    public static final String PAGE_TITLE = "Tasas de cambio";

    private final Grid<Rate> grid = new Grid<>();
    private final AtomicBoolean addingRate = new AtomicBoolean(false);

    @Autowired
    private UI ui;
    @Autowired
    private GetBcvUsdRate getBcvUsdRate;
    @Autowired
    private RateRepository rateRepository;

    private Text countOfRatesText;


    public RateView() {
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

        add(getToolbar(), grid);
    }

    private void configureGrid() {
        grid.addClassNames("rates-grid");

        grid.addColumn(Rate::id).setHeader("ID");
        grid.addColumn(Rate::rate).setHeader("Tasa");
        grid.addColumn(rate -> ConvertUtil.format(rate.roundedRate(), rate.toCurrency())).setHeader("Tasa Redondeada");
        grid.addColumn(Rate::dateOfRate).setHeader("Fecha de la tasa");
        grid.addColumn(Rate::source).setHeader("Fuente de la tasa");
        grid.addColumn(rate -> String.format("%s -> %s", rate.fromCurrency().name(), rate.toCurrency().name())).setHeader("Monedas");
        grid.addColumn(Rate::createdAt).setHeader("Fecha de la tasa");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setWidthFull();

        grid.setItems(q -> {
            final var pageRequest = PageRequest.of(q.getPage(), q.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(q));
            return rateRepository.findAllBy(pageRequest).toStream();
        });

        final var contextMenu = new RateContextMenu(grid, this);
        add(grid, contextMenu);

        /*grid.setItems(query -> {
           return rateRepository.findAllBy(PageRequest.of(query.getPage(), query.getPageSize()) ).collectList().block().stream();
        });*/
    }

    private Subscriber<Void> refreshGridSubscriber() {
        return ViewUtil.emptySubscriber(throwable -> {
            asyncNotification("Error Refreshing Grid" + throwable.getMessage());
            logger().error("ERROR", throwable);
        });
    }

    @Override
    public void delete(Rate rate) {
        rateRepository.delete(rate)
                .then(refreshData())
                .subscribeOn(Schedulers.parallel())
                .subscribe(this.refreshGridSubscriber());
    }

    private static class RateContextMenu extends GridContextMenu<Rate> {

        public RateContextMenu(Grid<Rate> target, DeleteEntity<Rate> deleteRate) {
            super(target);

            addItem("Borrar", e -> e.getItem().ifPresent(deleteRate::delete));

            add(new Hr());

            /*GridMenuItem<Rate> emailItem = addItem("Email",
                    e -> e.getItem().ifPresent(person -> {
                        // System.out.printf("Email: %s%n", person.getFullName());
                    }));
            GridMenuItem<Rate> phoneItem = addItem("Call",
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
                        .subscribe(this.refreshGridSubscriber());
            }

        });

        countOfRatesText = new Text(null);

        HorizontalLayout toolbar = new HorizontalLayout(addButton, countOfRatesText);
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
        return log;
    }

    private Mono<List<Rate>> listOfRates() {
        return rateRepository.findAllBy(Pageable.unpaged())
                .collectList();
    }

    private Mono<Void> refreshData() {
        final var countRunnable = rateRepository.count()
                .map(count -> String.format("Cantidad de tasas de cambio: %d", count))
                .map(str -> (Runnable) () -> countOfRatesText.setText(str))
                .onErrorResume(throwable -> Mono.just(() -> asyncNotification("Error al cargar cantidad de tasas de cambio " + throwable.getMessage())));

        final var ratesRunnable = listOfRates()
                .map(rates -> {

                    return (Runnable) () -> {

                        /*grid.setItems(rates);
                        grid.getDataProvider().refreshAll();*/
                    };
                })
                .onErrorResume(throwable -> Mono.just(() -> asyncNotification("Error al cargar tasas de cambio " + throwable.getMessage())));


        return Mono.zip(countRunnable, ratesRunnable, (first, second) -> {

                    final var list = new ArrayList<Runnable>();
                    list.add(first);
                    list.add(second);

                    return list;
                })
                .doOnSuccess(list -> uiAsyncAction(() -> list.forEach(Runnable::run)))
                .ignoreElement()
                .and(Mono.empty());
    }

    private Mono<Void> newRate() {

        return getBcvUsdRate.newRate()
                .doOnSuccess(rate -> asyncNotification("Nueva tasa de cambio encontrada"))
                .flatMap(rateRepository::save)
                .doOnSuccess(rate -> asyncNotification("Tasa de cambio guardada"))
                .then(refreshData())
                .subscribeOn(Schedulers.parallel())
                .doOnSuccess(rate -> asyncNotification("Actualizando"));

    }


}
