package com.ftn.sbnz.service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ftn.sbnz.model.models.News;
import com.ftn.sbnz.model.models.Source;
import com.ftn.sbnz.model.models.Reputation;
import com.ftn.sbnz.model.models.SpreadEvent;

@SpringBootApplication
public class ServiceApplication  {
    
    private static Logger log = LoggerFactory.getLogger(ServiceApplication.class);

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(ServiceApplication.class, args);

        // ispis svih bean-ova
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        StringBuilder sb = new StringBuilder("Application beans:\n");
        for (String beanName : beanNames) {
            sb.append(beanName).append("\n");
        }
        log.info(sb.toString());

        // Kreiraj CEP sesiju sa pseudo satom
        KieContainer kieContainer = ctx.getBean(KieContainer.class);
        KieSession kieSession = kieContainer.newKieSession("ksession-rules");
        
        // Dobijanje pseudo sata
        org.kie.api.time.SessionPseudoClock clock = (org.kie.api.time.SessionPseudoClock) kieSession.getSessionClock();

        // TEST VIJEST 1 - Normalna distribucija (2-3 događaja)
        News news1 = new News();
        news1.setId(1L);
        news1.setTitle("SOKANTNO!!! Voda je opasna!");
        news1.setContent("Ekskluziva o opasnostima vode...");
        news1.setSource(new Source("Vijesti365", Reputation.UNKNOWN));

        // TEST VIJEST 2 - Koordinisano širenje (4-5 događaja)
        News news2 = new News();
        news2.setId(2L);
        news2.setTitle("Nova studija otkriva prednosti caja");
        news2.setContent("Naučnici su potvrdili zdravstvene prednosti...");
        news2.setSource(new Source("PouzdaniIzvor", Reputation.TRUSTED));

        // Ubaci sve vijesti u sesiju
        kieSession.insert(news1);
        kieSession.insert(news2);

        System.out.println("=== POČETAK SIMULACIJE ŠIRENJA VIJESTI ===\n");

        // SIMULACIJA ŠIRENJA VIJESTI 1 (Normalna distribucija - 3 događaja)
        // System.out.println("Širenje Vijest 1...");
        kieSession.insert(new SpreadEvent(1L, 1L, "Vijesti365", clock.getCurrentTime()));
        clock.advanceTime(2, TimeUnit.MINUTES);
        kieSession.fireAllRules();

        kieSession.insert(new SpreadEvent(2L, 1L, "BrzeVijesti", clock.getCurrentTime()));
        clock.advanceTime(1, TimeUnit.MINUTES);
        kieSession.fireAllRules();

        kieSession.insert(new SpreadEvent(3L, 1L, "ExpressNews", clock.getCurrentTime()));
        clock.advanceTime(1, TimeUnit.MINUTES);
        kieSession.fireAllRules();

        // SIMULACIJA ŠIRENJA VIJESTI 2 (Koordinisano širenje - 5 događaja)
        // System.out.println("\nŠirenje Vijest 2...");
        for (int i = 1; i <= 5; i++) {
            kieSession.insert(new SpreadEvent(10L + i, 2L, "Source" + i, clock.getCurrentTime()));
            clock.advanceTime(30, TimeUnit.SECONDS);
            kieSession.fireAllRules();
        }

        // Još jedan konačni fireAllRules za sve preostale pravila
        kieSession.fireAllRules();

        System.out.println("\n=== REZULTATI KLASIFIKACIJE ===");
        
        System.out.println("\nVijest 1: '" + news1.getTitle() + "'");
        System.out.println("Pouzdanost: " + news1.getConfidence());
        System.out.println("Tip distribucije: " + news1.getDistributionType());
        System.out.println("Objašnjenje: " + news1.getExplanation());

        System.out.println("\nVijest 2: '" + news2.getTitle() + "'");
        System.out.println("Pouzdanost: " + news2.getConfidence());
        System.out.println("Tip distribucije: " + news2.getDistributionType());
        System.out.println("Objašnjenje: " + news2.getExplanation());

        System.out.println("\n=== KRAJ SIMULACIJE ===");

        kieSession.dispose();
    }

    @Bean
    public KieContainer kieContainer() {
        KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks
                .newKieContainer(ks.newReleaseId("com.ftn.sbnz", "kjar", "0.0.1-SNAPSHOT"));
        KieScanner kScanner = ks.newKieScanner(kContainer);
        kScanner.start(1000);
        return kContainer;
    }
}