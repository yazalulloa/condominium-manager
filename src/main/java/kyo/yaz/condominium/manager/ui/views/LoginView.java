package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveListener;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import kyo.yaz.condominium.manager.core.config.domain.UserSession;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Route(LoginView.URL)
@AnonymousAllowed
public class LoginView extends BaseVerticalLayout implements BeforeEnterObserver, BeforeLeaveObserver,
    AfterNavigationObserver, BeforeLeaveListener, BeforeEnterListener {

  public static final String URL = "login";


  private final UserSession userSession;
  private final String oauthUrl;

  @Autowired
  public LoginView(@Value("${app.oauth2_google_url}") String oauthUrl, UserSession userSession) {
    this.userSession = userSession;
    this.oauthUrl = oauthUrl;

  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    Anchor loginLink = new Anchor(oauthUrl, "Login");
    // Set router-ignore attribute so that Vaadin router doesn't handle the login request
    loginLink.getElement().setAttribute("router-ignore", true);
    add(loginLink);
    getStyle().set("padding", "200px");
    setAlignItems(FlexComponent.Alignment.CENTER);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
   // logger().info("BEFORE_ENTER");
    if (hasLoggedIn()) {
      final var user = userSession.getUser();
      logger().info("IS_LOGGED_IN {}", user.toString());
      // event.forwardTo(MainLayout.class);
    }
  }


  private boolean hasLoggedIn() {
    return userSession.isLoggedIn();
  }

  @Override
  public void beforeLeave(BeforeLeaveEvent event) {
    logger().info("BEFORE_LEAVE");
    /*if (hasLoggedIn()) {
      final var user = userSession.getUser();
      logger().info("BEFORE_LEAVE {}", user.toString());
    }*/
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    final var location = event.getLocation();
    final var locationChangeEvent = event.getLocationChangeEvent();
    final var eventLocation = locationChangeEvent.getLocation();
   // logger().info("AFTER_NAVIGATION {} {} {}", event, location, locationChangeEvent);
   // logger().info("LOCATION {} {}", location.getPath(), location.getSegments());
  //  logger().info("EVENT_LOCATION {} {}", eventLocation.getPath(), eventLocation.getSegments());
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);
   // logger().info("onDetach");
  }

  /* @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (userSession.isLoggedIn()) {
            navigate(BuildingView.class);
        }
    }*/
}
