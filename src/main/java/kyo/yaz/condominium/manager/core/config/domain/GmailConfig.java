package kyo.yaz.condominium.manager.core.config.domain;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
