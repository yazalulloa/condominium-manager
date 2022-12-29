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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */

@SpringBootApplication
@EnableMongoRepositories()
@ComponentScan

@Theme(value = "condominium_manager")
@Push(PushMode.MANUAL)
@PWA(name = "Condominium Manager", shortName = "Condominium Manager", offlineResources = {})
@NpmPackage(value = "lumo-css-framework", version = "4.0.10")
@NpmPackage(value = "line-awesome", version = "1.3.0")
@NpmPackage(value = "@vaadin-component-factory/vcf-nav", version = "1.0.6")
@Slf4j
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {

        Single.fromCallable(NetworkUtil::getPublicIp)
                .subscribeOn(Schedulers.io())
                .subscribe(ip -> log.info("PUBLIC_IP {}", ip), throwable -> log.error("FAILED_TO_GET_PUBLIC_IP", throwable));

        SpringApplication.run(Application.class, args);
    }


}
