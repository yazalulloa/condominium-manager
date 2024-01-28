package kyo.yaz.condominium.manager.core.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.mongodb.MongoMetricsCommandListener;
import io.micrometer.core.instrument.binder.mongodb.MongoMetricsConnectionPoolListener;
import io.micrometer.observation.ObservationRegistry;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContextListener;
import java.util.List;
import java.util.Map;
import kyo.yaz.condominium.manager.core.component.ServletContextListenerImpl;
import kyo.yaz.condominium.manager.core.service.entity.EntityDownloader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.observability.ContextProviderFactory;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;
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

  @Bean
  public Map<String, EntityDownloader> downloadersMap(ApplicationContext context) {
    return context.getBeansOfType(EntityDownloader.class);
  }

  @Bean
  MongoClientSettingsBuilderCustomizer mongoMetricsSynchronousContextProvider(ObservationRegistry registry) {
    return (clientSettingsBuilder) -> {
      clientSettingsBuilder.contextProvider(ContextProviderFactory.create(registry))
              .addCommandListener(new MongoObservationCommandListener(registry));
    };
  }

}
