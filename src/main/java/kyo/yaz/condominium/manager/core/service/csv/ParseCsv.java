package kyo.yaz.condominium.manager.core.service.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.core.util.PoiUtil;
import kyo.yaz.condominium.manager.core.util.poi.PoiProcessor;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import kyo.yaz.condominium.manager.ui.views.util.AppUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.SAXException;

@Slf4j
public class ParseCsv {

  private static final Map<String, Month> monthsMap = new HashMap<String, Month>();

  static {
    monthsMap.put("ENE", Month.JANUARY);
    monthsMap.put("FEB", Month.FEBRUARY);
    monthsMap.put("MAR", Month.MARCH);
    monthsMap.put("ABR", Month.APRIL);
    monthsMap.put("MAY", Month.MAY);
    monthsMap.put("JUN", Month.JUNE);
    monthsMap.put("JUL", Month.JULY);
    monthsMap.put("AUG", Month.AUGUST);
    monthsMap.put("AGO", Month.AUGUST);
    monthsMap.put("AGOS", Month.AUGUST);
    monthsMap.put("SEP", Month.SEPTEMBER);
    monthsMap.put("OCT", Month.OCTOBER);
    monthsMap.put("NOV", Month.NOVEMBER);
    monthsMap.put("DEC", Month.DECEMBER);
  }

  public List<Expense> expenses(Sheet sheet) {
    final var expenses = new LinkedList<Expense>();

    var type = Expense.Type.COMMON;
    for (final Row row : sheet) {

      final var list = PoiUtil.toList(row);

      if (type == Expense.Type.UNCOMMON && list.size() == 0) {
        break;
      }

      if (list.size() >= 1) {
        if (list.get(0).contains("GASTOS NO COMUNES")) {
          type = Expense.Type.UNCOMMON;
        }

      }

      if (list.size() >= 2) {
        final var description = list.get(0).replaceAll("\\s{2,}", " ").trim();
        final var amount = list.get(1).trim();

        if (description.contains("GASTOS_COMUNES")) {
          type = Expense.Type.UNCOMMON;
          continue;
        }

        if (!PoiUtil.isThereIsANumber(amount)) {
          continue;
        }

        expenses.add(Expense.builder()
            .description(description)
            .amount(PoiUtil.decimal(amount))
            .type(type)
            .currency(Currency.VED)
            .build());
      }
    }

    final var comparator = Comparator.comparing(Expense::type)
        //.thenComparing(Expense::description)
        ;

    return expenses.stream().sorted(comparator)
        .collect(Collectors.toCollection(LinkedList::new));
  }

  public List<Debt> debts(Sheet sheet) {
    final var debts = new LinkedList<Debt>();

    for (final Row row : sheet) {
      final var list = PoiUtil.toList(row);
      if (list.size() >= 4) {

        try {
          final var apt = PoiUtil.apt(list.get(0).trim());
          final var name = list.get(1).trim();
          final var receipts = list.get(2).trim();
          final var amount = list.get(3).trim();

          if (!amount.isEmpty() && !AppUtil.isNumeric(amount)) {
            continue;
          }

          final var status = list.size() > 4 ? list.get(4) : "";
          final var abono = list.size() > 5 ? list.get(5) : null;

          final var previousPaymentAmount = Optional.ofNullable(abono)
              .filter(str -> !str.equals("OJO"))
              .map(PoiUtil::decimal)
              .orElse(null);

          final var amountDecimal = amount.isEmpty() ? BigDecimal.ZERO : PoiUtil.decimal(amount);

          final var debt = Debt.builder()
              .aptNumber(apt)
              .name(name)
              .receipts(PoiUtil.decimal(receipts).intValue())
              .amount(amountDecimal)
              .months(months(status))
              .previousPaymentAmount(previousPaymentAmount)
              .build();

          debts.add(debt);
        } catch (Exception e) {
          throw new RuntimeException(sheet.getSheetName() + " " + row.getRowNum(), e);
        }
      }
    }

    return debts.stream().sorted(Comparator.comparing(Debt::aptNumber))
        .collect(Collectors.toCollection(LinkedList::new));
  }

  public List<ExtraCharge> extraCharges(Sheet sheet) {
    final var extraCharges = new LinkedList<ExtraCharge>();
    boolean skipFirst = false;
    for (final Row row : sheet) {
      final var list = PoiUtil.toList(row);
      if (list.size() >= 3) {
        if (!skipFirst) {
          skipFirst = true;
          continue;
        }

        final var apt = PoiUtil.apt(list.get(0).trim());
        final var description = list.get(1).trim();
        final var amount = list.get(2).trim();

        final var currencyType = Optional.of(list)
            .filter(l -> l.size() > 3)
            .map(l -> l.get(3))
            .map(String::trim)
            .map(Currency::valueOf)
            .orElse(Currency.VED);

        final var extraCharge = ExtraCharge.builder()
            .aptNumber(apt)
            .description(description)
            .amount(PoiUtil.decimal(amount))
            .currency(currencyType)
            .build();

        extraCharges.add(extraCharge);
      }
    }

    return extraCharges.stream().sorted(Comparator
            .comparing(ExtraCharge::description)
            .thenComparing(ExtraCharge::aptNumber)
        )
        .collect(Collectors.toCollection(LinkedList::new));
  }

