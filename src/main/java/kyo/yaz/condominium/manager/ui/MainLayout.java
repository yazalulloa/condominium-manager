package kyo.yaz.condominium.manager.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.Optional;
import kyo.yaz.condominium.manager.core.config.domain.UserSession;
import kyo.yaz.condominium.manager.core.service.ProcessLoggedUser;
import kyo.yaz.condominium.manager.core.util.MyIconsIcons;
import kyo.yaz.condominium.manager.persistence.entity.User;
import kyo.yaz.condominium.manager.ui.appnav.AppNav;
import kyo.yaz.condominium.manager.ui.appnav.AppNavItem;
import kyo.yaz.condominium.manager.ui.views.RateView;
import kyo.yaz.condominium.manager.ui.views.SystemView;
import kyo.yaz.condominium.manager.ui.views.UserView;
import kyo.yaz.condominium.manager.ui.views.apartment.ApartmentView;
import kyo.yaz.condominium.manager.ui.views.building.BuildingView;
import kyo.yaz.condominium.manager.ui.views.email_config.EmailConfigView;
import kyo.yaz.condominium.manager.ui.views.receipt.ReceiptView;
import kyo.yaz.condominium.manager.ui.views.telegram_chat.TelegramChatView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Slf4j
public class MainLayout extends AppLayout {

  private static final String LOGOUT_SUCCESS_URL = "/";
  private final H2 viewTitle = new H2();

  private final UserSession userSession;
  private final ProcessLoggedUser processLoggedUser;

  public MainLayout(UserSession userSession, ProcessLoggedUser processLoggedUser) {
    this.userSession = userSession;
    this.processLoggedUser = processLoggedUser;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    setPrimarySection(Section.DRAWER);
    addDrawerContent();
    addHeaderContent();

    if (userSession.isLoggedIn()) {
      processLoggedUser.process(userSession.getUser());
    }
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.getElement().setAttribute("aria-label", "Menu toggle");

    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

    addToNavbar(true, toggle, viewTitle);
  }

  private void addDrawerContent() {
    H1 appName = new H1("Condominium Manager");
    appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
    Header header = new Header(appName);

    Scroller scroller = new Scroller(createNavigation());

    addToDrawer(header, scroller, createFooter());
  }

  private AppNav createNavigation() {

    return new AppNav()
        .addItem(new AppNavItem(BuildingView.PAGE_TITLE, BuildingView.class, VaadinIcon.BUILDING.create()))
        .addItem(new AppNavItem(RateView.PAGE_TITLE, RateView.class, VaadinIcon.COIN_PILES.create()))
        .addItem(new AppNavItem(ApartmentView.PAGE_TITLE, ApartmentView.class, VaadinIcon.USER.create()))
        .addItem(new AppNavItem(ReceiptView.PAGE_TITLE, ReceiptView.class, VaadinIcon.FILE_TEXT.create()))
        .addItem(new AppNavItem(EmailConfigView.PAGE_TITLE, EmailConfigView.class, VaadinIcon.ENVELOPE.create()))
        .addItem(new AppNavItem(UserView.PAGE_TITLE, UserView.class, VaadinIcon.USERS.create()))
        .addItem(
            new AppNavItem(TelegramChatView.PAGE_TITLE, TelegramChatView.class, MyIconsIcons.ICONS8_TELEGRAM.create()))
        .addItem(new AppNavItem(SystemView.PAGE_TITLE, SystemView.class, VaadinIcon.INFO.create()))
        ;

  }

  private Footer createFooter() {
    final var layout = new Footer();

    final var logoutButton = new Button("Cerrar sesion", click -> {
      UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
      final var logoutHandler = new SecurityContextLogoutHandler();
      logoutHandler.logout(
          VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
          null);
    });

    layout.add(logoutButton);
       /* Anchor loginLink = new Anchor("logout", "Cerrar sesion");
        layout.add(loginLink);*/

    return layout;
  }

  @Override
  protected void afterNavigation() {
    super.afterNavigation();

    final var email = Optional.ofNullable(userSession.getUser())
        .map(User::email)
        .orElse("");

    viewTitle.setText(getCurrentPageTitle() + " " + email);
  }

  private String getCurrentPageTitle() {
    return Optional.ofNullable(getContent())
        .map(Object::getClass)
        .map(clazz -> clazz.getAnnotation(PageTitle.class))
        .map(PageTitle::value)
        .orElse("");
  }
}
