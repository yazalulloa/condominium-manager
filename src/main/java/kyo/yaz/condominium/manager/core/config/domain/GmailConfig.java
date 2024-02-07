package kyo.yaz.condominium.manager.core.config.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GmailConfig {

  @NotBlank
  private String credentialsPath;
  @NotBlank
  private String tokensPath;
  @NotNull
  private Integer port;
  @NotBlank
  private String appName;
}
