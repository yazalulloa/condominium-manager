package kyo.yaz.condominium.manager.core.mapper;

import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import kyo.yaz.condominium.manager.ui.views.domain.ExtraChargeViewItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExtraChargeMapper {

    ExtraChargeMapper MAPPER = Mappers.getMapper(ExtraChargeMapper.class);

    static ExtraCharge to(ExtraChargeViewItem item) {
        return MAPPER.map(item);
    }

    static ExtraChargeViewItem to(ExtraCharge ExtraCharge) {
        return MAPPER.map(ExtraCharge);
    }

    ExtraCharge map(ExtraChargeViewItem item);

    default ExtraChargeViewItem map(ExtraCharge item) {
        return new ExtraChargeViewItem(item.aptNumber(), item.description(), item.amount(), item.currency());
    }
}
