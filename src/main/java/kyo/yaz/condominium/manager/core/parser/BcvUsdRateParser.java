package kyo.yaz.condominium.manager.core.parser;

import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.zip.CRC32;

@Component
public class BcvUsdRateParser {

    public Rate parse(String html) {

        final var document = Jsoup.parse(html);
        final var crc32 = new CRC32();
        crc32.update(ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8)));
        final var hash = crc32.getValue();

        final var dolar = document.getElementById("dolar");

        final var valDolar = dolar
                .childNode(1)
                .childNode(1)
                .childNode(3)
                .childNode(0)
                .childNode(0)
                .toString()
                .replaceAll("\\.", "")
                .replaceAll(",", ".")
                .trim();

        final var elementsByClass = document.getElementsByClass("pull-right dinpro center");

        final var date = elementsByClass.get(0)
                .childNode(1)
                .attr("content")
                .trim();

        final var rate = new BigDecimal(valDolar);
        final var dateOfRate = ZonedDateTime.parse(date).toLocalDate();

        return Rate.builder()
                .fromCurrency(Currency.USD)
                .toCurrency(Currency.VED)
                .rate(rate)
                .dateOfRate(dateOfRate)
                .source(Rate.Source.BCV)
                .hash(hash)
                .build();
    }

/*  public Single<Rate> parse(String html) {
    return Single.defer(() -> {
      final var document = Jsoup.parse(html);
      return parse(document);
    });

  }*/
}
