package kyo.yaz.condominium.manager.core.config.domain;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.Optional;
import kyo.yaz.condominium.manager.ui.views.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Slf4j
@Scope("prototype")
@Component
@SessionScope
public class UserSession implements Serializable {

  public Authentication authentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  public User getUser() {
    final var authentication = authentication();

    final var principal = (DefaultOidcUser) authentication.getPrincipal();
    log.info("PRINCIPAL {}", principal);

    final var idToken = principal.getIdToken();
    log.info("idToken {}", idToken);


    log.info("idToken claims {}", idToken.getClaims());
    log.info("idToken subject {}", idToken.getSubject());
    final var userInfo = principal.getUserInfo();
    log.info("userInfo {}", userInfo);
    log.info("subject {}", principal.getSubject());
    final var accessTokenHash = principal.getAccessTokenHash();
    log.info("accessTokenHash {}", accessTokenHash);
    log.info("authorizationCodeHash {}", principal.getAuthorizationCodeHash());
    log.info("authenticatedAt {}", Optional.ofNullable(principal.getAuthenticatedAt())
        .or(() -> Optional.ofNullable(idToken.getAuthenticatedAt()))
        .map(instant -> instant.atZone(ZoneOffset.UTC))
        .orElse(null));
    log.info("Claims {}", principal.getClaims());

    return new User(principal.getAttribute("given_name"), principal.getAttribute("family_name"),
        principal.getAttribute("email"),
        principal.getAttribute("picture"));
  }

  public boolean isLoggedIn() {
    final var authentication = authentication();
    return authentication != null && authentication.isAuthenticated()
        && (authentication.getPrincipal() instanceof DefaultOidcUser);
  }
}
