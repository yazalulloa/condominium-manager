package kyo.yaz.condominium.manager.core.util;

import com.google.common.base.CharMatcher;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PoiUtil {

    public static List<String> toList(Row row) {

        final var list = new LinkedList<String>();
        var empty = new AtomicBoolean(false);
        final var numberOfCells = row.getPhysicalNumberOfCells();
        row.cellIterator().forEachRemaining(cell -> {
            final var value = cellToString(cell);
            final var isEmpty = value.isEmpty();
            final var columnIndex = cell.getColumnIndex();
            if ((!isEmpty || (list.size() >= 2 && numberOfCells > columnIndex && !empty.get()))) {

                list.add(value);
            }
            empty.set(isEmpty);
        });

        while (list.peekLast() != null && list.peekLast().equals("")) {
            list.removeLast();
        }

        return list;
    }

    public static String cellToString(Cell cell) {
        switch (cell.getCellType()) {
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                final var numericCellValue = cell.getNumericCellValue();
                if (numericCellValue % 1 == 0) {
                    return String.valueOf(numericCellValue);
                }
                return BigDecimal.valueOf(numericCellValue).setScale(2, RoundingMode.DOWN).toPlainString();
            }
            //return String.valueOf(cell.getNumericCellValue());
            case STRING -> {
                return cell.getStringCellValue().trim();
            }
            case BLANK, FORMULA -> {
                //return String.valueOf(cell.getNumericCellValue());
                return "";
            }
            //case BOOLEAN, ERROR, _NONE,
            default -> throw new RuntimeException("INVALID_CELL_TYPE_" + cell.getCellType().name());
        }
    }

    public static String apt(String str) {
        return str.replaceAll("\\.0", "");
    }

    public static BigDecimal decimal(String str) {
        return new BigDecimal(toAmount(str));
    }

    public static String toAmount(String str) {
        final var point = str.lastIndexOf('.');
        final var comma = str.lastIndexOf(',');

        if (point == -1 && comma == -1) {
            return str;
        }

        if (point == -1) {
            final var count = CharMatcher.is(',').countIn(str);
            if (count == 1) {
                return str.replaceAll(",", ".");
            } else {
                return str.replaceAll(",", "");
            }
        }

        if (comma == -1) {
            final var count = CharMatcher.is('.').countIn(str);
            if (count == 1) {
                return str;
            } else {
                if (point == str.length() - 3) {
                    throw new RuntimeException("INVALID_AMOUNT");
                }

                return str.replaceAll("\\.", "");
            }
        }

        if (point > comma) {
            return str.replaceAll(",", "");
        } else {
            return str.replaceAll("\\.", "").replaceAll(",", ".");
        }
    }
}
