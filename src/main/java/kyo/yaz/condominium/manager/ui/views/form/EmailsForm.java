package kyo.yaz.condominium.manager.ui.views.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EmailsForm extends VerticalLayout {

    private final List<Component> emailComponents = new ArrayList<Component>();

    public EmailsForm() {
        this(null);
    }

    public EmailsForm(Set<String> emails) {
        Button plusButton = new Button("Añadir email", new Icon(VaadinIcon.PLUS));
        plusButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        plusButton.getElement().setAttribute("aria-label", "Add Email");

        plusButton.addClickListener(v -> add(emailComponent(null)));

        add(plusButton);

        setEmails(emails);
    }

    public void setEmails(Set<String> emails) {

        if (!CollectionUtils.isEmpty(emails)) {
            emails.stream().map(this::emailComponent)
                    .forEach(this::add);
        }
    }

    public void clearEmailComponents() {
        emailComponents.forEach(this::remove);
        emailComponents.clear();
    }


    private Component emailComponent(String email) {
        final var emailField = new EmailField();
        emailField.setWidthFull();
        //emailField.setLabel("Email " + ++count);
        if (email != null) {
            emailField.setValue(email);
        }

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
        closeButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        closeButton.getElement().setAttribute("aria-label", "Delete email");

        final var horizontalLayout = new HorizontalLayout(emailField, closeButton);
        horizontalLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        closeButton.addClickListener(v -> {
            remove(horizontalLayout);
            emailComponents.remove(horizontalLayout);
        });
        emailComponents.add(horizontalLayout);

        add(horizontalLayout);
        return horizontalLayout;
    }
}
