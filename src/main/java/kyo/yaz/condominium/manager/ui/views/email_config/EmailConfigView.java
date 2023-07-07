package kyo.yaz.condominium.manager.ui.views.email_config;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.security.PermitAll;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.mapper.EmailConfigMapper;
import kyo.yaz.condominium.manager.core.service.entity.EmailConfigService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.ObjectUtil;
import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.component.DeleteDialog;
import kyo.yaz.condominium.manager.ui.views.component.GridPaginator;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.util.IconUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@PageTitle(EmailConfigView.PAGE_TITLE)
@PermitAll
@Route(value = "email_configs", layout = MainLayout.class)
public class EmailConfigView extends BaseVerticalLayout {

    public static final String PAGE_TITLE = "Configuración de email";

    private final DeleteDialog deleteDialog = new DeleteDialog();
    private final TextField filterText = new TextField();

    private final Text queryCountText = new Text(null);
    private final Text totalCountText = new Text(null);
    private final Button addBtn = new Button(Labels.ADD);
    private final Grid<EmailConfig> grid = new Grid<>();

    private final GridPaginator gridPaginator = new GridPaginator(this::updateGrid);

    private final EmailConfigForm form = new EmailConfigForm();
    private final ProgressLayout progressLayout = new ProgressLayout();

    private final EmailConfigService service;

    @Autowired
    public EmailConfigView(EmailConfigService service) {
        this.service = service;
        init();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);


