package com.ftn.sbnz.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ftn.sbnz.model.models.DistributionType;
import com.ftn.sbnz.model.models.News;
import com.ftn.sbnz.model.models.Reputation;
import com.ftn.sbnz.model.models.Source;
import com.ftn.sbnz.service.ForwardChainingService.NewsClassificationResult;

@RestController
@RequestMapping("/api/forward-chaining")
@CrossOrigin(origins = "*")
public class ForwardChainingController {

    @Autowired
    private ForwardChainingService forwardChainingService;

    /**
     * Klasifikuje listu vijesti koristeći forward chaining
     */
    @PostMapping("/classify")
    public ResponseEntity<List<News>> classifyNews(@RequestBody List<News> newsList) {
        List<News> classified = forwardChainingService.classifyNews(newsList);
        return ResponseEntity.ok(classified);
    }

    /**
     * Klasifikuje jednu vijest
     */
    @PostMapping("/classify-single")
    public ResponseEntity<News> classifySingleNews(@RequestBody News news) {
        News classified = forwardChainingService.classifySingleNews(news);
        return ResponseEntity.ok(classified);
    }

    /**
     * Analizira vijesti sa statistikama
     */
    @PostMapping("/analyze")
    public ResponseEntity<NewsClassificationResult> analyzeNews(@RequestBody List<News> newsList) {
        NewsClassificationResult result = forwardChainingService.analyzeNews(newsList);
        return ResponseEntity.ok(result);
    }

    /**
     * Demo endpoint sa test podacima
     */
    @GetMapping("/demo")
    public ResponseEntity<NewsClassificationResult> runDemo() {
        // Kreiraj test podatke
        Source bbcSource = new Source("BBC", Reputation.TRUSTED);
        Source fakeSource = new Source("FakeNews24", Reputation.UNTRUSTED);
        Source unknownSource = new Source("RandomBlog", Reputation.UNKNOWN);
        Source tabloidSource = new Source("TabloidiNews", Reputation.UNTRUSTED);

        News news1 = new News();
        news1.setId(1L);
        news1.setTitle("Ekonomska analiza za Q3 2025");
        news1.setContent("Detaljana analiza ekonomskih trendova...");
        news1.setSource(bbcSource);
        news1.setDistributionType(DistributionType.NORMAL);

        News news2 = new News();
        news2.setId(2L);
        news2.setTitle("ŠOK!!! Vlada krije tajne dokumente!!!");
        news2.setContent("Ekskluzivno otkrivanje...");
        news2.setSource(fakeSource);
        news2.setDistributionType(DistributionType.COORDINATED);

        News news3 = new News();
        news3.setId(3L);
        news3.setTitle("Novi trend u modi");
        news3.setContent("Modna analiza...");
        news3.setSource(unknownSource);
        news3.setDistributionType(DistributionType.NORMAL);

        News news4 = new News();
        news4.setId(4L);
        news4.setTitle("Senzacija u Hollywoodu");
        news4.setContent("Skandal sa poznatima...");
        news4.setSource(tabloidSource);
        news4.setDistributionType(DistributionType.NORMAL);

        News news5 = new News();
        news5.setId(5L);
        news5.setTitle("Šokantno otkriće u medicini!!!");
        news5.setContent("Revolucionarno lečenje...");
        news5.setSource(unknownSource);
        news5.setDistributionType(DistributionType.EXPLOSIVE);

        News news6 = new News();
        news6.setId(6L);
        news6.setTitle("Redovna konferencija za novinare");
        news6.setContent("Zvanično saopštenje vlade...");
        news6.setSource(bbcSource);
        news6.setDistributionType(DistributionType.NORMAL);

        List<News> demoNews = Arrays.asList(news1, news2, news3, news4, news5, news6);
        NewsClassificationResult result = forwardChainingService.analyzeNews(demoNews);
        
        return ResponseEntity.ok(result);
    }
}