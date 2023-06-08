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
public class TelegramUpdate {

  @JsonProperty
  private final long updateId;

  @JsonProperty
  private final TelegramMessage message;

  @JsonProperty
  private final TelegramMessage editedMessage;

  @JsonProperty
  private final TelegramMessage channelPost;

  @JsonProperty
  private final TelegramMessage editedChannelPost;

  @JsonProperty
  private final InlineQuery inlineQuery;

  @JsonProperty
  private final CallbackQuery callbackQuery;
}