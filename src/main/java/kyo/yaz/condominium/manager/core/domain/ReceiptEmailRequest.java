package kyo.yaz.condominium.manager.core.domain;

import java.util.Collections;
import java.util.Set;
import lombok.Builder;
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
public class ReceiptEmailRequest {

  private final String aptNumber;

  private final int id;
  private final String from;
  @Builder.Default
  private final Set<String> to = Collections.emptySet();
  @Builder.Default
  private final Set<String> cc = Collections.emptySet();
  @Builder.Default
  private final Set<String> bcc = Collections.emptySet();
  private final String subject;
  private final String text;
  @Builder.Default
  private final Set<String> files = Collections.emptySet();
}
