package kyo.yaz.condominium.manager.ui.views.receipt.pdf;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.Vertx;
import jakarta.annotation.security.PermitAll;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.core.util.StringUtil;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.component.LazyComponent;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.receipt.ReceiptView;
import kyo.yaz.condominium.manager.ui.views.receipt.service.GetPdfReceipts;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle(ReceipPdfView.PAGE_TITLE)
@PermitAll
@Route("receipts/:receipt_id/pdfs")
public class ReceipPdfView extends BaseDiv {

  public static final String PAGE_TITLE = Labels.Receipt.PDF_VIEW_PAGE_TITLE;

  private final VerticalLayout progressContainer = new VerticalLayout();
  private final ProgressLayout progressLayout = new ProgressLayout();
  private final Anchor downloadAnchor = new Anchor();

  private final Vertx vertx;
  private final TranslationProvider translationProvider;
  private final GetPdfReceipts getPdfReceipts;

  private Runnable detachRunnable;
  private H5 title;

  @Autowired
  public ReceipPdfView(Vertx vertx, TranslationProvider translationProvider, GetPdfReceipts getPdfReceipts) {
    this.vertx = vertx;
    this.translationProvider = translationProvider;
    this.getPdfReceipts = getPdfReceipts;
    init();
  }

  private void init() {
    addClassName("receipt-pdf-view");
    setSizeFull();

    final var goBackBtn = new Button(new Icon(VaadinIcon.ARROW_LEFT));
    goBackBtn.setDisableOnClick(true);
    goBackBtn.addClickListener(e -> ui(ui -> ui.navigate(ReceiptView.class)));

    title = new H5("Recibo de Pago PDF ");
    title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE, LumoUtility.AlignContent.CENTER,
        LumoUtility.AlignSelf.CENTER, LumoUtility.AlignItems.CENTER);

    final var horizontalLayout = new HorizontalLayout(goBackBtn, title, downloadAnchor);

    progressContainer.add(progressLayout);
    progressContainer.setAlignItems(FlexComponent.Alignment.CENTER);
    progressContainer.setAlignSelf(FlexComponent.Alignment.CENTER);
    progressContainer.setSizeFull();
    progressContainer.setMargin(true);

    getPdfReceipts.asyncSubject()
        .subscribe(observerShowError(state -> uiAsyncAction(() -> progressLayout.setState(state))));

    add(horizontalLayout, progressContainer);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    initData();
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    if (detachRunnable != null) {
      detachRunnable.run();
    }

    super.onDetach(detachEvent);
  }

  public void initData() {
    final var receipt = (Receipt) VaadinSession.getCurrent().getAttribute("receipt");

    if (receipt == null) {
      navigate(ReceiptView.class);
      return;
    }

    progressLayout.setWidth(50, Unit.PERCENTAGE);
    progressLayout.setProgressText("Creando archivos");
    progressLayout.progressBar().setIndeterminate(true);
    progressLayout.setVisible(true);

    getPdfReceipts.pdfItems(receipt)
        .observeOn(Schedulers.io())
        .subscribe(singleObserver(list -> {

          detachRunnable = () -> vertx.runOnContext(
              v -> list.forEach(item -> vertx.fileSystem().delete(item.path().toString())));
          final var tabSheet = new TabSheet();
          tabSheet.setSizeFull();

          list.forEach(item -> tabSheet.add(item.id(), lazyComponent(item)));

          final var filePath = StringUtil.compressStr(getPdfReceipts.zipPath(receipt, list));

          final var month = translationProvider.translate(receipt.month().name());
          final var titleText = "Recibo de Pago PDF %s %s %s %s".formatted(receipt.buildingId(), month, receipt.date(),
              receipt.id());

          uiAsyncAction(() -> {
            title.setText(titleText);
            downloadAnchor.setHref("/file-download?path=" + filePath);
            downloadAnchor.getElement().setAttribute("download", true);
            downloadAnchor.add(new Button("Descargar todos"));
            remove(progressContainer);
            add(tabSheet);
            tabSheet.setSizeFull();
          });
        }));


  }


  private LazyComponent lazyComponent(PdfReceiptItem item) {
    final var component = new LazyComponent(() -> {

      final var streamResource = new StreamResource(item.fileName(), () -> {
        try {
          return new FileInputStream(item.path().toFile());
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      });

      final var pdfViewer = new PdfViewer();
      pdfViewer.setSizeFull();
      pdfViewer.setSrc(streamResource);
      return pdfViewer;
    });
    component.setSizeFull();
    return component;
  }


}
