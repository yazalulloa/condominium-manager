package kyo.yaz.condominium.manager.core.pdf;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
public class CreatePdfReceipt {

    private final String title; //"AVISO DE COBRO"

    private final String path;
    private final Receipt receipt;
    private final Apartment apartment;

    private final Building building;

    public void createPdf() throws FileNotFoundException {
        new File(path).mkdirs();
        final var writer = new PdfWriter(path);

        // Creating a PdfDocument object
        final var pdf = new PdfDocument(writer);

        try (final var document = new Document(pdf)) {

            document.setMargins(24, 24, 24, 24);
            document.setFontSize(10);
            addContent(document);
        }


    }

    public void addContent(Document document) {

        document.add(new Paragraph(new Text(title()).setBold()).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(building().name()));
        document.add(new Paragraph(building().rif()));
        document.add(new Paragraph("MES A PAGAR: " + receipt().month().name()));
        document.add(new Paragraph(receipt().date().toString()));
        document.add(new Paragraph("PROPIETARIO: " + apartment().name()));
        document.add(new Paragraph("APT: " + apartment().apartmentId().number()));

        final var ownExtraCharges = receipt.extraCharges().stream()
                .filter(extraCharge -> extraCharge.aptNumber().equals(apartment.apartmentId().number()))
                .filter(extraCharge -> DecimalUtil.greaterThanZero(extraCharge.amount()))
                .toList();

        /*if (aptInfo.fixedPay() != null) {

            final var payment = totalPayment(BigDecimal.valueOf(aptInfo.fixedPay()), currencyType, usdExchangeRate, ownExtraCharges, true)
                    .setScale(2, RoundingMode.HALF_UP);


            final var paragraph = new Paragraph(new Text(receiptValue + currencyType.numberFormat().format(payment)).setBold().setUnderline());
            document.add(paragraph);
            consumer.accept(payment);
            //document.add(new Paragraph(receiptValue + Util.veFormat.format(aptInfo.fixedPay() * usdExchangeRate)));
        } else {

            //document.add(new Paragraph("MONTO DE GASTOS NO COMUNES POR C/U: " + currencyType.numberFormat().format(unCommonPay)));
            final var aliquotAmount = Util.percentageOf(BigDecimal.valueOf(aptInfo.aliquot()), totalCommon);
            // document.add(new Paragraph("MONTO POR ALIQUOTA: " + currencyType.numberFormat().format(aliquotAmount)));
            final var beforePay = aliquotAmount.add(unCommonPay);//.setScale(2, RoundingMode.UP);
            final var payment = totalPayment(beforePay, currencyType, usdExchangeRate, ownExtraCharges, true).setScale(2, RoundingMode.HALF_UP);

            final var showVes = !buildingName.contains("TULIPANES") && !buildingName.contains("ANTONIETA");

            if (showVes) {
                final var paragraph = new Paragraph(new Text(receiptValue + currencyType.numberFormat().format(payment)).setUnderline());
                document.add(paragraph);
            }

            consumer.accept(payment);
            if (currencyType == CurrencyType.VES) {

                if (showVes) {
                    document.add(new Paragraph(String.format(usdExchangeRateTitle, usdExchangeDate, Util.veFormat.format(usdExchangeRate))));
                }

                final var usdReceiptValue = payment.divide(usdExchangeRate, 2, RoundingMode.HALF_UP);
                //payment.divide(BigDecimal.valueOf(usdExchangeRate), MathContext.UNLIMITED);
                document.add(new Paragraph(new Text(receiptValue + Util.usFormat.format(usdReceiptValue))).setUnderline());
            }

            document.add(new Paragraph("ALIQUOTA: " + aptInfo.aliquot()));
        }*/

        document.add(new Paragraph("\n"));
    }

    private Cell tableCell() {
        final var cell = new Cell();
        cell.setHorizontalAlignment(HorizontalAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setTextAlignment(TextAlignment.CENTER);
        //cell.setBackgroundColor(Color.);
        cell.setBorder(Border.NO_BORDER);
        cell.setPadding(1);
        return cell;
    }

    private Table debtTable(List<Debt> debts, Currency currencyType) {


        final var bool = debts.stream().map(Debt::previousPaymentAmount).map(Objects::nonNull).reduce(Boolean::logicalOr).orElse(false);

        final var table = getTable(bool ? 6 : 5);

        table.addHeaderCell(tableCell().add(new Paragraph("APTO")));
        table.addHeaderCell(tableCell().add(new Paragraph("PROPIETARIO")));
        table.addHeaderCell(tableCell().add(new Paragraph("RECIBOS")));
        table.addHeaderCell(tableCell().add(new Paragraph("DEUDA")));
        table.addHeaderCell(tableCell().add(new Paragraph("DESCRIPCIÓN")));

        if (bool) {
            table.addHeaderCell(tableCell().add(new Paragraph("ABONO")));
        }

        debts.forEach(debt -> {
            table.addCell(tableCell().add(new Paragraph(debt.aptNumber())));
            table.addCell(tableCell().add(new Paragraph(debt.name())));
            table.addCell(tableCell().add(new Paragraph(String.valueOf(debt.receipts()))));
            table.addCell(tableCell().add(new Paragraph(currencyType.numberFormat().format(debt.amount()))));

            final var months = Optional.ofNullable(debt.months())
                    .orElseGet(Collections::emptySet)
                    .stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            final var previousPaymentAmount = Optional.ofNullable(debt.previousPaymentAmount())
                    .map(decimal -> ConvertUtil.format(decimal, debt.previousPaymentAmountCurrency()))
                    .orElse("");


            table.addCell(tableCell().add(new Paragraph(!months.isEmpty() ? months : DecimalUtil.equalsToZero(debt.amount()) ? "SOLVENTE" : "")));
            if (bool) {
                table.addCell(tableCell().add(new Paragraph(previousPaymentAmount)));
            }
        });

        return table;
    }

    private Table getTable(int numColumns) {
        return new Table(numColumns, false)
                .setAutoLayout()
                .useAllAvailableWidth()
                //.setKeepTogether(true)
                ;
    }
}
