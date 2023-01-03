package kyo.yaz.condominium.manager.core.config.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
