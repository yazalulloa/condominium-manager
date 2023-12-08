package kyo.yaz.condominium.manager.core.service.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import org.junit.jupiter.api.Test;

class ParseCsvTest {

  @Test
  void test() throws IOException {
    final var path = "/home/yaz/Downloads/KORAL FACTURA   NOV 23  YAZAL.xlsx";
    //final var path = "/home/yaz/Downloads/ANTONIETA FACTURA  JUNIO 23.xlsx";
    //final var path = "/home/yaz/Downloads/GLADYS FACTURA  MAY23 YAZAL.xlsx";
    final var csvReceipt = new ParseCsv().csvReceipt(new File(path));

//    final var commonExpenses = csvReceipt.expenses().stream()
//        .filter(e -> e.type() == Expense.Type.COMMON)
//        .toList();
//
//    final var total = commonExpenses.stream()
//        .peek(e -> {
//          System.out.println("Expense " + e.description() + " " + e.amount());
//        })
//        .map(Expense::amount)
//        .reduce(BigDecimal::add)
//        .orElseThrow();

    //System.out.println(Json.encodePrettily(csvReceipt));
    System.out.println(csvReceipt);
  }
}