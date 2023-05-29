package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import kyo.yaz.condominium.manager.core.config.domain.UserSession;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Route(LoginView.URL)
@AnonymousAllowed
@Slf4j
public class LoginView extends BaseVerticalLayout implements BeforeEnterObserver, BeforeLeaveObserver {
    public static final String URL = "login";

    /**
     * URL that Spring uses to connect to Google services
     */
    public static final String OAUTH_URL = "/oauth2/authorization/google";

    private final UserSession userSession;

    @Autowired
    public LoginView(@Value("${app.oauth2_google_url}") String oauthUrl, UserSession userSession) {
        this.userSession = userSession;
        Anchor loginLink = new Anchor(oauthUrl, "Login");
        // Set router-ignore attribute so that Vaadin router doesn't handle the login request
        loginLink.getElement().setAttribute("router-ignore", true);
        add(loginLink);
        getStyle().set("padding", "200px");
        setAlignItems(FlexComponent.Alignment.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (hasLoggedIn()) {
            final var user = userSession.getUser();
            log.info("IS_LOGGED_IN {}", user.toString());
            // event.forwardTo(MainLayout.class);
        }
    }


    private boolean hasLoggedIn() {
        return userSession.isLoggedIn();
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        final var user = userSession.getUser();
        log.info("USER {}", user.toString());
    }



   /* @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (userSession.isLoggedIn()) {
            navigate(BuildingView.class);
        }
    }*/
}
