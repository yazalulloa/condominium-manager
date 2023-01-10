package kyo.yaz.condominium.manager.core.pdf;

import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.core.util.ObjectUtil;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
public class CreatePdfAptReceipt extends CreatePdfReceipt {

    private final String title;

    private final Path path;
    private final Receipt receipt;
    private final Apartment apartment;
    private final Building building;

    protected void addContent(Document document) {

        final var receiptValue = "VALOR RECIBO: ";
        final var usdExchangeRateTitle = "TASA DE CAMBIO AL DÍA %s: %s";
        document.add(new Paragraph(new Text(title()).setBold()).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(building().name()));
        document.add(new Paragraph("RIF: " + building().rif()));
        document.add(new Paragraph("MES A PAGAR: " + receipt().month() + " " + receipt().year()));
        document.add(new Paragraph(receipt().date().toString()));
        document.add(new Paragraph("PROPIETARIO: " + apartment().name()));
        document.add(new Paragraph("APT: " + apartment().apartmentId().number()));

        final var aptTotal = receipt().aptTotals().stream().filter(total -> total.number().equals(apartment().apartmentId().number()))
                .findFirst()
                .orElseThrow();

        final var payment = aptTotal.amount();

        if (ObjectUtil.aBoolean(building().fixedPay())) {

            if (building().currenciesToShowAmountToPay().isEmpty()) {
                final var paragraph = new Paragraph(new Text(receiptValue + building().mainCurrency().numberFormat().format(payment)).setBold().setUnderline());
                document.add(paragraph);
            } else {

                for (final Currency type : building().currenciesToShowAmountToPay()) {

                    if (type == building().mainCurrency()) {
                        final var paragraph = new Paragraph(new Text(receiptValue + building().mainCurrency().numberFormat().format(payment)).setUnderline());
                        document.add(paragraph);
                    } else {
                        switch (type) {

                            case VED -> {
                                final var decimal = payment.multiply(receipt().rate().rate()).setScale(2, RoundingMode.HALF_UP);

                                final var paragraph = new Paragraph(new Text(receiptValue + type.numberFormat().format(decimal)).setUnderline());
                                document.add(paragraph);
                            }
                            case USD -> {

                                final var decimal = payment.divide(receipt().rate().rate(), 2, RoundingMode.HALF_UP);

                                final var paragraph = new Paragraph(new Text(receiptValue + type.numberFormat().format(decimal)).setUnderline());
                                document.add(paragraph);
                            }
                        }
                    }
                }
            }

            final var paragraph = new Paragraph(new Text(receiptValue + building().mainCurrency().numberFormat().format(payment)).setBold().setUnderline());
            document.add(paragraph);
        } else {


            final var showVes = building().currenciesToShowAmountToPay().contains(Currency.VED);

            if (showVes) {
                final var paragraph = new Paragraph(new Text(receiptValue + Currency.VED.numberFormat().format(payment)).setUnderline());
                document.add(paragraph);
            }

            if (building().mainCurrency() == Currency.VED) {

                if (showVes) {
                    document.add(new Paragraph(String.format(usdExchangeRateTitle, receipt().rate().dateOfRate(), ConvertUtil.format(receipt().rate().rate(), Currency.VED))));
                }

                final var usdReceiptValue = payment.divide(receipt().rate().rate(), 2, RoundingMode.HALF_UP);
                //payment.divide(BigDecimal.valueOf(usdExchangeRate), MathContext.UNLIMITED);
                document.add(new Paragraph(new Text(receiptValue + ConvertUtil.format(usdReceiptValue, Currency.USD))).setUnderline());
            }

            document.add(new Paragraph("ALIQUOTA: " + apartment().amountToPay()));
        }

        document.add(new Paragraph("\n"));
        final var commonExpenses = receipt().expenses().stream().filter(expense -> expense.type() == Expense.Type.COMMON)
                .toList();

        final var commonExpensesTable = expensesTable("TOTAL GASTOS COMUNES", receipt().totalCommonExpenses(), commonExpenses);

        document.add(new Paragraph(new Text("GASTOS COMUNES").setBold().setUnderline()));
        document.add(commonExpensesTable);

        document.add(new Paragraph(new Text("\n")));

        final var unCommonExpenses = receipt().expenses().stream().filter(expense -> expense.type() == Expense.Type.UNCOMMON)
                .toList();

        final var unCommonExpensesTable = expensesTable("TOTAL GASTOS NO COMUNES", receipt().totalUnCommonExpenses(), unCommonExpenses);

        document.add(new Paragraph(new Text("GASTOS NO COMUNES").setBold().setUnderline()));
        document.add(unCommonExpensesTable);


        document.add(new Paragraph(new Text("\n")));

        if (!aptTotal.extraCharges().isEmpty()) {
            final var div = new Div().setKeepTogether(true);
            div.add(new Paragraph("\n"));
            div.add(new Paragraph(new Text("CARGOS EXTRA").setBold().setUnderline()));

            final var table = PdfUtil.table(2);
            table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("DESCRIPCIÓN")));
            table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("MONTO")));
            aptTotal.extraCharges().forEach(charge -> {

                table.addCell(PdfUtil.tableCell().add(new Paragraph(charge.description())));
                table.addCell(PdfUtil.tableCell().add(new Paragraph(ConvertUtil.format(charge.amount(), charge.currency()))));
            });

            div.add(table);
            document.add(div);
        }

        document.add(new Paragraph(new Text("\n")));

        {
            final var div = new Div().setKeepTogether(true);

            div.add(new Paragraph(new Text("FONDO DE RESERVA").setBold().setUnderline()));


            document.add(div);
        }

        document.add(new Paragraph(new Text("\n")));

        {


            final var div = new Div().setKeepTogether(true);
            div.add(new Paragraph(new Text("DEUDAS").setBold().setUnderline()));

            final var table = debtTable(receipt().debts());
            div.add(table);

            final var receipts = receipt().debts().stream().map(Debt::receipts)
                    .reduce(Integer::sum)
                    .orElse(0);

            div.add(new Paragraph("NÚMERO DE RECIBOS: " + receipts));
            div.add(new Paragraph("DEUDA TOTAL: " + ConvertUtil.format(receipt().totalDebt(), building().debtCurrency())));
            document.add(div);
        }

    }


    private Table debtTable(List<Debt> debts) {


        final var bool = debts.stream().map(Debt::previousPaymentAmount).map(Objects::nonNull).reduce(Boolean::logicalOr).orElse(false);

        final var table = PdfUtil.table(bool ? 6 : 5);

        table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("APTO")));
        table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("PROPIETARIO")));
        table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("RECIBOS")));
        table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("DEUDA")));
        table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("DESCRIPCIÓN")));

        if (bool) {
            table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("ABONO")));
        }

        debts.forEach(debt -> {
            table.addCell(PdfUtil.tableCell().add(new Paragraph(debt.aptNumber())));
            table.addCell(PdfUtil.tableCell().add(new Paragraph(debt.name())));
            table.addCell(PdfUtil.tableCell().add(new Paragraph(String.valueOf(debt.receipts()))));
            table.addCell(PdfUtil.tableCell().add(new Paragraph(ConvertUtil.format(debt.amount(), building().debtCurrency()))));

            final var months = Optional.ofNullable(debt.months())
                    .orElseGet(Collections::emptySet)
                    .stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            final var previousPaymentAmount = Optional.ofNullable(debt.previousPaymentAmount())
                    .map(decimal -> ConvertUtil.format(decimal, debt.previousPaymentAmountCurrency()))
                    .orElse("");


            table.addCell(PdfUtil.tableCell().add(new Paragraph(!months.isEmpty() ? months : DecimalUtil.equalsToZero(debt.amount()) ? "SOLVENTE" : "")));
            if (bool) {
                table.addCell(PdfUtil.tableCell().add(new Paragraph(previousPaymentAmount)));
            }
        });

        return table;
    }

    private Table expensesTable(String totalTitle, BigDecimal total, List<Expense> expenses) {


        final var table = PdfUtil.table(2);

        table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("DESCRIPCIÓN")));
        table.addHeaderCell(PdfUtil.tableCell().add(new Paragraph("MONTO")));

        expenses.forEach(expense -> {
            final var description = PdfUtil.tableCell()
                    .setTextAlignment(TextAlignment.LEFT)
                    .add(new Paragraph(expense.description()));
            table.addCell(description);

            final var amount = PdfUtil.tableCell()
                    .setTextRenderingMode(PdfCanvasConstants.TextRenderingMode.FILL)
                    .setTextAlignment(TextAlignment.RIGHT);
            amount.add(new Paragraph(ConvertUtil.format(expense.amount(), expense.currency())));
            table.addCell(amount);
        });


        final var format = ConvertUtil.format(total, building().mainCurrency());

        final var description = PdfUtil.tableCell();
        description.setTextAlignment(TextAlignment.LEFT);
        description.add(new Paragraph(totalTitle));
        table.addCell(description);

        final var amount = PdfUtil.tableCell();
        amount.setTextAlignment(TextAlignment.RIGHT);
        amount.add(new Paragraph(format));
        table.addCell(amount);

        return table;

    }


}
