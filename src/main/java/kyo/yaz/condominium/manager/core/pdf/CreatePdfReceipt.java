package kyo.yaz.condominium.manager.core.pdf;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;

public abstract class CreatePdfReceipt {

  public void createPdf() {
    try (final var writer = new PdfWriter(path().toFile())) {
      try (final var pdf = new PdfDocument(writer)) {
        try (final var document = new Document(pdf)) {

          document.setMargins(18, 18, 18, 18);
          document.setFontSize(10);
          addContent(document);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public abstract Path path();

  public String id() {
    return Optional.ofNullable(apartment())
        .map(Apartment::apartmentId)
        .map(Apartment.ApartmentId::number)
        .or(() -> Optional.ofNullable(building()).map(Building::id))
        .orElse(null);
  }

  public abstract Apartment apartment();

  public abstract Building building();

  protected abstract void addContent(Document document);
}
