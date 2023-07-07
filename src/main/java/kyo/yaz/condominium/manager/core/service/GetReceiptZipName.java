package kyo.yaz.condominium.manager.core.service;

import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetReceiptZipName {

    private final TranslationProvider translationProvider;
    public String fileName(Receipt receipt) {
        final var month = translationProvider.translate(receipt.month().name());
        return "%s_%s_%s.zip".formatted(receipt.buildingId(), month, receipt.date());
    }
}
