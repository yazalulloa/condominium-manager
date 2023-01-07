package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.util.Labels;

import java.util.Objects;
import java.util.function.Consumer;

public class ReceiptGridContextMenu extends GridContextMenu<Receipt> {

    private final Consumer<Receipt> copyConsumer;
    private final Consumer<Receipt> deleteConsumer;
    private final Consumer<Receipt> sendEmailsConsumer;

    public ReceiptGridContextMenu(Grid<Receipt> target, Consumer<Receipt> copyConsumer, Consumer<Receipt> deleteConsumer, Consumer<Receipt> sendEmailsConsumer) {
        super(target);
        this.copyConsumer = copyConsumer;
        this.deleteConsumer = deleteConsumer;
        this.sendEmailsConsumer = sendEmailsConsumer;


        addItem(Labels.COPY, e -> e.getItem().ifPresent(copyConsumer));
        addItem(Labels.SEND_EMAIL, e -> e.getItem().ifPresent(sendEmailsConsumer));
        add(new Hr());


        addItem(Labels.DELETE, e -> e.getItem().ifPresent(deleteConsumer));

        // Do not show context menu when header is clicked
        setDynamicContentHandler(Objects::nonNull);
    }
}
