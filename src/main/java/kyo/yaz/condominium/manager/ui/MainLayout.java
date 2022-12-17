package kyo.yaz.condominium.manager.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import kyo.yaz.condominium.manager.ui.appnav.AppNav;
import kyo.yaz.condominium.manager.ui.appnav.AppNavItem;
import kyo.yaz.condominium.manager.ui.views.ApartmentView;
import kyo.yaz.condominium.manager.ui.views.BuildingView;
import kyo.yaz.condominium.manager.ui.views.RateView;
import kyo.yaz.condominium.manager.ui.views.ReceiptView;


public class MainLayout extends AppLayout {

    private H2 viewTitle;

    public MainLayout() {
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

        addToDrawer(header, scroller, createFooter());
    }

    private AppNav  createNavigation() {

        return new AppNav()
                .addItem(new AppNavItem(BuildingView.PAGE_TITLE, BuildingView.class, "la la-globe"))
                .addItem(new AppNavItem(RateView.PAGE_TITLE, RateView.class, "la la-globe"))
                .addItem(new AppNavItem(ApartmentView.PAGE_TITLE, ApartmentView.class, "la la-globe"))
                .addItem(new AppNavItem(ReceiptView.PAGE_TITLE, ReceiptView.class, "la la-globe"));

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
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    private void createHeader() {
        H1 logo = new H1("Vaadin CRM");
        logo.addClassNames("text-l", "m-m");

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);

    }

   /* private void createDrawer() {
        RouterLink buildings = new RouterLink(BuildingView.PAGE_TITLE, BuildingView.class);
        buildings.setHighlightCondition(HighlightConditions.sameLocation());

        addToDrawer(new VerticalLayout(
                buildings,
                new RouterLink(RateView.PAGE_TITLE, RateView.class),
                new RouterLink(ApartmentView.PAGE_TITLE, ApartmentView.class),
                new RouterLink(ReceiptView.PAGE_TITLE, ReceiptView.class)
        ));
    }*/

}
