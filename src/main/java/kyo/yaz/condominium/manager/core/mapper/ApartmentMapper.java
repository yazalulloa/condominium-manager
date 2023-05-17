package kyo.yaz.condominium.manager.core.mapper;

import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.ui.views.domain.ApartmentViewItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

@Mapper
public interface ApartmentMapper {

    ApartmentMapper MAPPER = Mappers.getMapper(ApartmentMapper.class);

    static Apartment to(ApartmentViewItem item) {
        return MAPPER.map(item);
    }

    static ApartmentViewItem to(Apartment Apartment) {
        return MAPPER.map(Apartment);
    }

    default Apartment map(ApartmentViewItem item) {
        final var apartmentId = new Apartment.ApartmentId(item.getBuildingId(), item.getNumber());
        return new Apartment(apartmentId, item.getName(), item.getIdDoc(), item.getEmails(), item.getAmountToPay());
    }

    default ApartmentViewItem map(Apartment apartment) {

        final var optional = Optional.ofNullable(apartment.apartmentId());
        return new ApartmentViewItem(
                optional.map(Apartment.ApartmentId::buildingId).orElse(null),
                optional.map(Apartment.ApartmentId::number).orElse(null), apartment.name(), apartment.idDoc(), apartment.emails(),
                apartment.amountToPay());
    }
}
