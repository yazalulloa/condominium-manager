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
public class InlineKeyboardButton {

  @JsonProperty
  private final String text;

  @JsonProperty
  private final String url;

  @JsonProperty
  private final LoginUrl loginUrl;

  @JsonProperty
  private final String callbackData;

  @JsonProperty
  private final String switchInlineQuery;

  @JsonProperty
  private final String switchInlineQueryCurrentChat;

  @JsonProperty
  private final Boolean pay;

  public static InlineKeyboardButton url(String text, String url) {
    return InlineKeyboardButton.builder()
        .text(text)
        .url(url)
        .build();
  }

  public static InlineKeyboardButton loginUrl(String text, LoginUrl loginUrl) {
    return InlineKeyboardButton.builder()
        .text(text)
        .loginUrl(loginUrl)
        .build();
  }

  public static InlineKeyboardButton callbackData(String text, String callbackData) {
    return InlineKeyboardButton.builder()
        .text(text)
        .callbackData(callbackData)
        .build();
  }

  public static InlineKeyboardButton switchInlineQuery(String text, String switchInlineQuery) {
    return InlineKeyboardButton.builder()
        .text(text)
        .switchInlineQuery(switchInlineQuery)
        .build();
  }

  public static InlineKeyboardButton switchInlineQueryCurrentChat(String text, String switchInlineQueryCurrentChat) {
    return InlineKeyboardButton.builder()
        .text(text)
        .switchInlineQueryCurrentChat(switchInlineQueryCurrentChat)
        .build();
  }

  public static InlineKeyboardButton pay(String text) {
    return InlineKeyboardButton.builder()
        .text(text)
        .pay(true)
        .build();
  }

}
