package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.security.PermitAll;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.service.entity.UserService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.entity.User;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.component.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.util.IconUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle(UserView.PAGE_TITLE)
@PermitAll
@Route(value = "users", layout = MainLayout.class)
public class UserView extends BaseVerticalLayout {

  public static final String PAGE_TITLE = "Usuarios";

  private final Grid<User> grid = new Grid<>();

  private final Text queryCountText = new Text(null);
  private final Text totalCountText = new Text(null);
  private final DeleteDialog deleteDialog = new DeleteDialog();  private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);
  private final UserService userService;
  @Autowired
  public UserView(UserService userService) {
    this.userService = userService;
    init();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    initData();
  }

  private void init() {
    addClassName("users-view");
    setSizeFull();
    configureGrid();

    add(getToolbar(), grid, footer());
  }

  private void initData() {

    paging()
        .map(paging -> (Runnable) () -> {
          setItems(paging);
          gridPaginator.init();
        })
        .doOnSuccess(this::uiAsyncAction)
        .ignoreElement()
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }

  private Component footer() {
    final var footer = new Div(gridPaginator, totalCountText);
    footer.addClassName("footer");
    return footer;
  }

  private void configureGrid() {
    grid.addClassNames("users-grid");

    grid.addComponentColumn(this::card);
    grid.setItemDetailsRenderer(itemDetailsRenderer());
    grid.setPageSize(gridPaginator.itemsPerPage());
    grid.setSizeFull();
  }

  private Renderer<User> itemDetailsRenderer() {
    return new ComponentRenderer<>(Div::new, (div, user) -> {

      div.add(new Span(user.userInfoStr()));
    });
  }

  private void deleteDialog(User item) {
    deleteDialog.setText(
        Labels.ASK_CONFIRMATION_DELETE_USERS.formatted(item.email(), item.name()));
    deleteDialog.setDeleteAction(() -> delete(item));
    deleteDialog.open();
  }

  private Component card(User user) {

    final var set = Stream.of(user.givenName(), user.name())
        .filter(Objects::nonNull)
        .map(String::trim)
        .collect(Collectors.toSet());

    final var names = String.join(", ", set);

    final var avatar = new Avatar(names);
    avatar.setImage(user.picture());

    final var body = new Div(
        avatar,
        new Span("ID: " + user.id()),
        new Span(names),
        new Span(user.email()),
        new Span("Access Token: " + user.lastAccessTokenHash()),
        new Span("Access Token Date: " + DateUtil.formatVe(user.lastAccessTokenHashDate())),
        new Span("Issue At: " + DateUtil.formatVe(user.issuedAt())),
        new Span("Expiration At: " + DateUtil.formatVe(user.expirationAt()))
    );

    body.addClassName("body");

    final var deleteBtn = new Button(IconUtil.trash());
    deleteBtn.addClickListener(v -> deleteDialog(user));

    final var buttons = new Div(deleteBtn);
    buttons.addClassName("buttons");

    final var div = new Div(body, buttons);
    div.addClassName("card");
    return div;
  }

  public void delete(User rate) {
    userService.delete(rate)
        .andThen(refreshData())
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }

  private Component getToolbar() {

    final var toolbar = new Div(queryCountText);
    toolbar.addClassName("toolbar");
    return toolbar;
  }

  private Single<Paging<User>> paging() {
    return userService.paging(null, gridPaginator.currentPage(), gridPaginator.itemsPerPage());
  }

  private void setCountText(long queryCount, long totalCount) {
    queryCountText.setText(String.format("Usuarios: %d", queryCount));
    gridPaginator.set(queryCount, totalCount);
    totalCountText.setText(String.format("Usuarios Totales: %d", totalCount));
  }

  private Completable refreshData() {

    return paging()
        .map(paging -> (Runnable) () -> setItems(paging))
        .doOnSuccess(this::uiAsyncAction)
        .ignoreElement();
  }

  private void setItems(Paging<User> paging) {
    grid.setPageSize(gridPaginator.itemsPerPage());
    grid.setItems(paging.results());

    setCountText(paging.queryCount(), paging.totalCount());

    grid.getDataProvider().refreshAll();
  }

  private void updateGrid() {
    refreshData()
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }




}
