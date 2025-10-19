package com.ftn.sbnz.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ftn.sbnz.model.models.SpreadEvent;
import com.ftn.sbnz.model.models.DistributionType;
import com.ftn.sbnz.model.models.News;
import com.ftn.sbnz.model.models.Reputation;
import com.ftn.sbnz.model.models.Source;
import com.ftn.sbnz.service.CEPService.SpreadAnalysisResult;
import com.ftn.sbnz.service.CEPService.SpreadSimulationConfig;

@RestController
@RequestMapping("/api/cep")
@CrossOrigin(origins = "*")
public class CEPController {

    @Autowired
    private CEPService cepService;

    /**
     * Analizira pattern širenja na osnovu liste evenata
     */
    @PostMapping("/analyze-spread")
    public ResponseEntity<SpreadAnalysisResult> analyzeSpread(@RequestBody SpreadAnalysisRequest request) {
        SpreadAnalysisResult result = cepService.analyzeSpreadPattern(request.getEvents(), request.getNewsList());
        return ResponseEntity.ok(result);
    }

    /**
     * Simulira širenje vijesti
     */
    @PostMapping("/simulate-spread")
    public ResponseEntity<SpreadAnalysisResult> simulateSpread(@RequestBody SpreadSimulationRequest request) {
        SpreadAnalysisResult result = cepService.simulateNewsSpread(request.getNews(), request.getConfig());
        return ResponseEntity.ok(result);
    }

    /**
     * Demo endpoint sa kompleksnim scenarijem
     */
    @GetMapping("/demo")
    public ResponseEntity<SpreadAnalysisResult> runDemo() {
        // Kreiraj test vijesti
        Source source1 = new Source("MainSource", Reputation.TRUSTED);
        Source source2 = new Source("SecondarySource", Reputation.UNKNOWN);

        News news1 = new News();
        news1.setId(1L);
        news1.setTitle("Važna politička vijest");
        news1.setSource(source1);

        News news2 = new News();
        news2.setId(2L);
        news2.setTitle("ŠOK!!! Skandal u sportu!!!");
        news2.setSource(source2);

        List<News> newsList = Arrays.asList(news1, news2);

        // Kreiraj spread evente
        List<SpreadEvent> events = new ArrayList<>();
        long currentTime = 0;

        // Normalno širenje za news1
        for (int i = 0; i < 3; i++) {
            currentTime += 5000; // 5 sekundi
            SpreadEvent event = new SpreadEvent((long) i, 1L, (long) (i + 1), "Source" + (i + 1), currentTime);
            events.add(event);
        }

        // Eksplozivno širenje za news2 (mnogo evenata u kratkom vremenu)
        long explosiveStart = currentTime + 1000;
        for (int i = 0; i < 8; i++) {
            explosiveStart += 500; // 0.5 sekunde između
            SpreadEvent event = new SpreadEvent((long) (10 + i), 2L, (long) (10 + i), "FastSource" + i, explosiveStart);
            events.add(event);
        }

        SpreadAnalysisResult result = cepService.analyzeSpreadPattern(events, newsList);
        return ResponseEntity.ok(result);
    }

    /**
     * Demo koordinisanog širenja
     */
    @GetMapping("/demo-coordinated")
    public ResponseEntity<SpreadAnalysisResult> runCoordinatedDemo() {
        // Vijest koja će imati koordinisano širenje
        Source source = new Source("OriginalSource", Reputation.UNKNOWN);
        News news = new News();
        news.setId(1L);
        news.setTitle("Kontroverzna politička izjava");
        news.setSource(source);

        List<News> newsList = List.of(news);

        // Koordinisano širenje - više izvora u istom vremenskom okviru
        List<SpreadEvent> events = new ArrayList<>();
        long baseTime = System.currentTimeMillis();

        // Grupa 1 - svi objavljuju u roku od 2 sekunde
        for (int i = 0; i < 5; i++) {
            SpreadEvent event = new SpreadEvent((long) i, 1L, (long) (i + 1), "CoordSource" + (i + 1), baseTime + (i * 400));
            events.add(event);
        }

        // Kratka pauza
        baseTime += 10000;

        // Grupa 2 - još jedna koordinisana grupa
        for (int i = 0; i < 4; i++) {
            SpreadEvent event = new SpreadEvent((long) (10 + i), 1L, (long) (10 + i), "SecondWave" + i, baseTime + (i * 300));
            events.add(event);
        }

        SpreadAnalysisResult result = cepService.analyzeSpreadPattern(events, newsList);
        return ResponseEntity.ok(result);
    }

    // Helper klase za request-e
    public static class SpreadAnalysisRequest {
        private List<SpreadEvent> events;
        private List<News> newsList;

        public List<SpreadEvent> getEvents() { return events; }
        public void setEvents(List<SpreadEvent> events) { this.events = events; }
        
        public List<News> getNewsList() { return newsList; }
        public void setNewsList(List<News> newsList) { this.newsList = newsList; }
    }

    public static class SpreadSimulationRequest {
        private News news;
        private SpreadSimulationConfig config;

        public News getNews() { return news; }
        public void setNews(News news) { this.news = news; }
        
        public SpreadSimulationConfig getConfig() { return config; }
        public void setConfig(SpreadSimulationConfig config) { this.config = config; }
    }
}