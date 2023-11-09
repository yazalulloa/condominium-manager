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

  static ReserveFundViewItem to(ReserveFund reserveFund) {
    return MAPPER.map(reserveFund);
  }

   default ReserveFund map(ReserveFundViewItem item) {
    return ReserveFund.builder()
        .name(item.getName())
        .fund(item.getFund())
        .expense(item.getExpense())
        .pay(item.getPay())
        .active(item.getActive())
        .type(item.getType())
        .expenseType(item.getExpenseType())
        .addToExpenses(item.getAddToExpenses())
        .build();
   }

   default ReserveFundViewItem map(ReserveFund item) {
    return ReserveFundViewItem.builder()
        .name(item.getName())
        .fund(item.getFund())
        .expense(item.getExpense())
        .pay(item.getPay())
        .active(item.getActive())
        .type(item.getType())
        .expenseType(item.getExpenseType())
        .addToExpenses(item.getAddToExpenses())
        .build();
   }
}
