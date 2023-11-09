package kyo.yaz.condominium.manager.core.util.poi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public record PoiProcessor(
    InputStream inputStream, int pagingSize,
    Set<Integer> sheets,
    Consumer<PoiPage> consumer
) {

  public void streamFile() throws OpenXML4JException, IOException, SAXException, ParserConfigurationException {
//    try (final var opcPackage = OPCPackage.open(path, PackageAccess.READ)) {
    try (final var opcPackage = OPCPackage.open(inputStream)) {
      final var sharedStringsTable = new ReadOnlySharedStringsTable(opcPackage);
      final var xssfReader = new XSSFReader(opcPackage);
      final var styles = xssfReader.getStylesTable();
      final var iterator = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
      int index = 0;
      while (iterator.hasNext()) {
        try (InputStream stream = iterator.next()) {

          if (sheets == null || sheets.isEmpty() || sheets.contains(index)) {

            final var sheetName = iterator.getSheetName();
            int currentIndex = index;
            final var sheetHandler = new SheetContentsHandlerImpl(pagingSize, rows -> {
              consumer.accept(new PoiPage(sheetName, currentIndex, rows, rows.size()));
            });

            final var formatter = new DataFormatter(true);
            final var sheetSource = new InputSource(stream);

            final var sheetParser = XMLHelper.newXMLReader();
            final var handler = new XSSFSheetXMLHandler(styles, null, sharedStringsTable, sheetHandler, formatter, false);
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
            sheetHandler.executeConsumer();
          }
        }
        ++index;
      }
      opcPackage.revert();
    }
  }
}
