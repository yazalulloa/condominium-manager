package kyo.yaz.condominium.manager.ui.views.receipt;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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
import com.vaadin.flow.server.VaadinSession;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.security.PermitAll;
import java.time.Month;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.core.service.SendEmailReceipts;
import kyo.yaz.condominium.manager.core.service.csv.LoadCsvReceipt;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.service.entity.ReceiptService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.component.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.domain.EmailAptReceiptRequest;
import kyo.yaz.condominium.manager.ui.views.receipt.pdf.ReceipPdfView;
import kyo.yaz.condominium.manager.ui.views.receipt.service.GetPdfReceipts;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.IconUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle(ReceiptView.PAGE_TITLE)
@PermitAll
@Route(value = "receipts", layout = MainLayout.class)
public class ReceiptView extends BaseVerticalLayout {

  public static final String PAGE_TITLE = Labels.Receipt.VIEW_PAGE_TITLE;
  private final Grid<Receipt> grid = new Grid<>();

  private final MultiSelectComboBox<Month> monthsPicker = ViewUtil.monthMultiComboBox();
  private final MultiSelectComboBox<String> buildingComboBox = new MultiSelectComboBox<>();

  private final Set<String> buildingIds = new LinkedHashSet<>();

  private final TextField filterText = new TextField();

