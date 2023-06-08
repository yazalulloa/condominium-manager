package kyo.yaz.condominium.manager.core.domain.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
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
public class TelegramMessage {

  @JsonProperty
  private final long messageId;

  @JsonProperty
  private final TelegramUser from;

  @JsonProperty
  private final Chat senderChat;

  @JsonProperty
  private final long date;

  @JsonProperty
  private final TelegramUser forwardFrom;

  @JsonProperty
  private final Chat forwardFromChat;

  @JsonProperty
  private final long forwardFromMessageId;

  @JsonProperty
  private final String forwardSignature;

  @JsonProperty
  private final String forwardSenderName;

  @JsonProperty
  private final long forwardDate;

  @JsonProperty
  private final boolean isAutomaticForward;

  @JsonProperty
  private final TelegramMessage replyToMessage;

  @JsonProperty
  private final TelegramUser viaBot;

  @JsonProperty
  private final long editDate;

  @JsonProperty
  private final boolean hasProtectedContent;

  @JsonProperty
  private final String mediaGroupId;

  @JsonProperty
  private final String authorSignature;

  @JsonProperty
  private final String text;

  @JsonProperty
  private final List<EntitiesItem> entities;

  @JsonProperty
  private final TelegramUser newChatParticipant;

  @JsonProperty
  private final TelegramUser newChatMember;

  @JsonProperty
  private final List<TelegramUser> newChatMembers;

  @JsonProperty
  private final TelegramUser leftChatParticipant;

  @JsonProperty
  private final TelegramUser leftChatMember;

  @JsonProperty
  private final Chat chat;

  @JsonProperty
  private final InlineKeyboardMarkup replyMarkup;


}