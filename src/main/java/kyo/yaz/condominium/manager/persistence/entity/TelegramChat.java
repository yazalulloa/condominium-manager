package kyo.yaz.condominium.manager.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Set;
import kyo.yaz.condominium.manager.persistence.domain.NotificationEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Document("telegram_chats")
@EqualsAndHashCode
public class TelegramChat {

  @Id
  @JsonProperty
  private final TelegramChatId id;

  @JsonProperty
  private final long chatId;

  @JsonProperty
  private final String firstName;

  @JsonProperty
  private final String lastName;

  @JsonProperty
  private final String username;

  @JsonProperty
  private final User user;

  @JsonProperty
  private final String update;

  @JsonProperty
  private final ZonedDateTime createdAt;

  @JsonProperty
  private ZonedDateTime updatedAt;

  @JsonProperty
  private Set<NotificationEvent> notificationEvents;

  @Jacksonized
  @Builder(toBuilder = true)
  @Accessors(fluent = true)
  @ToString
  @Getter
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class TelegramChatId implements Serializable {

    @JsonProperty
    private final long chatId;
    @JsonProperty
    private final String userId;

  }

}
