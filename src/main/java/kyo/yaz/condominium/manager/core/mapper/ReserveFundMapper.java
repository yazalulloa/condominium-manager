package kyo.yaz.condominium.manager.core.mapper;

import kyo.yaz.condominium.manager.persistence.domain.ReserveFund;
import kyo.yaz.condominium.manager.ui.views.domain.ReserveFundViewItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReserveFundMapper {

  ReserveFundMapper MAPPER = Mappers.getMapper(ReserveFundMapper.class);

  static ReserveFund to(ReserveFundViewItem item) {
    return MAPPER.map(item);
  }

  static ReserveFundViewItem to(ReserveFund ReserveFund) {
    return MAPPER.map(ReserveFund);
  }

  default ReserveFund map(ReserveFundViewItem item) {
    return new ReserveFund(item.getName(), item.getFund(), item.getPay(), item.getActive(), item.getType(),
        item.getExpenseType(), item.getAddToExpenses());
  }

  default ReserveFundViewItem map(ReserveFund item) {
    return new ReserveFundViewItem(item.name(), item.fund(), item.pay(), item.active(), item.type(), item.expenseType(),
        item.addToExpenses());
  }
}
