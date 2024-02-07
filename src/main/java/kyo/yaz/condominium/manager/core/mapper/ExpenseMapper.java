package kyo.yaz.condominium.manager.core.mapper;

import kyo.yaz.condominium.manager.core.util.ObjectUtil;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.ui.views.domain.ExpenseViewItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExpenseMapper {

  ExpenseMapper MAPPER = Mappers.getMapper(ExpenseMapper.class);

  static Expense to(ExpenseViewItem item) {
    return MAPPER.map(item);
  }

  static ExpenseViewItem to(Expense expense) {
    return MAPPER.map(expense);
  }

  Expense map(ExpenseViewItem item);

  default ExpenseViewItem map(Expense expense) {
    return new ExpenseViewItem(expense.description(), expense.amount(), expense.currency(),
        ObjectUtil.aBoolean(expense.reserveFund()), expense.type());
  }
}