  private final Text countText = new Text(null);
  private final Text totalCountText = new Text(null);
  private final Button addEntityButton = new Button(Labels.Receipt.ADD_BUTTON_LABEL);
  private final BuildingService buildingService;
  private final DeleteDialog deleteDialog = new DeleteDialog();
  private final ReceiptService receiptService;
  private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);
  private final LoadCsvReceipt loadCsvReceipt;
  private final ProgressLayout progressLayout = new ProgressLayout();
  private final SendEmailReceipts sendEmailReceipts;
  private final TranslationProvider translationProvider;
  private final EmailAptReceiptDialog emailAptReceiptDialog;
  private final GetPdfReceipts getPdfReceipts;

  @Autowired
  public ReceiptView(BuildingService buildingService, ReceiptService receiptService, LoadCsvReceipt loadCsvReceipt,
      SendEmailReceipts sendEmailReceipts,
      TranslationProvider translationProvider,
      EmailAptReceiptDialog emailAptReceiptDialog, GetPdfReceipts getPdfReceipts) {
    super();
    this.buildingService = buildingService;
    this.receiptService = receiptService;
    this.loadCsvReceipt = loadCsvReceipt;
    this.sendEmailReceipts = sendEmailReceipts;
    this.translationProvider = translationProvider;
    this.emailAptReceiptDialog = emailAptReceiptDialog;
    this.getPdfReceipts = getPdfReceipts;

    sendEmailReceipts.setPlConsumer(c -> uiAsyncAction(() -> c.accept(progressLayout)));
    init();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    initData();
  }

  private void init() {
    addClassName("receipt-view");
    setSizeFull();

    VaadinSession.getCurrent().setAttribute("receipt", null);

    // GRID CONFIGURATION

    grid.addClassNames("apartments-grid");

    grid.addComponentColumn(this::card);
    grid.setItemDetailsRenderer(new ComponentRenderer<>(receipt -> {

      final var rate = receipt.rate();

      return new Div(new Span(
          "%s %s %s %s %s".formatted(Labels.Receipt.RATE_LABEL, rate.rate(), rate.source(), rate.dateOfRate(),
              rate.createdAt())));
    }));
    //grid.setItemDetailsRenderer(createPersonDetailsRenderer());

    final var contextMenu = grid.addContextMenu();

    contextMenu.setDynamicContentHandler(Objects::nonNull);

    final var editMenu = contextMenu.addItem(Labels.EDIT);
    editMenu.addComponentAsFirst(createIcon(VaadinIcon.EDIT));
    editMenu.addMenuItemClickListener(e -> e.getItem().ifPresent(this::editEntity));

    final var sendEmailNowMenu = contextMenu.addItem(Labels.SEND_EMAIL_NOW);
    sendEmailNowMenu.addComponentAsFirst(createIcon(VaadinIcon.ENVELOPE));
    sendEmailNowMenu.addMenuItemClickListener(e -> e.getItem().ifPresent(this::sendEmails));

    final var sendEmailMenu = contextMenu.addItem(Labels.SEND_EMAIL);
    sendEmailMenu.addComponentAsFirst(createIcon(VaadinIcon.ENVELOPE));
    sendEmailMenu.addMenuItemClickListener(e -> e.getItem().ifPresent(receipt -> {
      emailAptReceiptDialog.setReceipt(receipt);
      emailAptReceiptDialog.open();
    }));

    final var copyMenu = contextMenu.addItem(Labels.COPY);
    copyMenu.addComponentAsFirst(createIcon(VaadinIcon.COPY));
    copyMenu.addMenuItemClickListener(e -> e.getItem().ifPresent(this::copyReceipt));

    final var deleteMenu = contextMenu.addItem(Labels.DELETE);
    deleteMenu.addComponentAsFirst(createIcon(VaadinIcon.TRASH));
    deleteMenu.addMenuItemClickListener(e -> e.getItem().ifPresent(this::deleteReceipt));

    final var viewPdfsMenu = contextMenu.addItem(Labels.VIEW_PDFS);
    viewPdfsMenu.addComponentAsFirst(createIcon(VaadinIcon.FILE));
    viewPdfsMenu.addMenuItemClickListener(e -> e.getItem().ifPresent(this::viewPdfs));

    grid.setPageSize(gridPaginator.itemsPerPage());
    grid.setSizeFull();

    grid.addItemDoubleClickListener(selection -> {
      editEntity(selection.getItem());
    });

    // TOOLBAR CONFIGURATION

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
    buildingComboBox.setAllowCustomValue(false);
    buildingComboBox.setAutoOpen(true);
    buildingComboBox.addValueChangeListener(o -> {
      if (!gridPaginator.goToFirstPage()) {
        updateGrid();
      }
    });

    monthsPicker.setPlaceholder(Labels.Receipt.MONTH_LABEL);
    monthsPicker.setClearButtonVisible(true);
    monthsPicker.setAllowCustomValue(false);
    monthsPicker.setAutoOpen(true);
    monthsPicker.setItemLabelGenerator(m -> this.translationProvider.translate(m.name()));
    monthsPicker.addValueChangeListener(o -> {
      if (!gridPaginator.goToFirstPage()) {
        updateGrid();
      }
    });

    final var toolbar = new HorizontalLayout(filterText, buildingComboBox, monthsPicker, addEntityButton, upload(),
        countText);
    toolbar.addClassName("toolbar");
    toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

    emailAptReceiptDialog.addListener(EmailAptReceiptDialog.SendEmailsEvent.class, e -> sendEmails(e.getObj()));

    getPdfReceipts.asyncSubject()
        .subscribe(observerShowError(state -> uiAsyncAction(() -> progressLayout.setState(state))));

    add(toolbar, progressLayout, grid, footer());
  }

  private Component footer() {
    final var footer = new Div(gridPaginator, totalCountText);
    footer.addClassName("footer");
    return footer;
  }

  private Component card(Receipt receipt) {
    final var div = new Div();
    div.addClassName("card");

    final var array = Stream.of(receipt.id(), receipt.buildingId(), receipt.year(),
            translationProvider.translate(receipt.month().name()), receipt.date())
        .map(Objects::toString)
        .map(Span::new)
        .toArray(Span[]::new);
    final var header = new Div(array);
    header.addClassName("header");

    div.add(header);
    div.add(cardBody(receipt));

    final var deleteBtn = new Button(IconUtil.trash());
    deleteBtn.addClickListener(v -> deleteReceipt(receipt));

    // download file with servlet
    final var anchor = new Anchor("/file-download?name=" + "test");
    anchor.getElement().setAttribute("download", true);
    anchor.add(new Button(new Icon(VaadinIcon.DOWNLOAD_ALT)));

    // MENU BAR

    final var menuBar = new MenuBar();
    menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
    final var menuItem = menuBar.addItem("•••");
    menuItem.getElement().setAttribute("aria-label", "Mas opciones");

    final var subMenu = menuItem.getSubMenu();

    final var editMenu = subMenu.addItem(Labels.EDIT, e -> editEntity(receipt));
    editMenu.addComponentAsFirst(createIcon(VaadinIcon.EDIT));

    final var sendEmailNowMenu = subMenu.addItem(Labels.SEND_EMAIL_NOW, e -> sendEmails(receipt));
    sendEmailNowMenu.addComponentAsFirst(createIcon(VaadinIcon.ENVELOPE));

    final var sendEmailMenu = subMenu.addItem(Labels.SEND_EMAIL, e -> {
      emailAptReceiptDialog.setReceipt(receipt);
      emailAptReceiptDialog.open();
    });
    sendEmailMenu.addComponentAsFirst(createIcon(VaadinIcon.ENVELOPE));

    final var copyMenu = subMenu.addItem(Labels.COPY, e -> copyReceipt(receipt));
    copyMenu.addComponentAsFirst(createIcon(VaadinIcon.COPY));

    final var deleteMenu = subMenu.addItem(Labels.DELETE, e -> deleteReceipt(receipt));
    deleteMenu.addComponentAsFirst(createIcon(VaadinIcon.TRASH));

    final var viewPdfsMenu = subMenu.addItem(Labels.VIEW_PDFS, e -> viewPdfs(receipt));
    viewPdfsMenu.addComponentAsFirst(createIcon(VaadinIcon.FILE));

    final var buttons = new Div(deleteBtn, menuBar, getPdfReceipts.fileDownloader(receipt)/*, anchor*/);
    buttons.addClassName("buttons");

    div.add(buttons);
    return div;
  }

  private Component cardBody(Receipt receipt) {
    final var div = new Div();
    div.addClassName("body");

    Map.of(
        Labels.Receipt.EXPENSE_COMMON_LABEL,
        ConvertUtil.format(receipt.totalCommonExpenses(), receipt.totalCommonExpensesCurrency()),
        Labels.Receipt.EXPENSE_UNCOMMON_LABEL,
        ConvertUtil.format(receipt.totalUnCommonExpenses(), receipt.totalUnCommonExpensesCurrency()),
        Labels.Receipt.DEBT_RECEIPT_TOTAL_NUMBER_LABEL, receipt.debtReceiptsAmount(),
        Labels.Receipt.DEBT_RECEIPT_TOTAL_AMOUNT_LABEL, receipt.totalDebt(),
        Labels.Receipt.RATE_LABEL, ConvertUtil.format(receipt.rate().rate(), receipt.rate().toCurrency()),
        Labels.Receipt.CREATED_AT_LABEL, DateUtil.formatVe(receipt.createdAt())

    ).forEach((label, value) -> div.add(new Span("%s: %s".formatted(label, value.toString()))));

    final var sentIcon = IconUtil.checkMarkOrCross(Optional.ofNullable(receipt.sent()).orElse(false));

    final var sent = new Span(new Span(Labels.Receipt.SENT_LABEL + ": "), sentIcon);
    if (receipt.lastSent() != null) {
      sentIcon.setTooltipText(DateUtil.formatVe(receipt.lastSent()));
    }

    div.add(sent);
    return div;
  }

  private void viewPdfs(Receipt receipt) {
    saveReceiptInSession(receipt);
    ui(ui -> ui.navigate(ReceipPdfView.class, new RouteParameters("receipt_id", receipt.id().toString())));
  }

  private Component createIcon(VaadinIcon vaadinIcon) {
    Icon icon = vaadinIcon.create();
    icon.getStyle().set("color", "var(--lumo-secondary-text-color)")
        .set("margin-inline-end", "var(--lumo-space-s")
        .set("padding", "var(--lumo-space-xs");
    return icon;
  }

  private void deleteReceipt(Receipt receipt) {
    deleteDialog.setText(
        Labels.Receipt.ASK_CONFIRMATION_DELETE.formatted(receipt.id(), receipt.buildingId(), receipt.date()));
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

  private void sendEmails(EmailAptReceiptRequest request) {
    uiAsyncAction(() -> {
      progressLayout.setProgressText("Creando archivos");
      progressLayout.progressBar().setIndeterminate(true);
      progressLayout.setVisible(true);
    });

    sendEmailReceipts.sendEmails(request)
        .andThen(receiptService.updateSent(request.receipt()))
        .andThen(refreshData())
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());

  }

  private void sendEmails(Receipt receipt) {
    uiAsyncAction(() -> {
      progressLayout.setProgressText("Creando archivos");
      progressLayout.progressBar().setIndeterminate(true);
      progressLayout.setVisible(true);
    });

    sendEmailReceipts.sendEmails(receipt)
        .andThen(receiptService.updateSent(receipt))
        .andThen(refreshData())
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }

  private void delete(Receipt receipt) {
    receiptService.delete(receipt.id())
        .doAfterTerminate(this::updateGrid)
        .subscribe(completableObserver());
  }

  private void initData() {

    Single.zip(paging(), buildingService.buildingIds(), (paging, set) ->
            (Runnable) () -> {
              buildingIds.addAll(set);
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

  private Upload upload() {
    final var buffer = new FileBuffer();
    final var upload = new Upload(buffer);
    upload.setDropAllowed(true);
    upload.setAutoUpload(true);
    upload.setAcceptedFileTypes(".xlsx");
    upload.setMaxFiles(1);
    int maxFileSizeInBytes = 3 * 1024 * 1024;
    upload.setMaxFileSize(maxFileSizeInBytes);

    final var i18n = new UploadI18N();
    i18n.setAddFiles(new UploadI18N.AddFiles().setOne("Seleccione un archivo"));
    upload.setI18n(i18n);

    upload.addSucceededListener(event -> {

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
        final var fileName = buffer.getFileData().getFileName();
        progressLayout.setProgressText("Procesando " + fileName);
        progressLayout.progressBar().setIndeterminate(true);
        progressLayout.setVisible(true);

        loadCsvReceipt.load(buildingId, buffer.getFileData().getFile())
            .subscribeOn(Schedulers.io())
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

  private void saveReceiptInSession(Receipt receipt) {
    VaadinSession.getCurrent().setAttribute("receipt", receipt);
  }

  private void setCountText(long queryCount, long totalCount) {
    countText.setText(String.format(Labels.Receipt.AMOUNT_OF_LABEL, queryCount));
    gridPaginator.set(queryCount, totalCount);
    totalCountText.setText(String.format("Recibos Totales: %d", totalCount));
  }

  private void updateGrid() {
    refreshData()
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }

  private Completable refreshData() {

    return paging()
        .map(paging -> (Runnable) () -> setItems(paging))
        .doOnSuccess(this::uiAsyncAction)
        .ignoreElement();
  }

  private void setItems(Paging<Receipt> paging) {
    grid.setPageSize(gridPaginator.itemsPerPage());
    grid.setItems(paging.results());

    setCountText(paging.queryCount(), paging.totalCount());

    grid.getDataProvider().refreshAll();
  }

  private Single<Paging<Receipt>> paging() {
    return receiptService.paging(buildingComboBox.getValue(), monthsPicker.getValue(), filterText.getValue(),
            gridPaginator.currentPage(),
            gridPaginator.itemsPerPage())
        .doOnSubscribe(d -> {
          uiAsyncAction(() -> {
            progressLayout.setProgressText("Buscando recibos");
            progressLayout.setVisible(true);
            progressLayout.progressBar().setIndeterminate(true);
          });
        })
        .doOnTerminate(() -> uiAsyncAction(() -> progressLayout.setVisible(false)));
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
