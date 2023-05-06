package kyo.yaz.condominium.manager.core.component;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import kyo.yaz.condominium.manager.ui.views.LoginView;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.oauth2Login().loginPage("/" + LoginView.URL).permitAll()
                .and()
                .authorizeHttpRequests()
                .requestMatchers(new AntPathRequestMatcher("/actuator/health"))
                .permitAll();


        super.configure(http);
    }
}
