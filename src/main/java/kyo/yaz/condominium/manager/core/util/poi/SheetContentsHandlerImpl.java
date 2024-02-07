package kyo.yaz.condominium.manager.core.util.poi;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import kyo.yaz.condominium.manager.core.util.PoiUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;

@Slf4j
public class SheetContentsHandlerImpl implements XSSFSheetXMLHandler.SheetContentsHandler {

  private final LinkedHashMap<String, Row> values = new LinkedHashMap<>();
  private final int pagingSize;
  private final Consumer<Collection<Row>> consumer;
  private boolean firstCellOfRow;
  private int currentRow = -1;
  private int currentCol = -1;

  public SheetContentsHandlerImpl(int pagingSize, Consumer<Collection<Row>> consumer) {
    this.pagingSize = pagingSize;
    this.consumer = consumer;
  }


  @Override
  public void startRow(int rowNum) {
    firstCellOfRow = true;
    currentRow = rowNum;
    currentCol = -1;

  }

  @Override
  public void endRow(int rowNum) {
    if (pagingSize > 0 && values.size() >= pagingSize) {
      executeConsumer();
    }
  }

  @Override
  public void cell(String cellReference, String formattedValue,
      XSSFComment comment) {
    if (firstCellOfRow) {
      firstCellOfRow = false;
    }

    if (cellReference == null) {
      cellReference = new CellAddress(currentRow, currentCol).formatAsString();
    }

    int thisCol = (new CellReference(cellReference)).getCol();

    if (formattedValue == null) {
      return;
    }

    currentCol = thisCol;

    if (formattedValue.startsWith("\"") && formattedValue.endsWith("\"")) {
      formattedValue = formattedValue.substring(1, formattedValue.length() - 1);
    }

    final var value = formattedValue.trim();

    if (value.isEmpty()) {
      return;
    }

    final var rowAddress = String.valueOf(currentRow);
    final var row = values.getOrDefault(rowAddress, new Row(rowAddress));
    final var cell = new Cell(cellReference, row.row(), PoiUtil.abcLetter(currentCol), value);
    row.addCell(cell);
    values.put(row.row(), row);

    try {
      //log.info("currentRow {} currentCol {} value: {}", currentRow, currentCol, formattedValue);

      //output.append(formattedValue);
    } catch (Exception e) {
      // let's remove quotes if they are already there

      //log.info("currentRow {} currentCol {} value: {}", currentRow, currentCol, formattedValue);
    }
  }

  public void executeConsumer() {
    if (!values.isEmpty()) {

      final var rows = values.values().stream().map(Row::newRow).toList();
      consumer.accept(rows);
    }
    values.clear();
  }
}
