package kyo.yaz.condominium.manager.ui.views.component;

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
