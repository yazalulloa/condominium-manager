package kyo.yaz.condominium.manager.core.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.json.jackson.DatabindCodec;
import kyo.yaz.condominium.manager.core.util.JacksonUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class PdfReceiptItemTest {

    @Test
    void test() throws JsonProcessingException {
        final var mapper = JacksonUtil.defaultConfig(DatabindCodec.mapper());

        final var item = new PdfReceiptItem(Path.of("C:\\Users\\Yaz\\marlene-dev\\yaz-condominium-manager\\tmp"), "file", "id");

        System.out.printf(mapper.writeValueAsString(item));
    }

}