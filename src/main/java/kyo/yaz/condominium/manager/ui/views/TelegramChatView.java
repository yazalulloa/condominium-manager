package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
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
import io.vertx.core.json.JsonObject;
import jakarta.annotation.security.PermitAll;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kyo.yaz.condominium.manager.core.config.domain.UserSession;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.service.entity.TelegramChatService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.component.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.util.IconUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@PageTitle(TelegramChatView.PAGE_TITLE)
@PermitAll
@Route(value = "telegram_chats", layout = MainLayout.class)
public class TelegramChatView extends BaseVerticalLayout {

  public static final String PAGE_TITLE = "Chats de Telegram";

  private final Grid<TelegramChat> grid = new Grid<>();

  private final Text queryCountText = new Text(null);
  private final Text totalCountText = new Text(null);
  private final ProgressLayout progressLayout = new ProgressLayout();
  private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);
  private final DeleteDialog deleteDialog = new DeleteDialog();
  private final TelegramChatService chatService;
  private final UserSession userSession;

  private final String telegramStartUrl;

  @Autowired
  public TelegramChatView(TelegramChatService chatService, UserSession userSession,
      @Value("${app.telegram_config.start_url}") String telegramStartUrl) {
    this.chatService = chatService;
    this.userSession = userSession;
    this.telegramStartUrl = telegramStartUrl;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    initData();
  }

  private void init() {
    addClassName("telegram-chats-view");
    setSizeFull();
    configureGrid();
    gridPaginator.init();

    add(getToolbar(), progressLayout, grid, footer());
  }

  private void initData() {

    paging()
        .map(paging -> (Runnable) () -> {
          setItems(paging);
          init();
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
    grid.addClassNames("telegram-chats-grid");

    grid.addComponentColumn(this::card);
    grid.setItemDetailsRenderer(itemDetailsRenderer());
    grid.setPageSize(gridPaginator.itemsPerPage());
    grid.setSizeFull();
  }

  private Renderer<TelegramChat> itemDetailsRenderer() {
    return new ComponentRenderer<>(Div::new, (div, chat) -> {

      div.add(new Span(new JsonObject(chat.update()).encodePrettily()));
    });
  }

  private void deleteDialog(TelegramChat item) {
    deleteDialog.setText(
        Labels.ASK_CONFIRMATION_DELETE_TELEGRAM_CHAT.formatted(item.chatId(), item.username()));
    deleteDialog.setDeleteAction(() -> delete(item));
    deleteDialog.open();
  }

  private Component card(TelegramChat chat) {

    final var set = Stream.of(chat.username(), chat.firstName(), chat.lastName())
        .filter(Objects::nonNull)
        .map(String::trim)
        .collect(Collectors.toSet());

    final var names = String.join(", ", set);

   /* final var avatar = new Avatar(names);
    avatar.setImage(chat.picture());*/

    final var body = new Div(
        //avatar,
        new Span("Chat id: " + chat.chatId()),
        new Span(names),
        new Span("Creado: " + DateUtil.formatVe(chat.createdAt())),
        new Span("Usuario: " + chat.user().email())
    );

    body.addClassName("body");

    final var deleteBtn = new Button(IconUtil.trash());
    deleteBtn.addClickListener(v -> deleteDialog(chat));

    final var buttons = new Div(deleteBtn);
    buttons.addClassName("buttons");

    final var div = new Div(body, buttons);
    div.addClassName("card");
    return div;
  }

  public void delete(TelegramChat rate) {
    chatService.delete(rate)
        .andThen(refreshData())
        .subscribeOn(Schedulers.io())
        .subscribe(completableObserver());
  }

  private Component getToolbar() {

    final var link = telegramStartUrl + userSession.getUser().id();

    final var btn = new Button("Enlazar cuenta");
    btn.addClickListener(event -> ui(ui -> ui.getPage().open(link, "_blank")));

    final var toolbar = new Div(btn, queryCountText);
    toolbar.addClassName("toolbar");
    return toolbar;
  }

  private Single<Paging<TelegramChat>> paging() {
    return chatService.paging(null, gridPaginator.currentPage(), gridPaginator.itemsPerPage());
  }

  private void setCountText(long queryCount, long totalCount) {
    queryCountText.setText(String.format("Chats: %d", queryCount));
    gridPaginator.set(queryCount, totalCount);
    totalCountText.setText(String.format("Chats  Totales: %d", totalCount));
  }

  private Completable refreshData() {

    return paging()
        .doOnSubscribe(d -> {
          uiAsyncAction(() -> {
            progressLayout.progressBar().setIndeterminate(true);
            progressLayout.setProgressText("Buscando chats");
            progressLayout.setVisible(true);
          });
        })
        .map(paging -> (Runnable) () -> {
          setItems(paging);
          progressLayout.setVisible(false);
        })
        .doOnSuccess(this::uiAsyncAction)
        .ignoreElement();
  }

  private void setItems(Paging<TelegramChat> paging) {
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
