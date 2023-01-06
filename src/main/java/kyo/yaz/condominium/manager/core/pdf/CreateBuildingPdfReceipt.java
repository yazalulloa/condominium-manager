package kyo.yaz.condominium.manager.core.pdf;

import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.nio.file.Path;

@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
public class CreateBuildingPdfReceipt extends CreatePdfReceipt {
    private final Path path;
    private final Receipt receipt;
    private final Building building;

    @Override
    public Apartment apartment() {
        return null;
    }

    protected void addContent(Document document) {
        document.add(new Paragraph(new Text("AVISO DE COBRO").setBold()).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(building().name()));
        document.add(new Paragraph(building().rif()));
        document.add(new Paragraph("MES A PAGAR: " + receipt().month()));
        document.add(new Paragraph(receipt().date().toString()));
        document.add(new Paragraph("LISTADO A PAGAR"));

        final var showMultipleCurrenciesAmountToPay = building().currenciesToShowAmountToPay().size() > 1;

        if (showMultipleCurrenciesAmountToPay) {
            document.add(new Paragraph("TASA DE CAMBIO USD: " + Currency.USD.numberFormat().format(receipt().rate().rate())));
            document.add(new Paragraph("FECHA DE TASA DE CAMBIO: " + receipt().rate().dateOfRate()));

        }
        document.add(new Paragraph("\n"));

        final var table = PdfUtil.table(showMultipleCurrenciesAmountToPay ? 2 + building().currenciesToShowAmountToPay().size() : 3);
        table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("APTO")));
        table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("PROPIETARIOS")));

        if (showMultipleCurrenciesAmountToPay) {
            building().currenciesToShowAmountToPay().forEach(currencyType -> {
                table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("MONTO " + currencyType.name())));
            });
        } else {

            table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("MONTO")));
        }

        receipt().aptTotals().forEach(aptTotal -> {
            final var aptCell = PdfUtil.tableCell()
                    .setTextRenderingMode(PdfCanvasConstants.TextRenderingMode.FILL)
                    .setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(aptTotal.number()));

            table.addCell(aptCell);

            final var nameCell = PdfUtil.tableCell()
                    .setTextRenderingMode(PdfCanvasConstants.TextRenderingMode.FILL)
                    .setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(aptTotal.name()));

            table.addCell(nameCell);

            if (showMultipleCurrenciesAmountToPay) {
                building().currenciesToShowAmountToPay().forEach(currencyType -> {

                    final var amount = Currency.toCurrency(aptTotal.amount(), building().mainCurrency(), receipt().rate().rate(), currencyType);

                    final var amountCell = PdfUtil.tableCell()
                            .setTextRenderingMode(PdfCanvasConstants.TextRenderingMode.FILL)
                            .setTextAlignment(TextAlignment.CENTER)
                            .add(new Paragraph(currencyType.numberFormat().format(amount)));

                    table.addCell(amountCell);
                });
            } else {
                final var amountCell = PdfUtil.tableCell()
                        .setTextRenderingMode(PdfCanvasConstants.TextRenderingMode.FILL)
                        .setTextAlignment(TextAlignment.CENTER)
                        .add(new Paragraph(building().mainCurrency().numberFormat().format(aptTotal.amount())));

                table.addCell(amountCell);
            }


        });
        document.add(table);

        final var total = receipt().aptTotals().stream().map(Receipt.AptTotal::amount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        document.add(new Paragraph("TOTAL: " + building().mainCurrency().numberFormat().format(total)));
    }
}
