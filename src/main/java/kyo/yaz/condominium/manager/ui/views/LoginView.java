package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import kyo.yaz.condominium.manager.core.component.UserSession;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;

@Route(LoginView.URL)
@AnonymousAllowed
public class LoginView extends BaseVerticalLayout implements BeforeEnterObserver {
    public static final String URL = "login";

    /**
     * URL that Spring uses to connect to Google services
     */
    public static final String OAUTH_URL = "/oauth2/authorization/google";

    private final UserSession userSession;

    @Autowired
    public LoginView(UserSession userSession) {
        this.userSession = userSession;
        Anchor loginLink = new Anchor(OAUTH_URL, "Login");
        // Set router-ignore attribute so that Vaadin router doesn't handle the login request
        loginLink.getElement().setAttribute("router-ignore", true);
        add(loginLink);
        getStyle().set("padding", "200px");
        setAlignItems(FlexComponent.Alignment.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (hasLoggedIn()) {
            // event.forwardTo(MainLayout.class);
        }
    }

    private boolean hasLoggedIn() {
        return userSession.isLoggedIn();
    }



   /* @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (userSession.isLoggedIn()) {
            navigate(BuildingView.class);
        }
    }*/
}
