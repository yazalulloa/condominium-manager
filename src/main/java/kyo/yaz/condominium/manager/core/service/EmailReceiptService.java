package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.core.domain.ReceiptEmailRequest;
import kyo.yaz.condominium.manager.core.domain.SendEmailRequest;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.core.service.entity.EmailConfigService;
import kyo.yaz.condominium.manager.core.util.GmailUtil;
import kyo.yaz.condominium.manager.core.util.MimeMessageUtil;
import kyo.yaz.condominium.manager.core.verticle.SendEmailVerticle;
import kyo.yaz.condominium.manager.core.vertx.VertxHandler;
import kyo.yaz.condominium.manager.ui.views.domain.EmailAptReceiptRequest;
import kyo.yaz.condominium.manager.ui.views.receipt.domain.ReceiptPdfProgressState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailReceiptService {

    private final VertxHandler vertxHandler;
    private final TranslationProvider translationProvider;
    private final EmailConfigService emailConfigService;
    private final BehaviorSubject<ReceiptPdfProgressState> asyncSubject = BehaviorSubject.create();

    private Single<List<Single<ReceiptEmailRequest>>> sendEmails(EmailAptReceiptRequest request, List<PdfReceiptItem> list) {

        return emailConfigService.get(request.building().emailConfig())
                .flatMap(emailConfig -> {


                    final var subject = request.subject() + " %s 2022 Adm. %s APT: %s";

                    return Observable.fromIterable(list)
                            .filter(pdfReceipt -> pdfReceipt.emails() != null && !pdfReceipt.emails().isEmpty())
                            .map(pdfReceipt -> {

                                final var month = translationProvider.getTranslation(request.receipt().month().name(),
                                        translationProvider.LOCALE_ES);

                                final var emailRequest = ReceiptEmailRequest.builder()
                                        .from(emailConfig.from())
                                        .to(pdfReceipt.emails())
                                        //.to(Set.of("yzlup2@gmail.com"))
                                        .subject(subject.formatted(month, pdfReceipt.buildingName(),
                                                pdfReceipt.id()))
                                        .text(request.message())
                                        .files(Set.of(pdfReceipt.path().toString()))
                                        .build();

                                final var mimeMessage = MimeMessageUtil.createEmail(emailRequest);
                                final var message = GmailUtil.mimeMessageToBase64(mimeMessage);

                                final var sendEmailRequest = SendEmailRequest.builder()
                                        .emailConfig(emailConfig)
                                        .message(message)
                                        .build();

                                return vertxHandler.get(SendEmailVerticle.SEND, sendEmailRequest)
                                        .ignoreElement()
                                        .toSingleDefault(emailRequest);
                            })
                            .toList();
                });
    }
}
