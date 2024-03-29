package kyo.yaz.condominium.manager.ui.views.component;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.shared.Registration;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.TreeSet;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GridPaginator extends Div implements
    HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<Integer>, Integer>> {

  private final ComboBox<Integer> itemsPerPageComBox = ViewUtil.itemPerPageComboBox();
  private final ComboBox<Integer> pageComboBox = new ComboBox<>();
  private final Runnable runnable;
  private long totalCount = 0;
  private long numberOfPages = 0;
  private boolean isInit = false;

  private Registration pageComboBoxRegistration;

  public GridPaginator(Runnable runnable) {

    this.runnable = runnable;
    addClassName("grid-paginator");
    set(totalCount, totalCount);
    //init();

    itemsPerPageComBox.addClassName("items-per-page");
    pageComboBox.addClassName("page");
  }

  public void set(long currentCount, long fullCount) {
    final var previousCount = this.totalCount;
    this.totalCount = currentCount;

    if (previousCount != totalCount) {
      calculatePages();
    }

    setVisible(fullCount > 0);
  }

  private void calculatePages() {
    //log.info("Calculating pages");

    final var pageSize = itemsPerPageComBox.getValue();
    final var oldNumberOfPages = numberOfPages;
    if (totalCount == 0) {
      numberOfPages = 1;
    } else {

      numberOfPages = BigDecimal.valueOf(totalCount).divide(BigDecimal.valueOf(pageSize), 0, RoundingMode.UP)
          .longValue();
    }

    if (oldNumberOfPages != numberOfPages) {
      setPages();
    }

    setVisible(numberOfPages > 1);
  }

  public long totalCount() {
    return totalCount;
  }

  public long numberOfPages() {
    return numberOfPages;
  }

  public int itemsPerPage() {
    return itemsPerPageComBox.getValue();
  }

  public int currentPage() {
    return Optional.ofNullable(pageComboBox.getValue())
        .map(v -> v - 1)
        .orElse(0);
  }

  private void setPages() {
    var pages = numberOfPages;

    final var pageList = new TreeSet<>(Integer::compareTo);
    while (pages > 0) {
      pageList.add((int) pages--);
    }

    pageComboBox.setItems(pageList);
    if (pageComboBox.getValue() == null || pageComboBox.getValue() == 0) {
      removePageComboBoxListener();
      pageComboBox.setValue(1);
      setPageComboBoxListener();
    }
  }

  public void init() {
    if (isInit) {
      return;
    }

    itemsPerPageComBox.setAutoOpen(true);
    itemsPerPageComBox.setItemLabelGenerator(String::valueOf);
    pageComboBox.setAutoOpen(true);
    pageComboBox.setItemLabelGenerator(String::valueOf);

    // itemsPerPageComBox.setWidth(80, Unit.EM);
    // pageComboBox.setWidth(80, Unit.EM);

    final var firstPage = new Button();
    final var previousPage = new Button();
    final var nextPage = new Button();
    final var lastPage = new Button();

    firstPage.setIcon(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT));
    firstPage.addClickListener(v -> {
      if (pageComboBox.getValue() != 1) {
        pageComboBox.setValue(1);
      }
    });

    previousPage.setIcon(new Icon(VaadinIcon.ANGLE_LEFT));
    previousPage.addClickListener(v -> {

      final var value = pageComboBox.getValue();
      if (value != 1) {
        pageComboBox.setValue(value - 1);
      }
    });

    nextPage.setIcon(new Icon(VaadinIcon.ANGLE_RIGHT));
    nextPage.addClickListener(v -> {

      final var value = pageComboBox.getValue();
      if (value != numberOfPages) {
        pageComboBox.setValue(value + 1);
      }
    });

    lastPage.setIcon(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT));
    lastPage.addClickListener(v -> {

      if (pageComboBox.getValue() != numberOfPages) {
        pageComboBox.setValue((int) numberOfPages);
      }
    });

    itemsPerPageComBox.addValueChangeListener(o -> {
      calculatePages();
      run();
    });
    setPageComboBoxListener();

    add(itemsPerPageComBox, firstPage, previousPage, pageComboBox, nextPage, lastPage);
    isInit = true;
  }

  public boolean goToFirstPage() {
    final var value = pageComboBox.getValue();

    if (value != null && value != 1) {
      pageComboBox.setValue(1);
      return true;
    }
    return false;
  }

  private void removePageComboBoxListener() {
    if (pageComboBoxRegistration != null) {
      pageComboBoxRegistration.remove();
      pageComboBoxRegistration = null;
    }
  }

  private void setPageComboBoxListener() {
    removePageComboBoxListener();
    pageComboBoxRegistration = pageComboBox.addValueChangeListener(this);
  }

  private void run() {
    if (runnable != null) {
      runnable.run();
    }
  }

  @Override
  public void valueChanged(AbstractField.ComponentValueChangeEvent<ComboBox<Integer>, Integer> event) {
    if (event.getValue() != null) {
      //log.info("runnable {}", event.getValue());
      run();
    }
  }
}

