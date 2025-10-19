package com.ftn.sbnz.service;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ftn.sbnz.model.models.News;
import com.ftn.sbnz.model.models.ConfidenceCategory;

@Service
public class ForwardChainingService {

    private static Logger log = LoggerFactory.getLogger(ForwardChainingService.class);

    private final KieContainer kieContainer;

    @Autowired
    public ForwardChainingService(KieContainer kieContainer) {
        log.info("Initialising ForwardChainingService.");
        this.kieContainer = kieContainer;
    }

    /**
     * Klasifikuje vijesti koristeći forward chaining pravila
     */
    public List<News> classifyNews(List<News> newsList) {
        KieSession kieSession = kieContainer.newKieSession("ksession-rules");
        
        try {
            // Ubaci sve vijesti u sesiju
            for (News news : newsList) {
                kieSession.insert(news);
                if (news.getSource() != null) {
                    kieSession.insert(news.getSource());
                }
            }
            
            // Pokreni forward chaining pravila
            int rulesFired = kieSession.fireAllRules();
            log.info("Forward Chaining - Pokrenuto {} pravila", rulesFired);
            
            return newsList;
            
        } finally {
            kieSession.dispose();
        }
    }

    /**
     * Klasifikuje jednu vijest
     */
    public News classifySingleNews(News news) {
        KieSession kieSession = kieContainer.newKieSession("ksession-rules");
        
        try {
            kieSession.insert(news);
            if (news.getSource() != null) {
                kieSession.insert(news.getSource());
            }
            
            int rulesFired = kieSession.fireAllRules();
            log.info("Forward Chaining - Pokrenuto {} pravila za vijest: {}", rulesFired, news.getTitle());
            
            return news;
            
        } finally {
            kieSession.dispose();
        }
    }

    /**
     * Analizira vijesti i vraća statistiku
     */
    public NewsClassificationResult analyzeNews(List<News> newsList) {
        List<News> classifiedNews = classifyNews(newsList);
        
        NewsClassificationResult result = new NewsClassificationResult();
        result.setAllNews(classifiedNews);
        
        // Grupiši po kategorijama
        for (News news : classifiedNews) {
            switch (news.getConfidence()) {
                case POUZDANA:
                    result.addTrustedNews(news);
                    break;
                case POTENCIJALNO_LAZNA:
                    result.addPotentiallyFakeNews(news);
                    break;
                case SUMNJIVA:
                    result.addSuspiciousNews(news);
                    break;
                default:
                    result.addUnclassifiedNews(news);
                    break;
            }
        }
        
        return result;
    }

    // Helper klasa za rezultate klasifikacije
    public static class NewsClassificationResult {
        private List<News> allNews = new ArrayList<>();
        private List<News> trustedNews = new ArrayList<>();
        private List<News> potentiallyFakeNews = new ArrayList<>();
        private List<News> suspiciousNews = new ArrayList<>();
        private List<News> unclassifiedNews = new ArrayList<>();
        
        // Setters
        public void setAllNews(List<News> allNews) { this.allNews = allNews; }
        public void addTrustedNews(News news) { this.trustedNews.add(news); }
        public void addPotentiallyFakeNews(News news) { this.potentiallyFakeNews.add(news); }
        public void addSuspiciousNews(News news) { this.suspiciousNews.add(news); }
        public void addUnclassifiedNews(News news) { this.unclassifiedNews.add(news); }
        
        // Getters
        public List<News> getAllNews() { return allNews; }
        public List<News> getTrustedNews() { return trustedNews; }
        public List<News> getPotentiallyFakeNews() { return potentiallyFakeNews; }
        public List<News> getSuspiciousNews() { return suspiciousNews; }
        public List<News> getUnclassifiedNews() { return unclassifiedNews; }
        
        // Statistics
        public int getTotalCount() { return allNews.size(); }
        public int getTrustedCount() { return trustedNews.size(); }
        public int getPotentiallyFakeCount() { return potentiallyFakeNews.size(); }
        public int getSuspiciousCount() { return suspiciousNews.size(); }
        public int getUnclassifiedCount() { return unclassifiedNews.size(); }
        
        public double getTrustedPercentage() {
            return getTotalCount() > 0 ? (double) getTrustedCount() / getTotalCount() * 100 : 0;
        }
        
        public double getSuspiciousPercentage() {
            return getTotalCount() > 0 ? (double) getSuspiciousCount() / getTotalCount() * 100 : 0;
        }
        
        public double getPotentiallyFakePercentage() {
            return getTotalCount() > 0 ? (double) getPotentiallyFakeCount() / getTotalCount() * 100 : 0;
        }
    }
}