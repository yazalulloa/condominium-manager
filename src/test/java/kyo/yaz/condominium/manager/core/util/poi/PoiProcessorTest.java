package kyo.yaz.condominium.manager.core.util.poi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import kyo.yaz.condominium.manager.core.service.csv.ParseCsv;
import kyo.yaz.condominium.manager.core.service.csv.ParseCsv.ExtraChargeKey;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class PoiProcessorTest {

  @Test
  void test() throws OpenXML4JException, IOException, ParserConfigurationException, SAXException {
    final var path = "/home/yaz/Downloads/KORAL FACTURA   OCTUBRE 2023 yazal.xlsx";
    final var list = new ArrayList<ExtraChargeKey>();

    final var csvReceipt = new ParseCsv().csvReceipt(new File(path));
    System.out.println(csvReceipt);

    /*for (ExtraChargeKey extraChargeKey : list) {
      System.out.println(extraChargeKey.description());
      for (ExtraCharge extraCharge : extraChargeKey.extraCharges) {
        System.out.println(extraCharge);
      }

      System.out.println();
    }*/
  }


}