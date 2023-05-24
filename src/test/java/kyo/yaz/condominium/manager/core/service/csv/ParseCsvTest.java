package kyo.yaz.condominium.manager.core.service.csv;

import io.vertx.core.json.Json;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

class ParseCsvTest {

    @Test
    void test() throws IOException {
        final var csvReceipt = new ParseCsv().csvReceipt(new FileInputStream("C:\\workspace\\personal\\marlene-app\\conf\\tulipanes\\prefacturacion.xlsx"));

        System.out.println(Json.encodePrettily(csvReceipt));
    }
}