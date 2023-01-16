package kyo.yaz.condominium.manager.ui.views.receipt;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.Vertx;
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
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.domain.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


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
    private final Vertx vertx;
    private final BuildingService buildingService;
    private final DeleteDialog deleteDialog = new DeleteDialog();
    private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);
    private final ReceiptService receiptService;
    private final LoadCsvReceipt loadCsvReceipt;
    private final ProgressLayout progressLayout = new ProgressLayout();
    private final CreatePdfReceiptService createPdfReceiptService;
    private final SendEmailReceipts sendEmailReceipts;

    @Autowired
    public ReceiptView(Vertx vertx, BuildingService buildingService, ReceiptService receiptService, LoadCsvReceipt loadCsvReceipt, CreatePdfReceiptService createPdfReceiptService, SendEmailReceipts sendEmailReceipts) {
        super();
        this.vertx = vertx;
        this.buildingService = buildingService;
        this.receiptService = receiptService;
        this.loadCsvReceipt = loadCsvReceipt;
        this.createPdfReceiptService = createPdfReceiptService;
        this.sendEmailReceipts = sendEmailReceipts;

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

        progressLayout.setVisible(false);

        add(getToolbar(), progressLayout, grid, gridPaginator);
    }

    private ComponentRenderer<Anchor, Receipt> downloadAnchor() {

        return new ComponentRenderer<>(Anchor::new, (anchor, item) -> {

            final var downloadButton = new Button(new Icon(VaadinIcon.DOWNLOAD_ALT));
            downloadButton.setDisableOnClick(true);

            downloadButton.addClickListener(v -> {

                progressLayout.setProgressText("Creando archivos");
                progressLayout.progressBar().setIndeterminate(true);
                progressLayout.setVisible(true);
            });

            downloadButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);

            anchor.setHref(new StreamResource(createPdfReceiptService.fileName(item), () -> {
                anchor.setEnabled(false);
                try {


                    final var path = createPdfReceiptService.zip(item)
                            .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                            .blockingGet();

                    uiAsyncAction(() -> progressLayout.setProgressText("Descargando"));

                    vertx.setTimer(TimeUnit.SECONDS.toMillis(2), l -> {
                        uiAsyncAction(() -> {
                            progressLayout.setVisible(false);
                            anchor.setEnabled(true);
                            downloadButton.setEnabled(true);
                        });
                    });

                    return new FileInputStream(path);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));

            anchor.getElement().setAttribute("download", true);
            anchor.add(downloadButton);
        });
    }

    private void configureGrid() {
        grid.addClassNames("apartments-grid");
        grid.setColumnReorderingAllowed(true);

        grid.addColumn(Receipt::id).setHeader(Labels.Receipt.ID_LABEL).setSortable(true).setKey(Labels.Receipt.ID_LABEL);
        grid.addColumn(Receipt::buildingId).setHeader(Labels.Receipt.BUILDING_LABEL).setSortable(true).setKey(Labels.Receipt.BUILDING_LABEL);
        grid.addColumn(Receipt::year).setHeader(Labels.Receipt.YEAR_LABEL).setSortable(true).setKey(Labels.Receipt.YEAR_LABEL);
        grid.addColumn(Receipt::month).setHeader(Labels.Receipt.MONTH_LABEL).setSortable(true).setKey(Labels.Receipt.MONTH_LABEL);
        grid.addColumn(Receipt::date).setHeader(Labels.Receipt.DATE_LABEL).setSortable(true).setKey(Labels.Receipt.DATE_LABEL);
        grid.addColumn(receipt -> ConvertUtil.format(receipt.totalCommonExpenses(), receipt.totalCommonExpensesCurrency())).setHeader(Labels.Receipt.EXPENSE_COMMON_LABEL);
        grid.addColumn(receipt -> ConvertUtil.format(receipt.totalUnCommonExpenses(), receipt.totalUnCommonExpensesCurrency())).setHeader(Labels.Receipt.EXPENSE_UNCOMMON_LABEL);
        grid.addColumn(Receipt::debtReceiptsAmount).setHeader(Labels.Receipt.DEBT_RECEIPT_TOTAL_NUMBER_LABEL).setSortable(true).setKey(Labels.Receipt.DEBT_RECEIPT_TOTAL_NUMBER_LABEL);
        grid.addColumn(Receipt::totalDebt).setHeader(Labels.Receipt.DEBT_RECEIPT_TOTAL_AMOUNT_LABEL).setSortable(true).setKey(Labels.Receipt.DEBT_RECEIPT_TOTAL_AMOUNT_LABEL);
        grid.addColumn(receipt -> ConvertUtil.format(receipt.rate().rate(), receipt.rate().toCurrency())).setHeader(Labels.Receipt.RATE_LABEL).setSortable(true).setKey(Labels.Receipt.RATE_LABEL);
        grid.addColumn(receipt -> DateUtil.formatVe(receipt.createdAt())).setHeader(Labels.Receipt.CREATED_AT_LABEL).setSortable(true).setKey(Labels.Receipt.CREATED_AT_LABEL);

        //final var menuBar = new MenuBar();


        grid.addColumn(downloadAnchor())
                .setHeader(Labels.DOWNLOAD)
                .setTextAlign(ColumnTextAlign.CENTER)
                // .setFrozenToEnd(true)
                .setFlexGrow(0);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.addComponentColumn(receipt -> {
            final var menuBar = new MenuBar();
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
            final var menuItem = menuBar.addItem("•••");
            menuItem.getElement().setAttribute("aria-label", "Mas opciones");

            final var subMenu = menuItem.getSubMenu();

            final var sendEmailMenu = subMenu.addItem(Labels.SEND_EMAIL, e -> sendEmails(receipt));
            sendEmailMenu.addComponentAsFirst(createIcon(VaadinIcon.ENVELOPE));

            final var copyMenu = subMenu.addItem(Labels.COPY, e -> copyReceipt(receipt));
            copyMenu.addComponentAsFirst(createIcon(VaadinIcon.COPY));

            final var deleteMenu = subMenu.addItem(Labels.DELETE, e -> deleteReceipt(receipt));
            deleteMenu.addComponentAsFirst(createIcon(VaadinIcon.TRASH));

            return menuBar;
        }).setWidth("70px").setFlexGrow(0);

        final var contextMenu = grid.addContextMenu();

        contextMenu.setDynamicContentHandler(Objects::nonNull);

        final var sendEmailMenu = contextMenu.addItem(Labels.SEND_EMAIL);
        sendEmailMenu.addComponentAsFirst(createIcon(VaadinIcon.ENVELOPE));
        sendEmailMenu.addMenuItemClickListener(e -> e.getItem().ifPresent(this::sendEmails));

        final var copyMenu = contextMenu.addItem(Labels.COPY);
        copyMenu.addComponentAsFirst(createIcon(VaadinIcon.COPY));
        copyMenu.addMenuItemClickListener(e -> e.getItem().ifPresent(this::copyReceipt));

        final var deleteMenu = contextMenu.addItem(Labels.DELETE);
        deleteMenu.addComponentAsFirst(createIcon(VaadinIcon.TRASH));
        deleteMenu.addMenuItemClickListener(e -> e.getItem().ifPresent(this::deleteReceipt));

        grid.setPageSize(gridPaginator.itemsPerPage());
        grid.setSizeFull();

        grid.addSelectionListener(selection -> {

            selection.getFirstSelectedItem()
                    .ifPresent(this::editEntity);
        });
    }

    private Component createIcon(VaadinIcon vaadinIcon) {
        Icon icon = vaadinIcon.create();
        icon.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("margin-inline-end", "var(--lumo-space-s")
                .set("padding", "var(--lumo-space-xs");
        return icon;
    }

    private void deleteReceipt(Receipt receipt) {
        deleteDialog.setText(Labels.Receipt.ASK_CONFIRMATION_DELETE.formatted(receipt.id(), receipt.buildingId(), receipt.date()));
        deleteDialog.setDeleteAction(() -> delete(receipt));
        deleteDialog.open();
    }

    private void copyReceipt(Receipt receipt) {
        final var newItem = receipt.toBuilder()
                .id(null)
                .createdAt(null)
                .updatedAt(null)
                .build();

        saveReceiptInSession(newItem);
        editEntity("copy");
    }

    private void sendEmails(Receipt receipt) {
        uiAsyncAction(() -> {
            progressLayout.setProgressText("Creando archivos");
            progressLayout.progressBar().setIndeterminate(true);
            progressLayout.setVisible(true);
        });

        createPdfReceiptService.createFiles(receipt)
                .observeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .flatMap(list -> sendEmailReceipts.send(receipt, list))
                .toFlowable()
                .flatMap(list -> {

                    uiAsyncAction(() -> {
                        progressLayout.progressBar().setIndeterminate(false);
                        progressLayout.progressBar().setMin(0);
                        progressLayout.progressBar().setMax(list.size());
                        progressLayout.progressBar().setValue(0);
                        progressLayout.setProgressText("Enviando emails %s/%s".formatted(0, list.size()));
                    });

                    final AtomicInteger i = new AtomicInteger(1);
                    final var singles = list.stream()
                            .map(c -> c.toSingleDefault(i.getAndIncrement()))
                            .collect(Collectors.toCollection(LinkedList::new));

                    return Single.concat(singles)
                            .doOnNext(integer -> {
                                uiAsyncAction(() -> {
                                    progressLayout.progressBar().setValue(integer);
                                    progressLayout.setProgressText("Enviando emails %s/%s".formatted(integer, list.size()));
                                });
                            });
                })
                .doAfterTerminate(() -> uiAsyncAction(() -> progressLayout.setVisible(false)))
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .ignoreElements()
                .subscribe(completableObserver());
    }

    private void delete(Receipt receipt) {
        receiptService.delete(receipt.id())
                .doAfterTerminate(this::updateGrid)
                .subscribe(completableObserver());
    }

    private Single<Runnable> setCount() {
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

        Single.zip(countMono, setBuildingsIds, (count, setBuildings) -> List.of(this::init, count, setBuildings))
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement()
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
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

                progressLayout.progressBar().setIndeterminate(true);
                progressLayout.setVisible(true);

                loadCsvReceipt.load(buildingId, inputStream)
                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                        .subscribe(singleObserver(receipt -> {

                            uiAsyncAction(() -> {
                                saveReceiptInSession(receipt);
                                editEntity("file");
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
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }

    private Completable refreshData() {

        final var countMono = setCount();

        final var updateGrid = entityList()
                .map(list -> (Runnable) () -> {

                    grid.setPageSize(gridPaginator.itemsPerPage());
                    grid.setItems(list);

                    grid.getDataProvider().refreshAll();
                });

        return Single.zip(countMono, updateGrid, List::of)
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement();
    }

    private Single<List<Receipt>> entityList() {
        return receiptService.list(buildingComboBox.getValue(), filterText.getValue(), gridPaginator.currentPage(), gridPaginator.itemsPerPage());
    }

    private void editEntity(Receipt receipt) {
        saveReceiptInSession(receipt);
        editEntity(String.valueOf(receipt.id()));
    }

    private void addEntity() {
        editEntity("new");
    }

    private void editEntity(String id) {
        grid.asSingleSelect().clear();
        ui(ui -> ui.navigate(EditReceiptView.class, new RouteParameters("receipt_id", id)));
    }


}
