package kyo.yaz.condominium.manager.core.mapper;


import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.ui.views.receipt.debts.DebtViewItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DebtMapper {

  DebtMapper MAPPER = Mappers.getMapper(DebtMapper.class);

  static Debt to(DebtViewItem item) {
    return MAPPER.map(item);
  }

  static DebtViewItem to(Debt Debt) {
    return MAPPER.map(Debt);
  }

  Debt map(DebtViewItem item);

  default DebtViewItem map(Debt debt) {
    return new DebtViewItem(debt.aptNumber(), debt.name(), debt.receipts(), debt.amount(), debt.months(),
        debt.previousPaymentAmount(), debt.previousPaymentAmountCurrency());
  }
}
