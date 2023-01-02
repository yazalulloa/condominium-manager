package kyo.yaz.condominium.manager.core.config.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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