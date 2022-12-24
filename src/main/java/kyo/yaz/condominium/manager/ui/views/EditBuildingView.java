package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import kyo.yaz.condominium.manager.ui.MainLayout;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PageTitle(BuildingView.PAGE_TITLE)
@Route(value = "buildings/:building_id", layout = MainLayout.class)
public class EditBuildingView {
}
