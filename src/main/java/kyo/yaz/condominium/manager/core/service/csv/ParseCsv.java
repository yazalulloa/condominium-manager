package kyo.yaz.condominium.manager.core.service.csv;

import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.util.PoiUtil;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.math.BigDecimal;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

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
            if (list.size() >= 2) {
                final var description = list.get(0).replaceAll("\\s{2,}", " ").trim();
                final var amount = list.get(1).trim();

                if (description.contains("GASTOS_COMUNES")) {
                    type = Expense.Type.UNCOMMON;
                    continue;
                }

                expenses.add(Expense.builder()
                        .description(description)
                        .amount(new BigDecimal(amount))
                        .type(type)
                        .currency(Currency.VED)
                        .build());
            }
        }

        final var comparator = Comparator.comparing(Expense::type).thenComparing(Expense::description);

        return expenses.stream().sorted(comparator)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public List<Debt> debts(Sheet sheet) {
        final var debts = new LinkedList<Debt>();

        boolean skipFirst = false;
        for (final Row row : sheet) {
            final var list = PoiUtil.toList(row);
            if (list.size() >= 4) {
                if (!skipFirst) {
                    skipFirst = true;
                    continue;
                }
                final var apt = PoiUtil.apt(list.get(0).trim());
                final var name = list.get(1).trim();
                final var receipts = list.get(2).trim();
                final var amount = list.get(3).trim();
                final var status = list.size() > 4 ? list.get(4) : "";
                final var abono = list.size() > 5 ? list.get(5) : null;

                final var previousPaymentAmount = Optional.ofNullable(abono)
                        .map(PoiUtil::decimal)
                        .orElse(null);


                final var debt = Debt.builder()
                        .aptNumber(apt)
                        .name(name)
                        .receipts(PoiUtil.decimal(receipts).intValue())
                        .amount(PoiUtil.decimal(amount))
                        .months(months(status))
                        .previousPaymentAmount(previousPaymentAmount)
                        .build();

                debts.add(debt);
            }
        }

        return debts.stream().sorted(Comparator.comparing(Debt::aptNumber)).collect(Collectors.toCollection(LinkedList::new));
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
                        .orElse(null);

                final var extraCharge = ExtraCharge.builder()
                        .aptNumber(apt)
                        .description(description)
                        .amount(PoiUtil.decimal(amount))
                        .currency(currencyType)
                        .build();

                extraCharges.add(extraCharge);
            }
        }

        return extraCharges.stream().sorted(Comparator.comparing(ExtraCharge::aptNumber)).collect(Collectors.toCollection(LinkedList::new));
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


}
