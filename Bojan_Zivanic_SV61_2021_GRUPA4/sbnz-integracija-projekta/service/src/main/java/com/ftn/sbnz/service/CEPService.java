package com.ftn.sbnz.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.time.SessionPseudoClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ftn.sbnz.model.models.SpreadEvent;
import com.ftn.sbnz.model.models.DistributionType;
import com.ftn.sbnz.model.models.News;

@Service
public class CEPService {

    private static Logger log = LoggerFactory.getLogger(CEPService.class);

    private final KieContainer kieContainer;

    @Autowired
    public CEPService(KieContainer kieContainer) {
        log.info("Initialising CEPService.");
        this.kieContainer = kieContainer;
    }

    /**
     * Analizira pattern širenja vijesti koristeći CEP
     */
    public SpreadAnalysisResult analyzeSpreadPattern(List<SpreadEvent> events, List<News> newsList) {
        KieSession kieSession = kieContainer.newKieSession("ksession-rules");
        SpreadAnalysisResult result = new SpreadAnalysisResult();
        
        try {
            SessionPseudoClock clock = (SessionPseudoClock) kieSession.getSessionClock();
            
            // Ubaci vijesti u sesiju
            for (News news : newsList) {
                kieSession.insert(news);
                if (news.getSource() != null) {
                    kieSession.insert(news.getSource());
                }
            }
            
            // Ubaci spread evente hronološki
            for (SpreadEvent event : events) {
                clock.advanceTime(event.getTimestamp(), TimeUnit.MILLISECONDS);
                kieSession.insert(event);
                kieSession.fireAllRules();
                
                log.info("CEP - Obrađen event: newsId={}, sourceId={}, time={}ms", 
                        event.getNewsId(), event.getSourceId(), event.getTimestamp());
            }
            
            // Finalno pokretanje pravila
            kieSession.fireAllRules();
            
            // Analiziraj rezultate
            for (News news : newsList) {
                result.addAnalyzedNews(news);
                
                switch (news.getDistributionType()) {
                    case COORDINATED:
                        result.addCoordinatedNews(news);
                        break;
                    case EXPLOSIVE:
                        result.addExplosiveNews(news);
                        break;
                    case NORMAL:
                        result.addNormalNews(news);
                        break;
                }
            }
            
            result.setTotalEvents(events.size());
            
            return result;
            
        } finally {
            kieSession.dispose();
        }
    }

    /**
     * Simulira širenje vijesti sa auto-generisanim eventovima
     */
    public SpreadAnalysisResult simulateNewsSpread(News news, SpreadSimulationConfig config) {
        List<SpreadEvent> events = generateSpreadEvents(news.getId(), config);
        List<News> newsList = List.of(news);
        
        return analyzeSpreadPattern(events, newsList);
    }

    /**
     * Generiše spread evente za simulaciju
     */
    private List<SpreadEvent> generateSpreadEvents(Long newsId, SpreadSimulationConfig config) {
        List<SpreadEvent> events = new ArrayList<>();
        long currentTime = 0;
        
        for (int i = 0; i < config.getEventCount(); i++) {
            currentTime += config.getTimeIntervalMs();
            
            SpreadEvent event = new SpreadEvent();
            event.setId((long) i);
            event.setNewsId(newsId);
            event.setSourceId((long) (i + 1));
            event.setSourceName("Source" + (i + 1));
            event.setTimestamp(currentTime);
            
            events.add(event);
        }
        
        return events;
    }

    // Helper klase
    public static class SpreadAnalysisResult {
        private List<News> analyzedNews = new ArrayList<>();
        private List<News> coordinatedNews = new ArrayList<>();
        private List<News> explosiveNews = new ArrayList<>();
        private List<News> normalNews = new ArrayList<>();
        private int totalEvents;
        
        // Getters and setters
        public List<News> getAnalyzedNews() { return analyzedNews; }
        public void addAnalyzedNews(News news) { this.analyzedNews.add(news); }
        
        public List<News> getCoordinatedNews() { return coordinatedNews; }
        public void addCoordinatedNews(News news) { this.coordinatedNews.add(news); }
        
        public List<News> getExplosiveNews() { return explosiveNews; }
        public void addExplosiveNews(News news) { this.explosiveNews.add(news); }
        
        public List<News> getNormalNews() { return normalNews; }
        public void addNormalNews(News news) { this.normalNews.add(news); }
        
        public int getTotalEvents() { return totalEvents; }
        public void setTotalEvents(int totalEvents) { this.totalEvents = totalEvents; }
        
        public int getCoordinatedCount() { return coordinatedNews.size(); }
        public int getExplosiveCount() { return explosiveNews.size(); }
        public int getNormalCount() { return normalNews.size(); }
    }

    public static class SpreadSimulationConfig {
        private int eventCount = 10;
        private long timeIntervalMs = 1000; // 1 sekunda između eventova
        
        public SpreadSimulationConfig() {}
        
        public SpreadSimulationConfig(int eventCount, long timeIntervalMs) {
            this.eventCount = eventCount;
            this.timeIntervalMs = timeIntervalMs;
        }
        
        public int getEventCount() { return eventCount; }
        public void setEventCount(int eventCount) { this.eventCount = eventCount; }
        
        public long getTimeIntervalMs() { return timeIntervalMs; }
        public void setTimeIntervalMs(long timeIntervalMs) { this.timeIntervalMs = timeIntervalMs; }
    }
}