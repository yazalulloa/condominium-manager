package kyo.yaz.condominium.manager.core.parser;

import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.service.entity.SequenceService;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Component()
public class BcvUsdRateParser {

    private final SequenceService sequenceService;

    @Autowired
    public BcvUsdRateParser(SequenceService sequenceService) {
        this.sequenceService = sequenceService;
    }


    public Mono<Rate> parse(Document document) {
        return Mono.fromCallable(() -> {
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
                    .build();

            /*return sequenceService.nextSequence(Sequence.Type.RATES)
                    .map(id -> {

                    });
*/
        });

    }

    public Mono<Rate> parse(String html) {
        return Mono.defer(() -> {
            final var document = Jsoup.parse(html);
            return parse(document);
        });


    }
}
