package kyo.yaz.condominium.manager.core.domain;

import java.nio.file.Path;

public record PdfReceiptItem(Path path, String fileName, String id) {
}
