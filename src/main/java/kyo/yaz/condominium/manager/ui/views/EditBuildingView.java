package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kyo.yaz.condominium.manager.core.mapper.BuildingMapper;
import kyo.yaz.condominium.manager.core.mapper.ExtraChargeMapper;
import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.ScrollPanel;
import kyo.yaz.condominium.manager.ui.views.domain.ExtraChargeViewItem;
import kyo.yaz.condominium.manager.ui.views.form.CreateBuildingForm;
import kyo.yaz.condominium.manager.ui.views.form.ExtraChargeForm;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@PageTitle(BuildingView.PAGE_TITLE)
@Route(value = "buildings/:building_id", layout = MainLayout.class)
public class EditBuildingView extends ScrollPanel implements BeforeEnterObserver {

    private final CreateBuildingForm createBuildingForm = new CreateBuildingForm();
    private final Grid<ExtraChargeViewItem> extraChargeGrid = new Grid<>(ExtraChargeViewItem.class, false);
    private final Set<ExtraChargeViewItem> extraCharges = new HashSet<>();

    private final H3 extraChargeTitle = new H3(Labels.EXTRA_CHARGE_TITLE);
    private final ExtraChargeForm extraChargeForm = new ExtraChargeForm();
    private final Button saveBtn = new Button(Labels.SAVE);
    private final Button cancelBtn = new Button(Labels.CANCEL);
    private final BuildingService buildingService;
    private final ApartmentService apartmentService;
    private String buildingIdParam;
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
        //setSizeFull();
        configureGrid();
        configureListeners();
        addContent();

    }

    private void addContent() {

        add(createBuildingForm);
        add(createButtonsLayout());

        if (extraChargesVisible) {

            add(extraChargeTitle);
            add(extraChargeForm);
            add(extraChargeGrid);
        }
    }

    private void configureGrid() {
        extraChargeGrid.addClassNames("extra_charge-grid");
        extraChargeGrid.setColumnReorderingAllowed(true);
        extraChargeGrid.setAllRowsVisible(true);
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

    private void initData() {


        final var aptNumbersMono = Maybe.fromOptional(Optional.of(buildingIdParam))
                .flatMapSingle(apartmentService::aptNumbers)
                .switchIfEmpty(Maybe.fromCallable(Collections::emptyList));

        final var buildingViewItemMono = Maybe.fromOptional(Optional.of(buildingIdParam))
                .flatMap(buildingService::find)
                .map(BuildingMapper::to);

        Maybe.zip(buildingViewItemMono, aptNumbersMono, (viewItem, list) -> {
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
                .subscribeOn(Schedulers.io())
                .ignoreElement()
                .subscribe(completableObserver());
    }

    private void configureListeners() {
        extraChargeForm.addListener(ExtraChargeForm.SaveEvent.class, event -> {

            extraCharges.add(event.getObj());
            setExtraChargesInGrid();
        });

        createBuildingForm.addListener(CreateBuildingForm.SaveEvent.class, event -> {
            saveBtn.setEnabled(false);

            final var list = ConvertUtil.toList(extraCharges, ExtraChargeMapper::to);
            final var build = BuildingMapper.to(event.getBuilding()).toBuilder()
                    .extraCharges(list)
                    .build();

            buildingService.save(build)
                    .subscribeOn(Schedulers.io())
                    .ignoreElement()
                    .subscribe(completableObserver(this::navigateBack));
        });
    }

    private void navigateBack() {
        navigate(BuildingView.class);
    }


    private HorizontalLayout createButtonsLayout() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveBtn.addClickShortcut(Key.ENTER);
        cancelBtn.addClickShortcut(Key.ESCAPE);

        createBuildingForm.binder.addStatusChangeListener(e -> saveBtn.setEnabled(createBuildingForm.binder.isValid()));

        saveBtn.setEnabled(createBuildingForm.binder.isValid());
        saveBtn.addClickListener(event -> createBuildingForm.validateAndSave());

        cancelBtn.addClickListener(event -> navigateBack());

        return new HorizontalLayout(saveBtn, cancelBtn);
    }
}
