package kyo.yaz.condominium.manager;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;
import kyo.yaz.condominium.manager.core.util.EnvUtil;
import kyo.yaz.condominium.manager.core.util.FileUtil;
import kyo.yaz.condominium.manager.core.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets and some desktop browsers.
 */

@SpringBootApplication
@EnableMongoRepositories()
@EnableCaching
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

    public static void main(String[] args) throws IOException {
        EnvUtil.saveAppStartedAt();
        NetworkUtil.showPublicIp();

        Files.createDirectories(Paths.get("config"));
        FileUtil.writeEnvToFile("APPLICATION_FILE", "config/application.yml");
        FileUtil.writeEnvToFile("VERTICLES_FILE", "config/verticles.yml");

        if (EnvUtil.isShowDir()) {
            FileUtil.showDir();
        }

        SpringApplication.run(Application.class, args);
    }
}
