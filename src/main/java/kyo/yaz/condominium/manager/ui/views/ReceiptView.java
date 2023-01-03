package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import kyo.yaz.condominium.manager.core.service.CreatePdfReceiptService;
import kyo.yaz.condominium.manager.core.service.SendEmailReceipts;
import kyo.yaz.condominium.manager.core.service.csv.LoadCsvReceipt;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.service.entity.ReceiptService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.domain.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileInputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@PageTitle(ReceiptView.PAGE_TITLE)
@Route(value = "receipts", layout = MainLayout.class)
public class ReceiptView extends BaseVerticalLayout {
    public static final String PAGE_TITLE = Labels.Receipt.VIEW_PAGE_TITLE;
    private final Grid<Receipt> grid = new Grid<>();
    private final ComboBox<String> buildingComboBox = new ComboBox<>();

    private final Set<String> buildingIds = new LinkedHashSet<>();

    private final TextField filterText = new TextField();

    private final Text countText = new Text(null);
    private final Button addEntityButton = new Button(Labels.Receipt.ADD_BUTTON_LABEL);
    private final BuildingService buildingService;
    private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);

    private final DeleteDialog deleteDialog = new DeleteDialog();
    private final ReceiptService receiptService;

    private final LoadCsvReceipt loadCsvReceipt;

    private final VerticalLayout progressLayout = new VerticalLayout();

    private final CreatePdfReceiptService createPdfReceiptService;
    private final SendEmailReceipts sendEmailReceipts;

    @Autowired
    public ReceiptView(BuildingService buildingService, ReceiptService receiptService, LoadCsvReceipt loadCsvReceipt, CreatePdfReceiptService createPdfReceiptService, SendEmailReceipts sendEmailReceipts) {
        super();
        this.buildingService = buildingService;
        this.receiptService = receiptService;
        this.loadCsvReceipt = loadCsvReceipt;
        this.createPdfReceiptService = createPdfReceiptService;
        this.sendEmailReceipts = sendEmailReceipts;
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
        VaadinSession.getCurrent().setAttribute("receipt", null);

        final var progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);

        final var progressBarLabel = new Div();
        progressBarLabel.setText("Procesando archivo...");
        progressLayout.add(progressBarLabel, progressBar);
        progressLayout.setVisible(false);

        add(getToolbar(), progressLayout, grid, gridPaginator);
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
        grid.addColumn(Receipt::totalDebt).setHeader(Labels.Receipt.DEBT_RECEIPT_TOTAL_AMOUNT_LABEL).setSortable(true).setKey(Labels.Receipt.DEBT_RECEIPT_TOTAL_AMOUNT_LABEL);
        grid.addColumn(receipt -> receipt.rate().rate()).setHeader(Labels.Receipt.RATE_LABEL).setSortable(true).setKey(Labels.Receipt.RATE_LABEL);
        grid.addColumn(receipt -> DateUtil.formatVe(receipt.createdAt())).setHeader(Labels.Receipt.CREATED_AT_LABEL).setSortable(true).setKey(Labels.Receipt.CREATED_AT_LABEL);


        grid.addColumn(
                        new ComponentRenderer<>(Anchor::new, (anchor, item) -> {

                            anchor.setHref(new StreamResource(createPdfReceiptService.fileName(item), () -> {

                                try {
                                    final var path = createPdfReceiptService.zip(item)
                                            .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                                            .blockingGet();

                                    return new FileInputStream(path);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }));


                            anchor.getElement().setAttribute("download", true);
                            final var downloadButton = new Button(new Icon(VaadinIcon.DOWNLOAD_ALT));
                            anchor.add(downloadButton);
                        }))
                .setHeader(Labels.DOWNLOAD)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        grid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_SUCCESS,
                                    ButtonVariant.LUMO_TERTIARY_INLINE);
                            button.addClickListener(e -> {
                                button.setEnabled(false);
                               sendEmailReceipts.send(item)
                                       .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                                       .doAfterTerminate(() -> button.setEnabled(true))
                                       .subscribe(completableObserver(() -> {}, this::showError));

                            });
                            button.setIcon(new Icon(VaadinIcon.ENVELOPE));
                        }))
                .setHeader(Labels.SEND_EMAIL)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        grid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_SUCCESS,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> {
                                final var newItem = item.toBuilder()
                                        .id(null)
                                        .createdAt(null)
                                        .updatedAt(null)
                                        .build();

                                saveReceiptInSession(newItem);
                                addEntity("copy");

                            });
                            button.setIcon(new Icon(VaadinIcon.COPY));
                        }))
                .setHeader(Labels.COPY)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        grid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> {

                                deleteDialog.setText(Labels.Receipt.ASK_CONFIRMATION_DELETE.formatted(item.id(), item.buildingId(), item.date()));
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
                .map(set -> (Runnable) () -> {
                    buildingIds.addAll(set);
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

    private Upload upload() {
        final var buffer = new FileBuffer();
        final var upload = new Upload(buffer);
        upload.setDropAllowed(true);
        upload.setAutoUpload(true);
        upload.setAcceptedFileTypes(".xlsx");
        upload.setMaxFiles(1);
        int maxFileSizeInBytes = 2 * 1024 * 1024;
        upload.setMaxFileSize(maxFileSizeInBytes);

        final var i18n = new UploadI18N();
        i18n.setAddFiles(new UploadI18N.AddFiles().setOne("Seleccione un archivo"));
        upload.setI18n(i18n);

        upload.addSucceededListener(event -> {
            final var inputStream = buffer.getInputStream();

            final var dialog = new Dialog();

            final var comboBox = new ComboBox<String>();
            comboBox.setItems(buildingIds);
            comboBox.setPlaceholder(Labels.Apartment.BUILDING_LABEL);
            comboBox.setClearButtonVisible(false);
            comboBox.setAutoOpen(true);

            final var processBtn = new Button("Procesar");
            processBtn.setEnabled(false);
            processBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

            final var cancelBtn = new Button(Labels.CANCEL);
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            comboBox.addValueChangeListener(e -> processBtn.setEnabled(true));

            cancelBtn.addClickListener(e -> {
                dialog.close();
                upload.clearFileList();
            });

            processBtn.addClickListener(v -> {

                processBtn.setEnabled(false);
                final var buildingId = comboBox.getValue();
                progressLayout.setVisible(true);

                loadCsvReceipt.load(buildingId, inputStream)
                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                        .subscribe(singleObserver(receipt -> {

                            uiAsyncAction(() -> {
                                saveReceiptInSession(receipt);
                                addEntity("file");
                            });

                        }, t -> {
                            showError(t);
                            uiAsyncAction(() -> progressLayout.setVisible(false));
                        }));


                upload.clearFileList();
                dialog.close();
            });

            dialog.setModal(true);
            dialog.add(comboBox);
            dialog.getFooter().add(processBtn);
            dialog.getFooter().add(cancelBtn);
            dialog.open();

        });

        upload.addFileRejectedListener(event -> viewHelper.showError(event.getErrorMessage()));

        upload.addFailedListener(event -> showError(event.getReason()));
        return upload;
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

        buildingComboBox.setPlaceholder(Labels.Apartment.BUILDING_LABEL);
        buildingComboBox.setClearButtonVisible(true);
        buildingComboBox.setAutoOpen(true);
        buildingComboBox.addValueChangeListener(o -> {
            if (!gridPaginator.goToFirstPage()) {
                updateGrid();
            }
        });


        HorizontalLayout toolbar = new HorizontalLayout(filterText, buildingComboBox, addEntityButton, upload(), countText);
        toolbar.addClassName("toolbar");
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return toolbar;
    }

    private void saveReceiptInSession(Receipt receipt) {
        VaadinSession.getCurrent().setAttribute("receipt", receipt);
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
