package kyo.yaz.condominium.manager.core.util;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;
import java.util.Set;
import kyo.yaz.condominium.manager.core.domain.ReceiptEmailRequest;

public class MimeMessageUtil {

  public static InternetAddress[] internetAddresses(Set<String> set) throws AddressException {
    final var addresses = new InternetAddress[set.size()];
    var i = 0;
    for (final String s : set) {
      addresses[i++] = new InternetAddress(s);
    }

    return addresses;
  }

  public static MimeMessage basicMimeMessage(ReceiptEmailRequest receiptEmailRequest) throws MessagingException {
    final var props = new Properties();
    final var session = Session.getDefaultInstance(props, null);

    final var mimeMessage = new MimeMessage(session);

    mimeMessage.setFrom(new InternetAddress(receiptEmailRequest.from()));

    mimeMessage.addRecipients(Message.RecipientType.TO, internetAddresses(receiptEmailRequest.to()));
    mimeMessage.addRecipients(Message.RecipientType.CC, internetAddresses(receiptEmailRequest.cc()));
    mimeMessage.addRecipients(Message.RecipientType.BCC, internetAddresses(receiptEmailRequest.bcc()));
    mimeMessage.setSubject(receiptEmailRequest.subject());
    return mimeMessage;
  }

  public static MimeMessage createEmail(ReceiptEmailRequest request) throws MessagingException {

    final var mimeMessage = basicMimeMessage(request);

    if (request.files().isEmpty()) {
      mimeMessage.setText(request.text());
    } else {
      final var multipart = new MimeMultipart();

      if (request.text() != null) {
        final var mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(request.text(), "text/plain");
        multipart.addBodyPart(mimeBodyPart);
      }

      for (String file : request.files()) {
        multipart.addBodyPart(mimeBodyPart(file));
      }

      mimeMessage.setContent(multipart);
    }

    return mimeMessage;
  }

  public static MimeBodyPart mimeBodyPart(String path) throws MessagingException {
    final var file = new File(path);
    final var bodyPart = new MimeBodyPart();
    final var source = new FileDataSource(file);
    bodyPart.setDataHandler(new DataHandler(source));
    bodyPart.setFileName(file.getName());
    return bodyPart;
  }
}
