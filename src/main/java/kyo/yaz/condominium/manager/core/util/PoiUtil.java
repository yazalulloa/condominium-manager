package kyo.yaz.condominium.manager.core.util;

import com.google.common.base.CharMatcher;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.parsers.ParserConfigurationException;
import kyo.yaz.condominium.manager.core.util.poi.SheetContentsHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Slf4j
public class PoiUtil {

  public static final Map<Integer, String> ABC_LETTERS = new LinkedHashMap<>();

  static {
    ABC_LETTERS.put(0, "A");
    ABC_LETTERS.put(1, "B");
    ABC_LETTERS.put(2, "C");
    ABC_LETTERS.put(3, "D");
    ABC_LETTERS.put(4, "E");
    ABC_LETTERS.put(5, "F");
    ABC_LETTERS.put(6, "G");
    ABC_LETTERS.put(7, "H");
    ABC_LETTERS.put(8, "I");
    ABC_LETTERS.put(9, "J");
    ABC_LETTERS.put(10, "K");
    ABC_LETTERS.put(11, "L");
    ABC_LETTERS.put(12, "M");
    ABC_LETTERS.put(13, "N");
    ABC_LETTERS.put(14, "O");
    ABC_LETTERS.put(15, "P");
    ABC_LETTERS.put(16, "Q");
    ABC_LETTERS.put(17, "R");
    ABC_LETTERS.put(18, "S");
    ABC_LETTERS.put(19, "T");
    ABC_LETTERS.put(20, "U");
    ABC_LETTERS.put(21, "V");
    ABC_LETTERS.put(22, "W");
    ABC_LETTERS.put(23, "X");
    ABC_LETTERS.put(24, "Y");
    ABC_LETTERS.put(25, "Z");
  }

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

  public static boolean isThereIsANumber(String str) {
    for (char character : str.toCharArray()) {
      final var isDigit = Character.isDigit(character);
      if (isDigit) {
        return true;
      }
    }

    return false;
  }

  public static BigDecimal decimal(String str) {

    try {
      return new BigDecimal(toAmount(str));
    } catch (Exception e) {
      final var msg = "FAILED TO PARSE %s".formatted(str);
      log.error(msg, str, e);
      throw new RuntimeException(msg, e);
    }
  }

  public static String toAmount(String str) {
    if (str.isEmpty()) {
      return "0";
    }

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



  public static String abcLetter(int i) {

    if (i <= 25) {
      return ABC_LETTERS.get(i);
    }

    var timesDivided = i / 25;
    var remainder = i % 25;

    timesDivided--;
    if (remainder != 0) {
      remainder--;
    } else {
      timesDivided--;
      remainder += 25;
    }

    if (timesDivided > 25) {
      return abcLetter(timesDivided) + ABC_LETTERS.get(remainder);
    }

    return ABC_LETTERS.get(timesDivided) + ABC_LETTERS.get(remainder);
  }
}
