package kyo.yaz.condominium.manager.ui.views.building;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.persistence.domain.Expense.Type;
import kyo.yaz.condominium.manager.ui.views.base.BaseDiv;
import kyo.yaz.condominium.manager.ui.views.component.DragDropDiv;
import kyo.yaz.condominium.manager.ui.views.component.DragDropList;
import kyo.yaz.condominium.manager.ui.views.domain.ReserveFundViewItem;
import kyo.yaz.condominium.manager.ui.views.util.IconUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.Labels.ReserveFund;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

@org.springframework.stereotype.Component
@Scope("prototype")
public class ReserveFundView extends BaseDiv {

  //private final LinkedList<ReserveFundViewItem> items = new LinkedList<>();
  private final Button addBtn = new Button(Labels.ADD);
  //private final Grid<ReserveFundViewItem> grid = new Grid<>(ReserveFundViewItem.class, false);
  private final ReserveFundForm form;

  private final DragDropList<ReserveFundViewItem, DragDropDiv<ReserveFundViewItem>> funds = new DragDropList<>();

  //private ReserveFundViewItem draggedItem;

  private final TranslationProvider translationProvider;

  @Autowired
  public ReserveFundView(ReserveFundForm form, TranslationProvider translationProvider) {
    this.form = form;
    this.translationProvider = translationProvider;
  }

  public void init() {
    addClassName("reserve-funds-view");
    configureForm();
    add(new HorizontalLayout(new H3(Labels.RESERVE_FUNDS_TITLE), addBtn), getContent());
    closeEditor();
  }

  public Collection<ReserveFundViewItem> items() {
    return funds.components().stream().map(DragDropDiv::item).collect(Collectors.toCollection(LinkedList::new));
  }


  private Component getContent() {

    addBtn.setDisableOnClick(true);
    addBtn.addClickListener(click -> addEntity());

    final var content = new HorizontalLayout(funds, form);
    content.setFlexGrow(2, funds);
    content.setFlexGrow(1, form);
    content.addClassNames("content");
    return content;
  }

  private void configureForm() {
    form.setWidth(30, Unit.EM);
    form.setHeightFull();
    form.addListener(ReserveFundForm.SaveEvent.class, event -> {

      funds.saveOrUpdate(event.getObj(), this::addItem);
      closeEditor();
    });
    form.addListener(ReserveFundForm.DeleteEvent.class, e -> removeItem(e.getObj()));
    form.addListener(ReserveFundForm.CloseEvent.class, e -> closeEditor());
  }

  private DragDropDiv<ReserveFundViewItem> card(ReserveFundViewItem item) {

    final var card = new DragDropDiv<>(item);
    card.addClassName("card");

    final var isActive = new Span(new Span(Labels.ReserveFund.ACTIVE_LABEL + ": "),
        IconUtil.checkMarkOrCross(item.getActive()));

    final var pay = Optional.ofNullable(item.getPay()).map(BigDecimal::toString)
        .orElse("");

    final var type = Optional.ofNullable(item.getType())
        .map(Enum::name)
        .orElse("");

    final var addToExpenses = new Span(new Span(Labels.ReserveFund.ADD_TO_EXPENSES_LABEL + ": "),
        IconUtil.checkMarkOrCross(Optional.ofNullable(item.getAddToExpenses()).orElse(true)));

    final var body = new Div(new Span(item.getName()), new Span(item.getFund().toString()), isActive,
        new Span(Labels.ReserveFund.TYPE_LABEL + ": " + translationProvider.translate(type)),
        new Span("Monto a pagar: " + pay),
        new Span(ReserveFund.EXPENSE_TYPE_LABEL + ": %s".formatted(
            translationProvider.translate(Optional.ofNullable(item.getExpenseType()).orElse(Type.COMMON).name()))),
        addToExpenses);

    body.addClassName("body");

    final var buttons = new Div(editBtn(new Button(), item), copyBtn(new Button(), item),
        deleteBtn(new Button(), item));
    buttons.addClassName("buttons");
    card.add(body, buttons);

    return card;
  }

  private Button deleteBtn(Button button, ReserveFundViewItem item) {
    button.addThemeVariants(ButtonVariant.LUMO_ICON,
        ButtonVariant.LUMO_ERROR,
        ButtonVariant.LUMO_TERTIARY);
    button.addClickListener(e -> this.removeItem(item));
    button.setIcon(new Icon(VaadinIcon.TRASH));
    return button;
  }

  private Button copyBtn(Button button, ReserveFundViewItem item) {
    button.addThemeVariants(ButtonVariant.LUMO_ICON,
        ButtonVariant.LUMO_SUCCESS,
        ButtonVariant.LUMO_TERTIARY);
    button.addClickListener(e -> {
      final var newItem = item.toBuilder().build();
      editEntity(newItem);
    });
    button.setIcon(new Icon(VaadinIcon.COPY));
    return button;
  }

  private Button editBtn(Button button, ReserveFundViewItem item) {
    button.addThemeVariants(ButtonVariant.LUMO_ICON,
        ButtonVariant.LUMO_PRIMARY,
        ButtonVariant.LUMO_PRIMARY);
    button.addClickListener(e -> editEntity(item));
    button.setIcon(new Icon(VaadinIcon.EDIT));
    return button;
  }

  private DragDropDiv<ReserveFundViewItem> addItem(ReserveFundViewItem item) {

    final var card = card(item);
    card.addClickListener(e -> {
      if (e.getClickCount() == 2) {
        editEntity(item);
      }
    });
    return card;
  }

  private void addEntity() {
    editEntity(form.defaultItem());
  }

  public void editEntity(ReserveFundViewItem item) {
    if (item == null) {
      closeEditor();
    } else {
      form.setItem(item);
      form.setVisible(true);
      addClassName("editing");
    }
  }

  private void removeItem(ReserveFundViewItem item) {
    funds.removeItem(item);
    form.setItem(form.defaultItem());
  }


  private void closeEditor() {
    form.setItem(null);
    form.setVisible(false);
    addBtn.setEnabled(true);
    removeClassName("editing");
  }


  public void setItems(Collection<ReserveFundViewItem> collection) {
    funds.removeAll();
    funds.components().clear();
    collection.stream().map(this::addItem).forEach(funds::addComponent);
  }

}
