package kyo.yaz.condominium.manager;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kyo.yaz.condominium.manager.core.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */

@SpringBootApplication
@EnableMongoRepositories()
@EnableScheduling
@EnableAsync
@ComponentScan
@Theme(value = "condominium_manager")
@Push(PushMode.AUTOMATIC)
@PWA(name = "Condominium Manager", shortName = "Condominium Manager", offlineResources = {})
//@NpmPackage(value = "lumo-css-framework", version = "4.0.10")
//@NpmPackage(value = "line-awesome", version = "1.3.0")
@NpmPackage(value = "@vaadin-component-factory/vcf-nav", version = "1.0.6")
@Slf4j
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    public static void main(String[] args) {
        final var timeZone = TimeZone.getDefault();
        log.info(timeZone.toString());
        TimeZone.setDefault(timeZone);
        Single.fromCallable(NetworkUtil::getPublicIp)
                .subscribeOn(Schedulers.io())
                .subscribe(ip -> log.info("PUBLIC_IP {}", ip), throwable -> log.error("FAILED_TO_GET_PUBLIC_IP", throwable));

        SpringApplication.run(Application.class, args);
    }


}
