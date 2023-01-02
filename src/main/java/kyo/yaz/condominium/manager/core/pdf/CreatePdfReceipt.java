package kyo.yaz.condominium.manager.core.pdf;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public abstract class CreatePdfReceipt {

    public void createPdf() throws FileNotFoundException {
        final var writer = new PdfWriter(path().toFile());

        // Creating a PdfDocument object
        final var pdf = new PdfDocument(writer);

        try (final var document = new Document(pdf)) {

            document.setMargins(24, 24, 24, 24);
            document.setFontSize(10);
            addContent(document);
        }

    }

    public abstract Path path();

    public abstract Apartment apartment();

    public abstract Building building();

    protected abstract void addContent(Document document);
}
