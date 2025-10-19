package com.ftn.sbnz.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ftn.sbnz.model.models.ConfidenceCategory;
import com.ftn.sbnz.model.models.DistributionType;
import com.ftn.sbnz.model.models.News;
import com.ftn.sbnz.model.models.Reputation;
import com.ftn.sbnz.model.models.Source;
import java.util.Map;

@RestController
@RequestMapping("/api/backward-chaining")
@CrossOrigin(origins = "*")
public class BackwardChainingController {

    @Autowired
    private BackwardChainingService backwardChainingService;

    /**
     * Analizira zašto su vijesti sumnjive
     */
    @PostMapping("/analyze-suspicious")
    public ResponseEntity<List<News>> analyzeSuspicious(@RequestBody List<News> newsList) {
        List<News> suspicious = backwardChainingService.whyIsSuspicious(newsList);
        return ResponseEntity.ok(suspicious);
    }

    /**
     * Analizira zašto su vijesti pouzdane
     */
    @PostMapping(value = "/analyze-trusted", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<News>> analyzeTrusted(@RequestBody List<News> newsList) {
        List<News> trusted = backwardChainingService.whyIsTrusted(newsList);
        return ResponseEntity.ok(trusted);
    }

    /**
     * Pronalazi nepouzdane izvore
     */
    @PostMapping(value = "/find-untrusted-sources", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Source>> findUntrustedSources(@RequestBody List<Source> sources) {
        List<Source> untrusted = backwardChainingService.findUntrustedSources(sources);
        return ResponseEntity.ok(untrusted);
    }

    /**
     * Kompleksna backward chaining analiza
     */
    @PostMapping(value = "/full-analysis", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map<String,Object>> performFullAnalysis(@RequestBody List<News> newsList) {
        Map<String,Object> result = backwardChainingService.performBackwardAnalysis(newsList);
        return ResponseEntity.ok(result);
    }

    /**
     * Test endpoint za frontend - može biti pozvano GET zahtevom
     */
    @GetMapping(value = "/full-analysis", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map<String,Object>> performFullAnalysisGet() {
        return runDemo(); // Koristi demo podatke
    }

    /**
     * Test endpoint sa demo podacima
     */
    @GetMapping(value = "/demo", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map<String,Object>> runDemo() {
        // Demo endpoint sada vraća rezultat iz performBackwardAnalysis kao i full-analysis
    Source trustedSource = new Source("BBC", Reputation.TRUSTED);
    Source untrustedSource = new Source("FakeNews24", Reputation.UNTRUSTED);
    Source unknownSource = new Source("RandomBlog", Reputation.UNKNOWN);

    News news1 = new News();
    news1.setId(1L);
    news1.setTitle("Važne ekonomske vijesti");
    news1.setSource(trustedSource);
    news1.setDistributionType(DistributionType.NORMAL);

    News news2 = new News();
    news2.setId(2L);
    news2.setTitle("ŠOK!!! Ekskluzivno otkrivanje tajni!!!");
    news2.setSource(untrustedSource);
    news2.setDistributionType(DistributionType.COORDINATED);

    News news3 = new News();
    news3.setId(3L);
    news3.setTitle("Senzacija u sportu");
    news3.setSource(unknownSource);
    news3.setDistributionType(DistributionType.NORMAL);

    News news4 = new News();
    news4.setId(4L);
    news4.setTitle("Šokantno otkriće naučnika!!!");
    news4.setSource(untrustedSource);
    news4.setDistributionType(DistributionType.EXPLOSIVE);

    News repostedNews = new News();
    repostedNews.setId(5L);
    repostedNews.setTitle("Prenesena vijest iz pouzdanog izvora");
    repostedNews.setSource(unknownSource);
    repostedNews.setDistributionType(DistributionType.NORMAL);

    List<News> demoNews = Arrays.asList(news1, news2, news3, news4, repostedNews);

    // 1. Forward chaining: klasifikuj demo vijesti
    List<News> classifiedDemoNews = backwardChainingService.whyIsTrusted(demoNews);

    // 2. Backward chaining: analiziraj razloge
    Map<String,Object> result = backwardChainingService.performBackwardAnalysis(classifiedDemoNews);
    result.put("totalAnalyzed", demoNews.size());
    return ResponseEntity.ok(result);
    }
}