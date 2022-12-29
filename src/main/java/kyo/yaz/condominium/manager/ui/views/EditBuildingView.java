package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.domain.ExtraChargeViewItem;
import kyo.yaz.condominium.manager.ui.views.form.CreateBuildingForm;
import kyo.yaz.condominium.manager.ui.views.form.ExtraChargeForm;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@PageTitle(BuildingView.PAGE_TITLE)
@Route(value = "buildings/:building_id", layout = MainLayout.class)
public class EditBuildingView extends VerticalLayout implements BeforeEnterObserver, AbstractView {

    private final CreateBuildingForm createBuildingForm = new CreateBuildingForm();
    private final Grid<ExtraChargeViewItem> extraChargeGrid = new Grid<>(ExtraChargeViewItem.class, false);
    private final Set<ExtraChargeViewItem> extraCharges = new HashSet<>();

    private final H3 extraChargeTitle = new H3(Labels.EXTRA_CHARGE_TITLE);
    private final ExtraChargeForm extraChargeForm = new ExtraChargeForm();
    private final Button saveBtn = new Button(Labels.SAVE);
    private final Button cancelBtn = new Button(Labels.CANCEL);
    private String buildingIdParam;

    private final BuildingService buildingService;

    private final ApartmentService apartmentService;

    private boolean extraChargesVisible;

    @Autowired
    public EditBuildingView(BuildingService buildingService, ApartmentService apartmentService) {
        super();
        this.buildingService = buildingService;
        this.apartmentService = apartmentService;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initData();
    }

    private void init() {
        addClassName("edit-building-view");
        setSizeFull();
        configureGrid();
        configureListeners();

        add(getContent());
        if (extraChargesVisible) {
            add(extraChargeGrid);
        }
        add(createButtonsLayout());

    }

    private Component getContent() {



        final var content = new HorizontalLayout(createBuildingForm);
        content.add(createBuildingForm);

        if (extraChargesVisible) {
            final var verticalLayout = new VerticalLayout(extraChargeTitle, extraChargeForm);
            content.add(verticalLayout);
            content.setFlexGrow(2, createBuildingForm);
            content.setFlexGrow(1, verticalLayout);
        }

        content.addClassNames("content");
        content.setWidthFull();
        return content;
    }

    private void configureGrid() {
        extraChargeGrid.addClassNames("extra_charge-grid");
        extraChargeGrid.setColumnReorderingAllowed(true);
        extraChargeGrid.addColumn(ExtraChargeViewItem::getAptNumber).setHeader(Labels.ExtraCharge.APT_LABEL).setSortable(true).setKey(Labels.ExtraCharge.APT_LABEL);
        extraChargeGrid.addColumn(ExtraChargeViewItem::getDescription).setHeader(Labels.ExtraCharge.DESCRIPTION_LABEL);
        extraChargeGrid.addColumn(ExtraChargeViewItem::getAmount).setHeader(Labels.ExtraCharge.AMOUNT_LABEL).setSortable(true).setKey(Labels.ExtraCharge.AMOUNT_LABEL);
        extraChargeGrid.addColumn(ExtraChargeViewItem::getCurrency).setHeader(Labels.ExtraCharge.CURRENCY_LABEL).setSortable(true).setKey(Labels.ExtraCharge.CURRENCY_LABEL);

        extraChargeGrid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> this.removeExtraCharge(item));
                            button.setIcon(new Icon(VaadinIcon.TRASH));
                        }))
                .setHeader(Labels.DELETE)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        extraChargeGrid.addColumn(
                        new ComponentRenderer<>(Button::new, (button, item) -> {
                            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                    ButtonVariant.LUMO_SUCCESS,
                                    ButtonVariant.LUMO_TERTIARY);
                            button.addClickListener(e -> {
                                final var newItem = item.toBuilder().build();
                                extraChargeForm.setExtraCharge(newItem);
                            });
                            button.setIcon(new Icon(VaadinIcon.TRASH));
                        }))
                .setHeader(Labels.COPY)
                .setTextAlign(ColumnTextAlign.END)
                .setFrozenToEnd(true)
                .setFlexGrow(0);

        extraChargeGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        extraChargeGrid.addSelectionListener(selection -> {

            selection.getFirstSelectedItem()
                    .ifPresent(extraChargeForm::setExtraCharge);
        });

        setExtraChargesInGrid();
    }

    private void removeExtraCharge(ExtraChargeViewItem item) {
        extraCharges.remove(item);
        setExtraChargesInGrid();
    }

    private void setExtraChargesInGrid() {

        uiAsyncAction(() -> {
            extraChargeGrid.setItems(extraCharges);
            extraChargeGrid.getDataProvider().refreshAll();
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        buildingIdParam = event.getRouteParameters().get("building_id")
                .filter(s -> !s.equals("new"))
                .orElse(null);
    }

    @Override
    public Component component() {
        return this;
    }

    @Override
    public Logger logger() {
        return log;
    }

    private void initData() {

        final var aptNumbersMono = Mono.justOrEmpty(buildingIdParam)
                .flatMap(apartmentService::aptNumbers)
                .defaultIfEmpty(Collections.emptyList())
                .subscribeOn(Schedulers.parallel());

        final var buildingViewItemMono = Mono.justOrEmpty(buildingIdParam)
                .flatMap(buildingService::find)
                .map(ConvertUtil::viewItem);

        Mono.zip(buildingViewItemMono, aptNumbersMono, (viewItem, list) -> {
                    return (Runnable) () -> {
                        extraCharges.addAll(viewItem.getExtraCharges());
                        createBuildingForm.setBuilding(viewItem);
                        extraChargeForm.setApartments(list);

                        extraChargesVisible = !list.isEmpty();
                        init();
                    };
                })
                .defaultIfEmpty(() -> {
                    extraChargesVisible = false;

                    init();
                })
                .doOnSuccess(this::uiAsyncAction)
                .subscribeOn(Schedulers.parallel())
                .and(Mono.empty())
                .subscribe(emptySubscriber());
    }

    private void configureListeners() {
        extraChargeForm.addListener(ExtraChargeForm.SaveEvent.class, event -> {

            extraCharges.add(event.getObj());
            setExtraChargesInGrid();
        });

        createBuildingForm.addListener(CreateBuildingForm.SaveEvent.class, event -> {
            saveBtn.setEnabled(false);
            final var building = ConvertUtil.building(event.getBuilding());

            final var list = ConvertUtil.toList(extraCharges, ConvertUtil::extraCharge);
            final var build = building.toBuilder()
                    .extraCharges(list)
                    .build();

            buildingService.save(build)
                    .subscribeOn(Schedulers.parallel())
                    .subscribe(b -> uiAsyncAction(this::navigateBack), this::showError);
        });
    }

    private void navigateBack() {

        ui(ui -> ui.navigate(BuildingView.class));
    }


    private HorizontalLayout createButtonsLayout() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveBtn.addClickShortcut(Key.ENTER);
        cancelBtn.addClickShortcut(Key.ESCAPE);

        createBuildingForm.binder.addStatusChangeListener(e -> saveBtn.setEnabled(createBuildingForm.binder.isValid()));

        saveBtn.addClickListener(event -> createBuildingForm.validateAndSave());

        cancelBtn.addClickListener(event -> navigateBack());


        //binder.addStatusChangeListener(e -> saveBtn.setEnabled(binder.isValid()));

        return new HorizontalLayout(saveBtn, cancelBtn);
    }
}