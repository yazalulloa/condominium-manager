package kyo.yaz.condominium.manager.core.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContextListener;
import java.util.List;
import kyo.yaz.condominium.manager.core.component.ServletContextListenerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
@Slf4j
public class AppConfig {

  @Bean
  public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter(
      @Value("${app.oauth2_google_url}") String oauthUrl) {
    ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
    FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    registration.setUrlPatterns(List.of(oauthUrl));
    log.info("Registering forwardHeaderFilter");
    return registration;
  }

  @Bean
  ServletListenerRegistrationBean<ServletContextListener> servletListener(
      ServletContextListenerImpl servletContextListener
  ) {
    ServletListenerRegistrationBean<ServletContextListener> srb
        = new ServletListenerRegistrationBean<>();
    srb.setListener(servletContextListener);
    servletContextListener.addHook();
    return srb;
  }
}
