package kyo.yaz.condominium.manager.ui.views.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EmailsForm extends VerticalLayout {

    private final List<EmailComponent> emailComponents = new ArrayList<>();

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

    public Set<String> getEmails() {
        return emailComponents.stream().map(EmailComponent::getEmail)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }


    private EmailComponent emailComponent(String email) {

        final var emailComponent = new EmailComponent(email);

        emailComponent.closeListener(() -> {

            remove(emailComponent);
            emailComponents.remove(emailComponent);
        });


        emailComponents.add(emailComponent);
        return emailComponent;


      /*  final var emailField = new EmailField();
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
        return horizontalLayout;*/
    }

    private static class EmailComponent extends HorizontalLayout {
        private final EmailField emailField = new EmailField();
        private final Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));

        public EmailComponent(String email) {
            super();

            emailField.setWidthFull();
            //emailField.setLabel("Email " + ++count);
            if (email != null) {
                emailField.setValue(email);
            }


            closeButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            closeButton.getElement().setAttribute("aria-label", "Delete email");

            add(emailField, closeButton);
        }

        public void closeListener(Runnable runnable) {
            closeButton.addClickListener(v -> runnable.run());
        }

        public String getEmail() {
            return emailField.getValue();
        }
    }
}
