package kyo.yaz.condominium.manager.ui.views.receipt.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.security.PermitAll;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.core.service.CreatePdfReceiptService;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.domain.LazyComponent;
import kyo.yaz.condominium.manager.ui.views.receipt.ReceiptView;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@PageTitle(ReceipPdfView.PAGE_TITLE)
@PermitAll
@Route("receipt_pdf")
public class ReceipPdfView extends BaseDiv {


    public static final String PAGE_TITLE = Labels.Receipt.PDF_VIEW_PAGE_TITLE;


    private final ProgressLayout progressLayout = new ProgressLayout();
    private final CreatePdfReceiptService createPdfReceiptService;
    private final ObjectMapper mapper;


    //private Receipt receipt;

    @Autowired
    public ReceipPdfView(CreatePdfReceiptService createPdfReceiptService, ObjectMapper mapper) {
        this.createPdfReceiptService = createPdfReceiptService;
        this.mapper = mapper;
        addClassName("receip-pdf-view");
        setSizeFull();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        final var goBackBtn = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        goBackBtn.setDisableOnClick(true);
        goBackBtn.addClickListener(e -> ui(ui -> ui.navigate(ReceiptView.class)));

        final var title = new H5("Recibo de Pago PDF");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE, LumoUtility.AlignContent.CENTER, LumoUtility.AlignSelf.CENTER, LumoUtility.AlignItems.CENTER);
        final var horizontalLayout = new HorizontalLayout(goBackBtn, title);
        add(horizontalLayout, progressLayout);
        init();
    }

    public void init() {
        progressLayout.setProgressText("Creando archivos");
        progressLayout.progressBar().setIndeterminate(true);
        progressLayout.setVisible(true);

        /*Single.fromCallable(() -> {

                    try (final var iterator = mapper.readerFor(PdfReceiptItem.class).<PdfReceiptItem>readValues(HardCode.PDF_RECEIPT_ITEMS)) {
                        return iterator.readAll()
                                .stream().sorted(ConvertUtil.pdfReceiptItemComparator())
                                .collect(Collectors.toCollection(LinkedList::new));
                    }
                })*/

        createPdfReceiptService.pdfItems((Receipt) VaadinSession.getCurrent().getAttribute("receipt"))
                .observeOn(Schedulers.io())
                .subscribe(singleObserver(list -> {
                    final var tabSheet = new TabSheet();
                    tabSheet.setSizeFull();

                    list.forEach(item -> tabSheet.add(item.id(), lazyComponent(item)));

                    uiAsyncAction(() -> {
                        progressLayout.setVisible(false);
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
