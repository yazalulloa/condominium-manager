package kyo.yaz.condominium.manager.core.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kyo.yaz.condominium.manager.core.domain.EmailRequest;
import kyo.yaz.condominium.manager.core.util.MimeMessageUtil;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

@Service
public class SendEmailReceipts {


    private final Gmail gmail;
    private final CreatePdfReceiptService createPdfReceiptService;

    @Autowired
    public SendEmailReceipts(Gmail gmail, CreatePdfReceiptService createPdfReceiptService) {
        this.gmail = gmail;
        this.createPdfReceiptService = createPdfReceiptService;
    }

    public Completable send(Receipt receipt) {

        return createPdfReceiptService.createFiles(receipt)
                .flatMapCompletable(list -> {

                    final var from = "yazalulloa@gmail.com";

                    final var subject = "AVISO DE COBRO %s 2022 Adm. %s APT: %s";
                    final var bodyText = "AVISO DE COBRO";

                    return Observable.fromIterable(list)
                            .filter(pdfReceipt -> pdfReceipt.apartment() != null)
                            .map(pdfReceipt -> {

                                final var emailRequest = EmailRequest.builder()
                                        .from(from)
                                        .to(Set.of(from))
                                        .subject(subject.formatted(receipt.month(), pdfReceipt.building().name(), pdfReceipt.apartment().apartmentId().number()))
                                        .text(bodyText)
                                        .files(Set.of(pdfReceipt.path().toString()))
                                        .build();

                                final var mimeMessage = MimeMessageUtil.createEmail(emailRequest);
                                final var message = createMessageWithEmail(mimeMessage);

                                final var send = gmail.users().messages().send(from, message);
                                return Completable.fromCallable(send::execute);
                            })
                            .toList()
                            .flatMapCompletable(Completable::concat);

                });

    }

    public static Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
