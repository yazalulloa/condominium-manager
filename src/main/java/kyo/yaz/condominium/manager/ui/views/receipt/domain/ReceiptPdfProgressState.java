package kyo.yaz.condominium.manager.ui.views.receipt.domain;

import kyo.yaz.condominium.manager.ui.views.domain.ProgressState;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@EqualsAndHashCode
public class ReceiptPdfProgressState implements ProgressState {

    private final String text;
    private final String endText;
    private final String subText;
    private final boolean indeterminate;
    private final double min;
    private final double max;
    private final double value;

    @Builder.Default
    private final boolean visible = true;

    public static ReceiptPdfProgressState visible(boolean visible) {
        return ReceiptPdfProgressState.builder()
                .visible(visible)
                .build();
    }

    public static ReceiptPdfProgressState ofIndeterminate(String text) {
        return ReceiptPdfProgressState.builder()
                .text(text)
                .indeterminate(true)
                .build();
    }
}
