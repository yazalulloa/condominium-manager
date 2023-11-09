package kyo.yaz.condominium.manager.core.util.poi;

import java.util.Collection;

public record PoiPage(
    String sheetName,
    int sheetIndex,
    Collection<Row> rows,
    int rowsCount
) {

}
