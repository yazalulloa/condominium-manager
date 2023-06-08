package kyo.yaz.condominium.manager.core.domain.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SendMessage {

  @JsonProperty
  private final Long chatId;

  @JsonProperty
  private final String text;

  @JsonProperty
  private final ParseMode parseMode;

  @JsonProperty
  private final Boolean disableWebPagePreview;

  @JsonProperty
  private final Boolean disableNotification;

  @JsonProperty
  private final Long replyToMessageId;

  @JsonProperty
  private final Boolean allowSendingWithoutReply;

  @JsonProperty
  private final Object replyMarkup;
}
