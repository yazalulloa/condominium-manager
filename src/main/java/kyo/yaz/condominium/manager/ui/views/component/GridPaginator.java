package kyo.yaz.condominium.manager.ui.views.component;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.TreeSet;

public class GridPaginator extends HorizontalLayout {
    private final ComboBox<Integer> itemsPerPageComBox = ViewUtil.itemPerPageComboBox();
    private final ComboBox<Integer> pageComboBox = new ComboBox<>();
    private final Runnable runnable;
    private long totalCount = 0;
    private long numberOfPages = 0;


    public GridPaginator(Runnable runnable) {
        this(0, runnable);
    }

    public GridPaginator(long totalCount, Runnable runnable) {
        this.runnable = runnable;
        set(totalCount);
        initViews();
    }

    public void set(long totalCount) {
        final var previousCount = this.totalCount;
        this.totalCount = totalCount;

        if (previousCount != totalCount) {
            calculatePages();
        }
    }

    private void calculatePages() {
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

        setVisible(!(numberOfPages == 1));
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
            pageComboBox.setValue(1);
        }
    }

    private void initViews() {

        itemsPerPageComBox.setAutoOpen(true);
        itemsPerPageComBox.setItemLabelGenerator(String::valueOf);
        pageComboBox.setAutoOpen(true);
        pageComboBox.setItemLabelGenerator(String::valueOf);

        itemsPerPageComBox.setWidth(80, Unit.PIXELS);
        pageComboBox.setWidth(80, Unit.PIXELS);

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
        });
        pageComboBox.addValueChangeListener(o -> {

            if (o.getValue() != null) {
                runnable.run();
            }
        });

        add(itemsPerPageComBox, firstPage, previousPage, pageComboBox, nextPage, lastPage);
    }

    public boolean goToFirstPage() {
        final var value = pageComboBox.getValue();

        if (value != null && value != 1) {
            pageComboBox.setValue(1);
            return true;
        }
        return false;
    }
}

