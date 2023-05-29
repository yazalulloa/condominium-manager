package kyo.yaz.condominium.manager.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import kyo.yaz.condominium.manager.core.config.domain.UserSession;
import kyo.yaz.condominium.manager.ui.appnav.AppNav;
import kyo.yaz.condominium.manager.ui.appnav.AppNavItem;
import kyo.yaz.condominium.manager.ui.views.RateView;
import kyo.yaz.condominium.manager.ui.views.apartment.ApartmentView;
import kyo.yaz.condominium.manager.ui.views.building.BuildingView;
import kyo.yaz.condominium.manager.ui.views.domain.User;
import kyo.yaz.condominium.manager.ui.views.email_config.EmailConfigView;
import kyo.yaz.condominium.manager.ui.views.receipt.ReceiptView;

import java.util.Optional;


public class MainLayout extends AppLayout {
    private H2 viewTitle;

    private final UserSession userSession;

    public MainLayout(UserSession userSession) {
        this.userSession = userSession;
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1("Condominium Manager");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller);
    }

    private AppNav createNavigation() {

        return new AppNav()
                .addItem(new AppNavItem(BuildingView.PAGE_TITLE, BuildingView.class, new Icon(VaadinIcon.BUILDING)))
                .addItem(new AppNavItem(RateView.PAGE_TITLE, RateView.class, new Icon(VaadinIcon.COIN_PILES)))
                .addItem(new AppNavItem(ApartmentView.PAGE_TITLE, ApartmentView.class, new Icon(VaadinIcon.USER)))
                .addItem(new AppNavItem(ReceiptView.PAGE_TITLE, ReceiptView.class, new Icon(VaadinIcon.FILE_TEXT)))
                .addItem(new AppNavItem(EmailConfigView.PAGE_TITLE, EmailConfigView.class, new Icon(VaadinIcon.ENVELOPE)));

    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Anchor loginLink = new Anchor("login", "Sign in");
        layout.add(loginLink);

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
