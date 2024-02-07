package kyo.yaz.condominium.manager.ui.views.domain;

import com.google.common.base.CaseFormat;
import com.vaadin.flow.component.datepicker.DatePicker;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class I18nProvider {

  private static final Map<String, DatePicker.DatePickerI18n> DATE_PICKERS = new HashMap<>();

  private final TranslationProvider translationProvider;

  public DatePicker.DatePickerI18n datePickerI18n() {
    return datePickerI18n(ZoneId.systemDefault().toString());
  }

  public DatePicker.DatePickerI18n datePickerI18n(String zoneId) {
    final var i18n = DATE_PICKERS.get(zoneId);

    log.info("ZoneId {}", zoneId);
    if (i18n == null && zoneId.equals("America/Caracas")) {
      DatePicker.DatePickerI18n singleFormatI18n = new DatePicker.DatePickerI18n();
      singleFormatI18n.setDateFormat("yyyy-MM-dd");

      final var months = Arrays.stream(Month.values()).map(Enum::name).map(translationProvider::translate)
          .map(str -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, str)).toList();

      final var weekDays = Arrays.stream(DayOfWeek.values()).map(Enum::name).map(translationProvider::translate)
          .map(str -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, str)).toList();

      final var weekdaysShort = weekDays.stream().map(str -> str.substring(0, 2)).toList();

      singleFormatI18n.setMonthNames(months);
      singleFormatI18n.setWeekdays(weekDays);
      singleFormatI18n.setWeekdaysShort(weekdaysShort);
      singleFormatI18n.setToday("Hoy");
      singleFormatI18n.setCancel("Cancelar");
      DATE_PICKERS.put(zoneId, singleFormatI18n);
      return singleFormatI18n;
    }
    return i18n;
  }
}
