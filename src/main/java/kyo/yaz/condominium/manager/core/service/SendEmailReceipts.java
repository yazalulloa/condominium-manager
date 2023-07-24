package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.core.domain.ReceiptEmailRequest;
import kyo.yaz.condominium.manager.core.domain.SendEmailRequest;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.service.entity.EmailConfigService;
import kyo.yaz.condominium.manager.core.util.GmailUtil;
import kyo.yaz.condominium.manager.core.util.MimeMessageUtil;
import kyo.yaz.condominium.manager.core.verticle.SendEmailVerticle;
import kyo.yaz.condominium.manager.core.vertx.VertxHandler;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.domain.EmailAptReceiptRequest;
import kyo.yaz.condominium.manager.ui.views.util.AppUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class SendEmailReceipts {

    private final VertxHandler vertxHandler;
    private final BuildingService buildingService;
    private final EmailConfigService emailConfigService;
    private final TranslationProvider translationProvider;
    private final ApartmentService apartmentService;
    private final GetPdfItems getPdfItems;
    private Consumer<Consumer<ProgressLayout>> plConsumer;

    public void setPlConsumer(Consumer<Consumer<ProgressLayout>> plConsumer) {
        this.plConsumer = plConsumer;
    }

    private void updateProgress(Consumer<ProgressLayout> consumer) {
        if (plConsumer != null) {
            plConsumer.accept(consumer);
        }
    }

    public Completable sendEmails(Receipt receipt) {
        getPdfItems.setPlConsumer(plConsumer);

        final var apartmentsByBuilding = apartmentService.rxApartmentsByBuilding(receipt.buildingId());
        final var buildingSingle = buildingService.get(receipt.buildingId());

        return Single.zip(buildingSingle, apartmentsByBuilding, (building, apartments) ->
                        sendEmails(new EmailAptReceiptRequest(AppUtil.DFLT_EMAIL_SUBJECT, AppUtil.DFLT_EMAIL_SUBJECT,
                                receipt, building, apartments)))
                .flatMapCompletable(c -> c);
    }


    public Completable sendEmails(EmailAptReceiptRequest emailAptReceiptRequest) {
        final var receipt = emailAptReceiptRequest.receipt();
        final var building = emailAptReceiptRequest.building();
        final var apartments = emailAptReceiptRequest.apartments();
        return getPdfItems.pdfItems(receipt, building, apartments)
                .doOnSuccess(l -> {
                    updateProgress(progressLayout -> {
                        progressLayout.setProgressText("Preparando para enviar");
                        progressLayout.progressBar().setIndeterminate(true);
                        progressLayout.setVisible(true);
                    });
                })
                .flatMap(list -> sendV2(emailAptReceiptRequest, list))

                .doOnSubscribe(d -> {
                    updateProgress(progressLayout -> {
                        progressLayout.setProgressText("Buscando data");
                        progressLayout.progressBar().setIndeterminate(true);
                        progressLayout.setVisible(true);

                    });
                })
                .toFlowable()
                .flatMap(list -> {
                    final var month = translationProvider.translate(receipt.month().name());
                    final var prefix = "Enviando %s %s %s".formatted(receipt.buildingId(), month, receipt.date());

                    final var progressText = prefix + " %s/%s";

                    updateProgress(progressLayout -> {
                        progressLayout.progressBar().setIndeterminate(false);
                        progressLayout.progressBar().setMin(0);
                        progressLayout.progressBar().setMax(list.size());
                        progressLayout.progressBar().setValue(0);
                        progressLayout.setProgressText(progressText.formatted(0, list.size()));
                    });

                    final AtomicInteger i = new AtomicInteger(1);

                    return Single.concat(list)
                            .doOnNext(request -> {
                                updateProgress(progressLayout -> {
                                    final var integer = i.getAndIncrement();
                                    progressLayout.progressBar().setValue(integer);

                                    progressLayout.setProgressText(progressText.formatted(integer, list.size()),
                                            "%s -> %s".formatted(request.from(), request.to()));
                                });
                            });
                })
                .doAfterTerminate(() -> updateProgress(progressLayout -> progressLayout.setVisible(false)))
                .ignoreElements();
    }

    private Single<List<Single<ReceiptEmailRequest>>> sendV2(EmailAptReceiptRequest request, List<PdfReceiptItem> list) {

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
