package kyo.yaz.condominium.manager.core.mapper;

import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.ui.views.building.BuildingViewItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BuildingMapper {

  BuildingMapper MAPPER = Mappers.getMapper(BuildingMapper.class);

  static Building to(BuildingViewItem item) {
    return MAPPER.map(item);
  }

  static BuildingViewItem to(Building building) {
    return MAPPER.map(building);
  }

  Building map(BuildingViewItem item);

  default BuildingViewItem map(Building item) {
    return new BuildingViewItem(item.id(), item.name(), item.rif(),
        item.mainCurrency(), item.debtCurrency(), item.currenciesToShowAmountToPay(),
        item.fixedPay(), item.fixedPayAmount(),
        item.roundUpPayments(), item.emailConfig(), item.createdAt(), item.updatedAt());
  }
}