        refreshData()
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }

    private void init() {
        addClassName("email-config-view");

        setSizeFull();
        configureGrid();
        configureForm();

        add(getToolbar(), progressLayout, getContent(), footer());
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassNames("emails-form-grid");

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.addComponentColumn(this::card);

        grid.setPageSize(gridPaginator.itemsPerPage());
        grid.setSizeFull();

       /* final var contextMenu = new EmailConfigView.EmailConfigContextMenu(grid, this);
        add(grid, contextMenu);*/

        grid.asSingleSelect().addValueChangeListener(event -> editEntity(event.getValue()));
    }

    private Component getToolbar() {
        filterText.setPlaceholder("Buscar");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> {
            if (!gridPaginator.goToFirstPage()) {
                updateGrid();
            }
        });
        addBtn.setDisableOnClick(true);
        addBtn.addClickListener(click -> addEntity());

        final var toolbar = new Div(filterText, addBtn, queryCountText);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private Component getContent() {
        final var content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private Component footer() {

        final var footer = new Div(gridPaginator, totalCountText);
        footer.addClassName("footer");
        return footer;
    }

    private void configureForm() {
        form.setWidth(30, Unit.EM);
        form.setHeightFull();
        form.addListener(EmailConfigForm.SaveEvent.class, this::saveEntity);
        form.addListener(EmailConfigForm.DeleteEvent.class, this::deleteEntity);
        form.addListener(EmailConfigForm.CloseEvent.class, e -> closeEditor());
    }

    private Component card(EmailConfig emailConfig) {
        final var div = new Div();
        div.addClassName("info");

        div.add(new Span(emailConfig.id()), new Span(Labels.EmailConfig.FROM_LABEL + ": " + emailConfig.from()));
        final var activeIcon = IconUtil.checkMarkOrCross(ObjectUtil.aBoolean(emailConfig.active()));
        div.add(new Span(new Span(Labels.EmailConfig.ACTIVE_LABEL + ": "), activeIcon));

        final var isAvailableIcon = IconUtil.checkMarkOrCross(ObjectUtil.aBoolean(emailConfig.isAvailable()));
        div.add(new Span(new Span(Labels.EmailConfig.IS_AVAILABLE_LABEL + ": "), isAvailableIcon));

        if (emailConfig.error() != null) {
            isAvailableIcon.setTooltipText(emailConfig.error());
        }

        if (emailConfig.createdAt() != null) {
            div.add(new Span(Labels.EmailConfig.CREATED_AT_LABEL + ": " + DateUtil.formatVe(emailConfig.createdAt())));
        }

        if (emailConfig.updatedAt() != null) {
            div.add(new Span(Labels.EmailConfig.UPDATED_AT_LABEL + ": " + DateUtil.formatVe(emailConfig.updatedAt())));
        }
        if (emailConfig.lastCheckAt() != null) {
            div.add(new Span(Labels.EmailConfig.LAST_CHECK_AT_LABEL + ": " + DateUtil.formatVe(emailConfig.lastCheckAt())));
        }

        final var card = new HorizontalLayout();
        card.addClassName("card");
        final var btnLayout = new Div();
        btnLayout.addClassName("buttons");

        final var deleteBtn = new Button(IconUtil.trash());
        deleteBtn.addClickListener(v -> deleteDialog(emailConfig));

        final var checkBtn = new Button(new Icon(VaadinIcon.CHECK));
        checkBtn.addClickListener(v ->
                checkConfig(emailConfig)
                        .andThen(refreshData()).subscribe(completableObserver()));

        btnLayout.add(deleteBtn, checkBtn);

        card.add(div, btnLayout);

        return card;
    }

    public void delete(EmailConfig obj) {

        service.delete(obj)
                .andThen(refreshData())
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }

    private Completable checkConfig(EmailConfig emailConfig) {
        return service.clear()
                .doOnSubscribe(d -> {
                    uiAsyncAction(() -> {
                        progressLayout.setProgressText("Borrando cache");
                        progressLayout.progressBar().setIndeterminate(true);
                        progressLayout.setVisible(true);
                    });
                })
                .andThen(service.check(emailConfig)
                        .doOnSubscribe(d -> {
                            uiAsyncAction(() -> {
                                progressLayout.setProgressText("Chequeando...");
                                progressLayout.progressBar().setIndeterminate(true);
                                progressLayout.setVisible(true);
                            });
                        }))
                .doAfterTerminate(() -> uiAsyncAction(() -> progressLayout.setVisible(false)));
    }

    private void setCountText(long queryCount, long totalCount) {
        queryCountText.setText(String.format("Configuraciones: %d", queryCount));
        gridPaginator.set(queryCount, totalCount);
        totalCountText.setText(String.format("Total: %d", totalCount));
    }

    private void updateGrid() {
        refreshData()
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());
    }

    private Completable refreshData() {

        return pagingMono()
                .map(paging -> (Runnable) () -> {
                    progressLayout.setVisible(false);
                    grid.setPageSize(gridPaginator.itemsPerPage());
                    grid.setItems(paging.results());

                    setCountText(paging.queryCount(), paging.totalCount());

                    grid.getDataProvider().refreshAll();
                    gridPaginator.init();
                })
                .doOnSuccess(this::uiAsyncAction)
                .ignoreElement();
    }

    private Single<Paging<EmailConfig>> pagingMono() {
        return service.paging(filterText.getValue(), gridPaginator.currentPage(), gridPaginator.itemsPerPage())
                .doOnSubscribe(d -> {
                    uiAsyncAction(() -> {
                        progressLayout.setProgressText("Buscando...");
                        progressLayout.progressBar().setIndeterminate(true);
                        progressLayout.setVisible(true);
                    });
                });
    }

    public void editEntity(EmailConfig item) {
        if (item == null) {
            closeEditor();
        } else {
            form.setItem(EmailConfigMapper.to(item));
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.setItem(null);
        form.setVisible(false);
        addBtn.setEnabled(true);
        removeClassName("editing");
    }

    private void addEntity() {
        grid.asSingleSelect().clear();
        editEntity(EmailConfig.builder().build());
    }

    private void saveEntity(EmailConfigForm.SaveEvent event) {
        final var item = event.getObj();

        final var config = EmailConfigMapper.to(item)
                .toBuilder()
                .createdAt(Optional.ofNullable(item.getCreatedAt()).orElseGet(DateUtil::nowZonedWithUTC))
                .updatedAt(item.getCreatedAt() != null ? DateUtil.nowZonedWithUTC() : null)
                .build();

        service.save(config)
                .flatMapCompletable(b -> b ? refreshData() : Completable.complete())
                .andThen(checkConfig(config))
                .subscribeOn(Schedulers.io())
                .subscribe(completableObserver());

        closeEditor();
    }

    private void deleteDialog(EmailConfig emailConfig) {
        deleteDialog.setText(Labels.ASK_CONFIRMATION_DELETE_EMAIL_CONFIG.formatted(emailConfig.id()));
        deleteDialog.setDeleteAction(() -> delete(emailConfig));
        deleteDialog.open();
    }

    private void deleteEntity(EmailConfigForm.DeleteEvent event) {
        deleteDialog(EmailConfigMapper.to(event.getObj()));
        closeEditor();
    }
}
