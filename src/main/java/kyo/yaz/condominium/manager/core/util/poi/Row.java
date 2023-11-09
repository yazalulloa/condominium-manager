package kyo.yaz.condominium.manager.core.util.poi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record Row(String row, int cellCount, Map<String, Cell> cells) {

  public Row(String row) {
    this(row, 0, new LinkedHashMap<>());
  }

  public void addCell(Cell cell) {
    cells().put(cell.column(), cell);
  }

  public int size() {
    return cells().size();
  }

  public String cellValue(String column) {
    return Optional.ofNullable(cells().get(column))
        .map(Cell::value)
        .orElse(null);
  }

  public Row newRow() {
    return new Row(this.row, cells.size(), cells);
  }
}
