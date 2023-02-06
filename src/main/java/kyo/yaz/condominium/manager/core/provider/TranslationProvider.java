package kyo.yaz.condominium.manager.core.provider;

import com.vaadin.flow.i18n.I18NProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;

@Slf4j
@Component
public class TranslationProvider implements I18NProvider {

    public static final String BUNDLE_PREFIX = "i18n.translate";

    public final Locale LOCALE_ES = new Locale("es", "VE");
    public final Locale LOCALE_EN = new Locale("en", "US");

    private final List<Locale> locales = List.of(LOCALE_ES, LOCALE_EN);

    @Override
    public List<Locale> getProvidedLocales() {
        return locales;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        if (key == null) {
            log.warn("Got lang request for key with null value!");
            return "";
        }

        final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PREFIX, locale);

        String value;
        try {
            value = bundle.getString(key);
        } catch (final MissingResourceException e) {
            log.warn("Missing resource", e);
            return "!" + locale.getLanguage() + ": " + key;
        }
        if (params.length > 0) {
            value = MessageFormat.format(value, params);
        }
        return value;
    }
}
