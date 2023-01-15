package kyo.yaz.condominium.manager.ui.views.building;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kyo.yaz.condominium.manager.core.mapper.BuildingMapper;
import kyo.yaz.condominium.manager.core.mapper.ExtraChargeMapper;
import kyo.yaz.condominium.manager.core.mapper.ReserveFundMapper;
import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.ScrollPanel;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Optional;


@PageTitle(BuildingView.PAGE_TITLE)
@Route(value = "buildings/:building_id", layout = MainLayout.class)
public class EditBuildingView extends ScrollPanel implements BeforeEnterObserver {

    private final CreateBuildingForm createBuildingForm = new CreateBuildingForm();
    private final ReserveFundView reserveFundView = new ReserveFundView();
    private final ExtraChargesView extraChargesView = new ExtraChargesView();
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

        configureListeners();
        extraChargesView.init();
        reserveFundView.init();
        addContent();
    }

    private void addContent() {

        extraChargesView.setVisible(extraChargesVisible);
        add(createBuildingForm, createButtonsLayout(), extraChargesView, reserveFundView);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        buildingIdParam = event.getRouteParameters().get("building_id")
                .filter(s -> !s.equals("new"))
                .orElse(null);
    }

    private void initData() {


        final var aptNumbersSingle = Maybe.fromOptional(Optional.of(buildingIdParam))
                .flatMapSingle(apartmentService::aptNumbers)
                .switchIfEmpty(Maybe.fromCallable(Collections::emptyList));

        final var buildingMaybe = Maybe.fromOptional(Optional.of(buildingIdParam))
                .flatMap(buildingService::find);

        Maybe.zip(buildingMaybe, aptNumbersSingle, (building, list) -> {
                    return (Runnable) () -> {
                        extraChargesView.setItems(ConvertUtil.toList(building.extraCharges(), ExtraChargeMapper::to));
                        extraChargesView.setApartments(list);

                        createBuildingForm.setBuilding(BuildingMapper.to(building));
                        reserveFundView.addItems(ConvertUtil.toList(building.reserveFunds(), ReserveFundMapper::to));
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

        createBuildingForm.addListener(CreateBuildingForm.SaveEvent.class, event -> {
            saveBtn.setEnabled(false);

            apartmentService.countByBuilding(event.getBuilding().getId())
                    .map(count -> {

                        final var list = ConvertUtil.toList(extraChargesView.items(), ExtraChargeMapper::to);

                        final var reserveFunds = ConvertUtil.toList(reserveFundView.list(), ReserveFundMapper::to);
                        logger().info("saving Reserve funds: " + reserveFunds.size());
                        return BuildingMapper.to(event.getBuilding())
                                .toBuilder()
                                .extraCharges(list)
                                .amountOfApts(count)
                                .reserveFunds(reserveFunds)
                                .build();
                    })
                    .flatMap(buildingService::save)
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
