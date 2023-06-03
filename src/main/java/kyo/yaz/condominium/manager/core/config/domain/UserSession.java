package kyo.yaz.condominium.manager.core.config.domain;

import kyo.yaz.condominium.manager.ui.views.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;

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
        log.info("PRINCIPAL {}", authentication.getPrincipal().getClass());
        final var principal = (DefaultOidcUser) authentication.getPrincipal();

        return new User(principal.getAttribute("given_name"), principal.getAttribute("family_name"), principal.getAttribute("email"),
                principal.getAttribute("picture"));
    }

    public boolean isLoggedIn() {
        final var authentication = authentication();
        return authentication != null && authentication.isAuthenticated() && (authentication.getPrincipal() instanceof DefaultOidcUser);
    }
}
