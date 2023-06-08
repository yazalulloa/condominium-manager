package kyo.yaz.condominium.manager;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;
import kyo.yaz.condominium.manager.core.util.EnvUtil;
import kyo.yaz.condominium.manager.core.util.NetworkUtil;
import kyo.yaz.condominium.manager.ui.views.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets and some desktop browsers.
 */

@SpringBootApplication
@EnableMongoRepositories()
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = {
    "kyo.yaz.condominium.manager"
})
@ServletComponentScan
@Theme(value = "condominium_manager")
@Push(PushMode.AUTOMATIC)
@PWA(name = "Condominium Manager", shortName = "CM")
//@NpmPackage(value = "lumo-css-framework", version = "4.0.10")
//@NpmPackage(value = "line-awesome", version = "1.3.0")
@NpmPackage(value = "@vaadin-component-factory/vcf-nav", version = "1.0.6")
@Slf4j
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

  public static void main(String[] args) {
    NetworkUtil.showPublicIp();

    FileUtil.writeEnvToFile("APPLICATION_FILE", "application.yml");
    FileUtil.writeEnvToFile("VERTICLES_FILE", "verticles.yml");

    if (EnvUtil.isShowDir()) {
      FileUtil.showDir();
    }

    SpringApplication.run(Application.class, args);
  }



   /* public void onStartup(ServletContext container) throws ServletException {
        AnnotationConfigWebApplicationContext ctx
                = new AnnotationConfigWebApplicationContext();
        ctx.register(WebMvcConfigure.class);
        ctx.setServletContext(container);

        ServletRegistration.Dynamic servlet = container.addServlet(
                "dispatcherExample", new DispatcherServlet(ctx));
        servlet.setLoadOnStartup(1);
        servlet.addMapping("/");
    }*/
}
