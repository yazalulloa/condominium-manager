package kyo.yaz.condominium.manager.core.parser;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.zip.CRC32;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.domain.HttpClientResponse;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
public class BcvUsdRateParser {

  public Rate parse(HttpClientResponse httpResponse) {

    final var html = httpResponse.body().toString();

    final var crc32 = new CRC32();
    crc32.update(ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8)));
    final var hash = crc32.getValue();

    final var document = Jsoup.parse(html);

    final var dolar = document.getElementById("dolar");

    final var valDolar = dolar
        .childNode(1)
        .childNode(1)
        .childNode(3)
        .childNode(0)
        .childNode(0)
        .toString()
        .replace("\\.", "")
        .replace(",", ".")
        .trim();

    final var elementsByClass = document.getElementsByClass("pull-right dinpro center");

    final var date = elementsByClass.get(0)
        .childNode(1)
        .attr("content")
        .trim();

    final var rate = new BigDecimal(valDolar);
    final var dateOfRate = ZonedDateTime.parse(date).toLocalDate();

    final var etag = httpResponse.headers().get("etag");
    final var lastModified = httpResponse.headers().get("last-modified");

    return Rate.builder()
        .fromCurrency(Currency.USD)
        .toCurrency(Currency.VED)
        .rate(rate)
        .dateOfRate(dateOfRate)
        .source(Rate.Source.BCV)
        .hash(hash)
        .hashes(Set.of(hash))
        .etag(etag)
        .etags(Set.of(etag))
        .lastModified(lastModified)
        .build();
  }
}
