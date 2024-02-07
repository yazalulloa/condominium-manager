package kyo.yaz.condominium.manager.core.pdf;

import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

public class PdfUtil {

  public static Cell tableCell() {
    final var cell = new Cell();
    cell.setHorizontalAlignment(HorizontalAlignment.CENTER);
    cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
    cell.setTextAlignment(TextAlignment.CENTER);
    //cell.setBackgroundColor(Color.);
    cell.setBorder(Border.NO_BORDER);
    cell.setPadding(1);
    return cell;
  }

  public static Table table(int numColumns) {

    return new Table(numColumns, false)
        .setAutoLayout()
        .useAllAvailableWidth()
        //.setKeepTogether(true)
        ;
  }
}
