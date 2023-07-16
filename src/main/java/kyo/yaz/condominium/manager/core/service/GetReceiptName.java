package kyo.yaz.condominium.manager.core.service;

import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetReceiptName {

    private final TranslationProvider translationProvider;

    public String zipFileName(Receipt receipt) {
        return fileName(receipt) + ".zip";
    }

    public String fileName(Receipt receipt) {
        final var month = translationProvider.translate(receipt.month().name());
        return "%s_%s_%s".formatted(receipt.buildingId(), month, receipt.date());
    }

}
