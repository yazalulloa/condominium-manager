package kyo.yaz.condominium.manager.ui.views.domain;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import kyo.yaz.condominium.manager.ui.views.util.Labels;

public class DeleteDialog extends Dialog {

    private Runnable deleteAction = () -> {
    };

    public DeleteDialog() {
        super();


        final var deleteButton = new Button(Labels.DELETE, (e) -> {
            deleteAction.run();
            this.close();
        });


        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR);
        deleteButton.getStyle().set("margin-right", "auto");
        this.getFooter().add(deleteButton);

        final var cancelButton = new Button(Labels.CANCEL, (e) -> this.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        this.getFooter().add(cancelButton);
        this.setModal(true);
    }

    public void setDeleteAction(Runnable deleteAction) {
        this.deleteAction = deleteAction;
    }
}
