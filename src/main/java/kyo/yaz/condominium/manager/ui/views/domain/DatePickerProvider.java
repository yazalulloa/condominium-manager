package kyo.yaz.condominium.manager.ui.views.domain;

import com.vaadin.flow.component.datepicker.DatePicker;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DatePickerProvider {

  private final I18nProvider i18nProvider;


  public DatePicker datePicker(String label) {
    return datePicker(label, DateUtil.VE_ZONE);
    //return datePicker(label, ZoneId.systemDefault());
    //return datePicker(label, ZoneOffset.UTC );

  }

  public DatePicker datePicker(String label, ZoneId zoneId) {

    final var datePicker = new DatePicker(label, LocalDate.now(zoneId));
    Optional.ofNullable(i18nProvider.datePickerI18n(zoneId.toString()))
        .ifPresent(datePicker::setI18n);

    return datePicker;
  }
}
