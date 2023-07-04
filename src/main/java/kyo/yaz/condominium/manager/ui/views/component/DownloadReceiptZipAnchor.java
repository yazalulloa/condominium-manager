package kyo.yaz.condominium.manager.ui.views.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.server.StreamResource;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.actions.DownloadReceiptZipAction;

import java.io.FileInputStream;

/*public class DownloadReceiptZipAnchor implements SerializableBiConsumer<Anchor, Receipt> {

    private final DownloadReceiptZipAction<Receipt> action;

    public DownloadReceiptZipAnchor(DownloadReceiptZipAction<Receipt> action) {
        this.action = action;
    }

    @Override
    public void accept(Anchor anchor, Receipt item) {
        final var downloadButton = new Button(new Icon(VaadinIcon.DOWNLOAD_ALT));
        downloadButton.setDisableOnClick(true);

        downloadButton.addClickListener(v -> action.downloadBtnClicked());

        downloadButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);

        anchor.setHref(new StreamResource(action.fileName(item), () -> {
            anchor.setEnabled(false);
            try {
                final var path = action.filePath(item);

                action.downloadFinished(() -> {
                    anchor.setEnabled(true);
                    downloadButton.setEnabled(true);

                });

                return new FileInputStream(path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));

        anchor.getElement().setAttribute("download", true);
        anchor.add(downloadButton);
    }
}*/