  private Set<Month> months(String str) {
    final var split = str.split("/");
    if (split.length > 1) {
      return Arrays.stream(split)
          .map(monthsMap::get)
          .filter(Objects::nonNull)
          .collect(Collectors.toCollection(LinkedHashSet::new));

    }

    return Optional.ofNullable(monthsMap.get(str))
        .map(Collections::singleton)
        .orElseGet(Collections::emptySet);
  }

  public CsvReceipt csvReceipt(File file) throws IOException {
    CsvReceipt csvReceipt = null;
    boolean possibleExtraChargesSheet = false;
    try (final var workbook = new XSSFWorkbook(new FileInputStream(file))) {

      final var numberOfSheets = workbook.getNumberOfSheets();

      final var expensesSheet = workbook.getSheetAt(0);
      final var debtsSheet = workbook.getSheetAt(1);
      possibleExtraChargesSheet = numberOfSheets > 2 && workbook.getSheetAt(2) != null;
      final var reserveFundSheet = numberOfSheets > 3 ? workbook.getSheetAt(3) : null;

      final var extraChargesSheet = numberOfSheets > 4 ? workbook.getSheetAt(4) : null;

      final var expenses = expenses(expensesSheet);
      final var debts = debts(debtsSheet);
      final var extraCharges = Optional.ofNullable(extraChargesSheet)
          .map(this::extraCharges)
          .orElseGet(Collections::emptyList);

      csvReceipt = CsvReceipt.builder()
          .expenses(expenses)
          .debts(debts)
          .extraCharges(extraCharges)
          .build();


    }

    if (csvReceipt.extraCharges().isEmpty() && possibleExtraChargesSheet) {
      try {
        final var extraCharges = extraCharges(new FileInputStream(file));
        csvReceipt = csvReceipt.toBuilder()
            .extraCharges(extraCharges)
            .build();
      } catch (Exception e) {
        log.info("Error in extraCharges", e);
      }
    }

    return csvReceipt;
  }

  public List<ExtraCharge> extraCharges(InputStream inputStream)
      throws OpenXML4JException, IOException, ParserConfigurationException, SAXException {
    final var list = new ArrayList<ExtraChargeKey>();

    final var afterDesc = new AtomicBoolean(false);
    final var poiProcessor = new PoiProcessor(
        inputStream, 0, Set.of(2), poiPage -> {

      for (kyo.yaz.condominium.manager.core.util.poi.Row initRow : poiPage.rows()) {
        final var cellIterator = initRow.cells().values().iterator();

        while (cellIterator.hasNext()) {
          final var initCell = cellIterator.next();
          final var cellValue = initCell.value();
          final var cellColumn = initCell.column();

          if (cellValue.equals("APTO") || cellValue.equals("MONTO")) {
            afterDesc.set(true);
            break;
          }

          if (!afterDesc.get()) {
            list.stream().filter(key -> key.columnAddr.equals(cellColumn))
                .findFirst()
                .ifPresent(extraChargeKey -> {

                  if (afterDesc.get()) {
                    extraChargeKey.addExtraCharge(cellValue, null);
                  } else {
                    list.remove(extraChargeKey);
                  }

                });

            if (!afterDesc.get()) {
              list.add(new ExtraChargeKey(cellColumn, cellValue));
            }
          } else {
            list.stream().filter(key -> key.columnAddr.equals(cellColumn))
                .findFirst()
                .ifPresent(extraChargeKey -> {

                  final var amount = DecimalUtil.valueOf(cellIterator.next().value().replace(",", ""));
                  extraChargeKey.addExtraCharge(cellValue, amount);
                });
          }

        }

      }
    }
    );

    poiProcessor.streamFile();
    return list.stream().map(ExtraChargeKey::extraCharges).flatMap(Collection::stream)
        .sorted(Comparator
            .comparing(ExtraCharge::description)
            .thenComparing(ExtraCharge::aptNumber))
        .toList();
  }

  public record ExtraChargeKey(String columnAddr, String description, List<ExtraCharge> extraCharges) {

    public ExtraChargeKey(String columnAddr, String description) {
      this(columnAddr, description, new ArrayList<>());
    }

    public void addExtraCharge(String aptNumber, BigDecimal amount) {
      extraCharges().add(ExtraCharge.builder()
          .aptNumber(aptNumber)
          .description(description())
          .amount(amount)
          .currency(Currency.VED)
          .build());
    }

  }

}
