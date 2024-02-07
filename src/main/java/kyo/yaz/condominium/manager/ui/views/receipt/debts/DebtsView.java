package kyo.yaz.condominium.manager.ui.views.receipt.debts;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.component.DragDropDiv;
import kyo.yaz.condominium.manager.ui.views.component.DragDropList;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DebtsView extends BaseDiv {


  private final Span totalSpan = new Span();
  private final Span receiptNumberSpan = new Span();
  private final DragDropList<DebtViewItem, DragDropDiv<DebtViewItem>> debts = new DragDropList<>();
/*    private List<DebtViewItem> items = Collections.emptyList();
    private final Grid<DebtViewItem> grid = new Grid<>();*/

  private final TranslationProvider translationProvider;
  private final DebtForm form;
  private Currency currency;


  @Autowired
  public DebtsView(TranslationProvider translationProvider, DebtForm form) {
    super();
    this.translationProvider = translationProvider;
    this.form = form;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency;
  }

  public void init() {

    addClassName("debts-view");
    //configureGrid();
    configureForm();
    add(new H3(Labels.DEBTS), new Div(totalSpan), new Div(receiptNumberSpan), getContent());
    closeEditor();
  }

  private com.vaadin.flow.component.Component getContent() {

    final var content = new HorizontalLayout(debts, form);
    content.setFlexGrow(2, debts);
    content.setFlexGrow(1, form);
    content.addClassNames("content");
    return content;
  }

  public void setItems(List<DebtViewItem> debtViewItems) {
    debts.components().clear();
    debts.removeAll();
    logger().info("setItems {}", debtViewItems.size());
    debtViewItems.stream().map(this::addDebt).forEach(debts::addComponent);
    calculateDebtInfo();
  }

  private DragDropDiv<DebtViewItem> card(DebtViewItem item) {

    final var card = new DragDropDiv<>(item);
    card.addClassName("base-card");
    final var header = new Div(new Span(item.getAptNumber()), new Span(item.getName()));
    header.addClassName("header");
    final var body = new Div(new Span("Recibos: %s".formatted(item.getReceipts())),
        new Span("Monto: %s".formatted(ConvertUtil.format(item.getAmount(), currency))));

    if (item.getMonths() != null && !item.getMonths().isEmpty()) {
      final var str = item.getMonths().stream().map(Enum::name).map(translationProvider::translate)
          .collect(Collectors.joining(", "));
      body.add(new Span("Meses: " + str));
    }

    if (item.getPreviousPaymentAmount() != null && item.getPreviousPaymentAmountCurrency() != null) {
      body.add(new Span(
          "Abono: " + ConvertUtil.format(item.getPreviousPaymentAmount(), item.getPreviousPaymentAmountCurrency())));
    }

    body.addClassName("body");

    final var buttons = new Div(editBtn(new Button(), item), resetBtn(new Button(), item));
    buttons.addClassName("buttons");
    card.add(header, body, buttons);

    return card;
  }

  private void configureForm() {
    form.setHeightFull();

    form.addListener(DebtForm.SaveEvent.class, event -> {

      logger().info("SAVING {}", debts.components().size());
      final var item = event.getObj();
      debts.saveOrUpdate(item, this::addDebt);
      closeEditor();
      calculateDebtInfo();
      logger().info("SAVING {}", debts.components().size());
    });

    form.addListener(DebtForm.DeleteEvent.class, event -> resetItem(event.getObj()));
    form.addListener(DebtForm.CloseEvent.class, e -> closeEditor());
  }

  private DragDropDiv<DebtViewItem> addDebt(DebtViewItem item) {

    final var card = card(item);
    card.addClickListener(e -> {
      if (e.getClickCount() == 2) {
        editEntity(item);
      }
    });

    return card;
  }

  private Button editBtn(Button button, DebtViewItem item) {
    button.addThemeVariants(ButtonVariant.LUMO_ICON,
        ButtonVariant.LUMO_SUCCESS,
        ButtonVariant.LUMO_TERTIARY);
    button.addClickListener(e -> editEntity(item));
    button.setIcon(new Icon(VaadinIcon.EDIT));
    return button;
  }

  private Button resetBtn(Button button, DebtViewItem item) {
    button.addThemeVariants(ButtonVariant.LUMO_ICON,
        ButtonVariant.LUMO_ERROR,
        ButtonVariant.LUMO_TERTIARY);
    button.addClickListener(e -> resetItem(item));
    button.setIcon(new Icon(VaadinIcon.CLOSE_SMALL));
    return button;
  }

  public void editEntity(DebtViewItem item) {
    if (item == null) {
      closeEditor();
    } else {
      form.setItem(item);
      form.setVisible(true);
      addClassName("editing");
    }
  }

  private void resetItem(DebtViewItem item) {
    item.setAmount(BigDecimal.ZERO);
    item.setReceipts(0);
    item.setMonths(Collections.emptySet());
    item.setPreviousPaymentAmount(BigDecimal.ZERO);
    item.setPreviousPaymentAmountCurrency(null);

    logger().info("resetItem {}", debts.components().size());
    debts.saveOrUpdate(item, this::addDebt);

    closeEditor();
    calculateDebtInfo();

    logger().info("resetItem {}", debts.components().size());
  }

  private void closeEditor() {
    form.setItem(null);
    form.setVisible(false);
    removeClassName("editing");
  }

  public List<DebtViewItem> debts() {
    return debts.components().stream().map(DragDropDiv::item).collect(Collectors.toCollection(LinkedList::new));
  }

  private void calculateDebtInfo() {
    logger().info("DEBTS {}", debts.components().size());
    final var total = debts().stream().map(DebtViewItem::getAmount).reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);

    final var receipts = debts().stream().map(DebtViewItem::getReceipts).reduce(Integer::sum)
        .orElse(0);

        /*var total = BigDecimal.ZERO;
        var receipts = 0;
        for (DebtViewItem debt : debts()) {
            total = total.add(debt.getAmount());
            receipts += debt.getReceipts();
        }*/

    totalSpan.setText("Deuda total: %s".formatted(currency.format(total)));
    receiptNumberSpan.setText("Recibos: %s".formatted(receipts));
  }
}
