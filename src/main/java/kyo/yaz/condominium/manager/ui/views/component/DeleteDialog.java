package kyo.yaz.condominium.manager.ui.views.component;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import kyo.yaz.condominium.manager.ui.views.util.Labels;

public class DeleteDialog extends ConfirmDialog {

  private Runnable deleteAction = () -> {
  };

  public DeleteDialog() {
    super();

    setCancelable(true);
    addCancelListener(e -> close());
    setCancelText(Labels.CANCEL);

    setConfirmText(Labels.DELETE);
    addConfirmListener(e -> {
      deleteAction.run();
      this.close();
    });



      /*  final var deleteButton = new Button(Labels.DELETE, (e) -> {
            deleteAction.run();
            this.close();
        });


        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR);
        deleteButton.getStyle().set("margin-right", "auto");
        this.getFooter().add(deleteButton);

        final var cancelBtn = new Button(Labels.CANCEL, (e) -> this.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        this.getFooter().add(cancelBtn);
        this.setModal(true);*/
  }

  public void setDeleteAction(Runnable deleteAction) {
    this.deleteAction = deleteAction;
  }
}
