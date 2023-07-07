package kyo.yaz.condominium.manager.core.domain;

import java.nio.file.Path;
import java.util.Set;

public record PdfReceiptItem(Path path, String fileName, String id,
                             String buildingName,
                             Set<String> emails) {
}
