package kyo.yaz.condominium.manager.ui.views.receipt.pdf;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
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
import jakarta.annotation.security.PermitAll;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.core.service.GetPdfItems;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.component.LazyComponent;
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

    private final VerticalLayout progressContainer = new VerticalLayout();
    private final ProgressLayout progressLayout = new ProgressLayout();
    private final GetPdfItems getPdfItems;

    @Autowired
    public ReceipPdfView(GetPdfItems getPdfItems) {
        this.getPdfItems = getPdfItems;
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

        progressContainer.add(progressLayout);
        progressContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        progressContainer.setAlignSelf(FlexComponent.Alignment.CENTER);
        progressContainer.setSizeFull();
        progressContainer.setMargin(true);

        add(horizontalLayout, progressContainer);
        init();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        getPdfItems.delete();
    }

    public void init() {
        final var receipt = (Receipt) VaadinSession.getCurrent().getAttribute("receipt");

        if (receipt == null) {
            navigate(ReceiptView.class);
            return;
        }

        progressLayout.setWidth(50, Unit.PERCENTAGE);
        progressLayout.setProgressText("Creando archivos");
        progressLayout.progressBar().setIndeterminate(true);
        progressLayout.setVisible(true);
        getPdfItems.setPlConsumer(c -> uiAsyncAction(() -> c.accept(progressLayout)));

        getPdfItems.pdfItems(receipt)
                .observeOn(Schedulers.io())
                .subscribe(singleObserver(list -> {
                    final var tabSheet = new TabSheet();
                    tabSheet.setSizeFull();

                    list.forEach(item -> tabSheet.add(item.id(), lazyComponent(item)));

                    uiAsyncAction(() -> {
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
