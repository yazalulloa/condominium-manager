package kyo.yaz.condominium.manager.core.util;

import com.google.api.services.gmail.model.Message;
import org.apache.commons.codec.binary.Base64;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GmailUtil {

    public static String mimeMessageToBase64(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        return Base64.encodeBase64URLSafeString(bytes);
    }

    public static Message fromBase64(String base64Message) {
        Message message = new Message();
        message.setRaw(base64Message);
        return message;
    }

    public static Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {

        String encodedEmail = mimeMessageToBase64(emailContent);
        return fromBase64(encodedEmail);
    }
}
