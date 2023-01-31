package kyo.yaz.condominium.manager.core.component;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import kyo.yaz.condominium.manager.ui.views.LoginView;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.oauth2Login().loginPage("/" + LoginView.URL).permitAll();
    }
}
