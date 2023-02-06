package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.EmailRequest;
import kyo.yaz.condominium.manager.core.domain.SendEmailRequest;
import kyo.yaz.condominium.manager.core.pdf.CreatePdfReceipt;
import kyo.yaz.condominium.manager.core.util.GmailUtil;
import kyo.yaz.condominium.manager.core.util.MimeMessageUtil;
import kyo.yaz.condominium.manager.core.verticle.SendEmailVerticle;
import kyo.yaz.condominium.manager.core.vertx.VertxHandler;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class SendEmailReceipts {
    private final VertxHandler vertxHandler;
    private final CreatePdfReceiptService createPdfReceiptService;

    @Autowired
    public SendEmailReceipts(VertxHandler vertxHandler, CreatePdfReceiptService createPdfReceiptService) {
        this.vertxHandler = vertxHandler;
        this.createPdfReceiptService = createPdfReceiptService;
    }

    public Single<List<Completable>> send(Receipt receipt, List<CreatePdfReceipt> list) {

        final var to = "yzlup2@gmail.com";

        final var subject = "AVISO DE COBRO %s 2022 Adm. %s APT: %s";
        final var bodyText = "AVISO DE COBRO";

        return Observable.fromIterable(list)
                .filter(pdfReceipt -> pdfReceipt.apartment() != null)
                .map(pdfReceipt -> {

                    final var emailRequest = EmailRequest.builder()
                            .from(pdfReceipt.building().receiptEmailFrom().email())
                            .to(pdfReceipt.apartment().emails())
                            //.to(Set.of(to))
                            .subject(subject.formatted(receipt.month(), pdfReceipt.building().name(), pdfReceipt.apartment().apartmentId().number()))
                            .text(bodyText)
                            .files(Set.of(pdfReceipt.path().toString()))
                            .build();

                    final var mimeMessage = MimeMessageUtil.createEmail(emailRequest);
                    final var message = GmailUtil.mimeMessageToBase64(mimeMessage);

                    final var sendEmailRequest = SendEmailRequest.builder()
                            .receiptEmailFrom(pdfReceipt.building().receiptEmailFrom())
                            .message(message)
                            .build();

                    return vertxHandler.get(SendEmailVerticle.SEND, sendEmailRequest)
                            .ignoreElement();
                })
                .toList();
    }

    public Completable send(Receipt receipt) {

        return createPdfReceiptService.createFiles(receipt)
                .flatMap(list -> send(receipt, list))
                .flatMapCompletable(Completable::concat);

    }


}
