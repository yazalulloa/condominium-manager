package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.service.entity.ReceiptService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.domain.DeleteDialog;
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
@PageTitle(ReceiptView.PAGE_TITLE)
@Route(value = "receipts", layout = MainLayout.class)
public class ReceiptView extends VerticalLayout implements AbstractView {
    public static final String PAGE_TITLE = Labels.Receipt.VIEW_PAGE_TITLE;
    private final Grid<Receipt> grid = new Grid<>();
    private final ComboBox<String> buildingComboBox = new ComboBox<>();

    private final TextField filterText = new TextField();

    private final Text countText = new Text(null);
    private final Button addEntityButton = new Button(Labels.Receipt.ADD_BUTTON_LABEL);
    private final BuildingService buildingService;
    private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);

    private final DeleteDialog deleteDialog = new DeleteDialog();
    private final ReceiptService receiptService;

    @Autowired
    public ReceiptView(BuildingService buildingService, ReceiptService receiptService) {
        super();
        this.buildingService = buildingService;
        this.receiptService = receiptService;
        init();
    }

    @Override
    public Component component() {
        return this;
    }

    @Override
    public Logger logger() {
        return log;
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

        add(getToolbar(), grid, gridPaginator);
    }

    private void configureGrid() {
        grid.addClassNames("apartments-grid");
        grid.setColumnReorderingAllowed(true);


        grid.addColumn(Receipt::id).setHeader(Labels.Receipt.ID_LABEL).setSortable(true).setKey(Labels.Receipt.ID_LABEL);
        grid.addColumn(Receipt::buildingId).setHeader(Labels.Receipt.BUILDING_LABEL).setSortable(true).setKey(Labels.Receipt.BUILDING_LABEL);
        grid.addColumn(Receipt::date).setHeader(Labels.Receipt.DATE_LABEL).setSortable(true).setKey(Labels.Receipt.DATE_LABEL);
        grid.addColumn(receipt -> ConvertUtil.format(receipt.totalCommonExpenses(), receipt.totalCommonExpensesCurrency())).setHeader(Labels.Receipt.EXPENSE_COMMON_LABEL);
        grid.addColumn(receipt -> ConvertUtil.format(receipt.totalUnCommonExpenses(), receipt.totalUnCommonExpensesCurrency())).setHeader(Labels.Receipt.EXPENSE_UNCOMMON_LABEL);
        grid.addColumn(Receipt::debtReceiptsAmount).setHeader(Labels.Receipt.DEBT_RECEIPT_TOTAL_NUMBER_LABEL).setSortable(true).setKey(Labels.Receipt.DEBT_RECEIPT_TOTAL_NUMBER_LABEL);
        grid.addColumn(receipt -> ConvertUtil.format(receipt.totalDebt(), receipt.totalDebtCurrency())).setHeader(Labels.Receipt.DEBT_RECEIPT_TOTAL_AMOUNT_LABEL).setSortable(true).setKey(Labels.Receipt.DEBT_RECEIPT_TOTAL_AMOUNT_LABEL);
        grid.addColumn(receipt -> receipt.rate().rate()).setHeader(Labels.Receipt.RATE_LABEL).setSortable(true).setKey(Labels.Receipt.RATE_LABEL);
        grid.addColumn(receipt -> DateUtil.formatVe(receipt.createdAt())).setHeader(Labels.Receipt.CREATED_AT_LABEL).setSortable(true).setKey(Labels.Receipt.CREATED_AT_LABEL);


        grid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> {

                                deleteDialog.setHeaderTitle(Labels.Receipt.ASK_CONFIRMATION_DELETE.formatted(item.id(), item.buildingId(), item.date()));
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

        grid.addSelectionListener(selection -> {

            selection.getFirstSelectedItem()
                    .ifPresent(building -> addEntity(building.id()));
        });
    }

    private void delete(Receipt receipt) {
        receiptService.delete(receipt.id())
                .doAfterTerminate(this::updateGrid)
                .subscribe(emptySubscriber());
    }

    private Mono<Runnable> setCount() {
        return receiptService.countAll()
                .map(count -> () -> setCountText(count));
    }

    private void initData() {
        final var countMono = setCount();

        final var setBuildingsIds = buildingService.buildingIds()
                .map(buildingIds -> (Runnable) () -> buildingComboBox.setItems(buildingIds));

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

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Buscar");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> {
            if (!gridPaginator.goToFirstPage()) {
                updateGrid();
            }
        });
        addEntityButton.setDisableOnClick(true);
        addEntityButton.addClickListener(click -> addEntity());


        //buildingComboBox.setHelperText(Labels.Apartment.BUILDING_LABEL);
        buildingComboBox.setPlaceholder(Labels.Apartment.BUILDING_LABEL);
        buildingComboBox.setClearButtonVisible(true);
        buildingComboBox.setAutoOpen(true);
        buildingComboBox.addValueChangeListener(o -> {
            if (!gridPaginator.goToFirstPage()) {
                updateGrid();
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(filterText, buildingComboBox, addEntityButton, countText);
        toolbar.addClassName("toolbar");
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return toolbar;
    }

    private void setCountText(Long count) {
        countText.setText(String.format(Labels.Receipt.AMOUNT_OF_LABEL, count));
        gridPaginator.set(count);
    }

    private void updateGrid() {
        refreshData()
                .subscribeOn(Schedulers.parallel())
                .subscribe(this.emptySubscriber());
    }

    private Mono<Void> refreshData() {

        final var countMono = setCount();

        final var updateGrid = entityList()
                .map(list -> (Runnable) () -> {

                    grid.setPageSize(gridPaginator.itemsPerPage());
                    grid.setItems(list);

                    grid.getDataProvider().refreshAll();
                });

        return Mono.zip(countMono, updateGrid, (count, update) -> {
                    return (Runnable) () -> {
                        count.run();
                        update.run();
                    };
                })
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .and(Mono.empty());
    }

    private Mono<List<Receipt>> entityList() {
        return receiptService.list(buildingComboBox.getValue(), filterText.getValue(), gridPaginator.currentPage(), gridPaginator.itemsPerPage());
    }

    private void addEntity(Long id) {
        addEntity(String.valueOf(id));
    }

    private void addEntity() {
        addEntity("new");
    }

    private void addEntity(String id) {
        grid.asSingleSelect().clear();
        ui(ui -> ui.navigate(EditReceiptView.class, new RouteParameters("receipt_id", id)));
    }


}
