package kyo.yaz.condominium.manager.core.service.csv;

import io.vertx.core.json.Json;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

class ParseCsvTest {

    @Test
    void test() throws IOException {
        //final var path = "C:\\workspace\\personal\\marlene-app\\conf\\tulipanes\\prefacturacion.xlsx";
        final var path = "/home/yaz/Downloads/GLADYS FACTURA  MAY23 YAZAL.xlsx";
        final var csvReceipt = new ParseCsv().csvReceipt(new FileInputStream(path));

        System.out.println(Json.encodePrettily(csvReceipt));
    }
}