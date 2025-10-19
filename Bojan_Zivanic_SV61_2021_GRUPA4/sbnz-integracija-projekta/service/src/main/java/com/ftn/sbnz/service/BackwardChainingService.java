package com.ftn.sbnz.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.runtime.rule.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ftn.sbnz.model.models.News;
import com.ftn.sbnz.model.models.Source;
import com.ftn.sbnz.model.models.ConfidenceCategory;
import com.ftn.sbnz.model.models.Reputation;
import com.ftn.sbnz.model.models.DistributionType;

@Service
public class BackwardChainingService {

    private static Logger log = LoggerFactory.getLogger(BackwardChainingService.class);

    private final KieContainer kieContainer;
    // Simple in-memory store of previously analyzed news (id -> News)
    private final java.util.concurrent.ConcurrentMap<Long, News> knownNewsStore = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    public BackwardChainingService(KieContainer kieContainer) {
        log.info("Initialising BackwardChainingService.");
        this.kieContainer = kieContainer;
    }

    // Returns a small set of known/demo news used to compare when client provides a single news
    private List<News> getKnownNews() {
    List<News> demo = new ArrayList<>();

    Source bbc = new Source("BBC", Reputation.TRUSTED);
    Source fake = new Source("FakeNews24", Reputation.UNTRUSTED);
    Source blog = new Source("RandomBlog", Reputation.UNKNOWN);

    News n1 = new News();
    n1.setId(1L);
    n1.setTitle("Važne ekonomske vijesti");
    n1.setSource(bbc);
    n1.setDistributionType(DistributionType.NORMAL);

    News n2 = new News();
    n2.setId(2L);
    n2.setTitle("ŠOK!!! Ekskluzivno otkrivanje tajni!!!");
    n2.setSource(fake);
    n2.setDistributionType(DistributionType.NORMAL);

    News n3 = new News();
    n3.setId(3L);
    n3.setTitle("Senzacija u sportu");
    n3.setSource(blog);
    n3.setDistributionType(DistributionType.NORMAL);

    // Dodaj demo vijest koja je repost pouzdane
    News n4 = new News();
    n4.setId(4L);
    n4.setTitle("Važne ekonomske vijesti"); // isti naslov kao n1
    n4.setSource(blog); // sa bloga, ali je repost BBC
    n4.setDistributionType(DistributionType.NORMAL);

    demo.add(n1);
    demo.add(n2);
    demo.add(n3);
    demo.add(n4);
    return demo;
    }

    // Simplified title similarity: use only normalized Levenshtein distance
    private boolean areTitlesSimilar(String t1, String t2) {
        if (t1 == null || t2 == null) return false;
        // Normalize unicode and remove diacritics
        String n1 = java.text.Normalizer.normalize(t1, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String n2 = java.text.Normalizer.normalize(t2, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        // Remove non-alphanumeric characters to reduce noise
        n1 = n1.replaceAll("[^A-Za-z0-9]+", "").toLowerCase().trim();
        n2 = n2.replaceAll("[^A-Za-z0-9]+", "").toLowerCase().trim();
        if (n1.isEmpty() || n2.isEmpty()) return false;

        int dist = levenshtein(n1, n2);
        int maxLen = Math.max(n1.length(), n2.length());
        double levRatio = 1.0 - ((double) dist / (double) maxLen);
        // Log the ratio to help debugging; keep threshold moderate
        log.info("LEV_RATIO between '{}' and '{}' = {} (dist={}, maxLen={})", t1, t2, String.format("%.3f", levRatio), dist, maxLen);
        double threshold = 0.72;
        return levRatio >= threshold;
    }

    // Simple iterative Levenshtein distance
    private static int levenshtein(String a, String b) {
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;
        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[m];
    }

    /**
     * HIJERARHIJSKI backward chaining - analizira sumnjive vijesti kroz hijerarhiju
     */
    public List<News> whyIsSuspicious(List<News> newsList) {
        KieSession kieSession = kieContainer.newKieSession("bwKsession");
        
        try {
            List<News> suspiciousNews = new ArrayList<>();
            
            // Ubaci sve činjenice u radnu memoriju
            for (News news : newsList) {
                kieSession.insert(news);
                if (news.getSource() != null) {
                    kieSession.insert(news.getSource());
                }
            }
            
            // Dodaj test Repost činjenice za demonstraciju rekurzije
            if (newsList.size() >= 2) {
                News reposted = newsList.get(0);
                News original = newsList.get(1);
                com.ftn.sbnz.model.models.Repost repost = new com.ftn.sbnz.model.models.Repost(reposted, original);
                kieSession.insert(repost);
                log.info("Dodana Repost činjenica: {} -> {}", reposted.getTitle(), original.getTitle());
            }
            
            // BACKWARD CHAINING sa triggerima
            kieSession.insert("analizirajSumnjivost");
            
            // Pokreni pravila
            int rulesFired = kieSession.fireAllRules();
            log.info("Backward Chaining whyIsSuspicious - Pokrenuto {} pravila", rulesFired);
            
            // Analiziraj rezultate
            Collection<?> facts = kieSession.getObjects();
            for (Object fact : facts) {
                if (fact instanceof News) {
                    News news = (News) fact;
                    if (news.getConfidence() != null && 
                        (news.getConfidence() == ConfidenceCategory.SUMNJIVA ||
                         news.getConfidence() == ConfidenceCategory.POTENCIJALNO_LAZNA)) {
                        suspiciousNews.add(news);
                        log.info("Backward Chaining [{}]: {} - {}", 
                                news.getConfidence(), news.getTitle(), news.getExplanation());
                    }
                }
            }
            
            log.info("Backward Chaining - Hijerarhijska analiza: {} sumnjive vijesti od {} ukupno", 
                    suspiciousNews.size(), newsList.size());
            return suspiciousNews;
            
        } finally {
            kieSession.dispose();
        }
    }

    /**
     * Analizira zašto je vijest pouzdana koristeći backward chaining
     */
    public List<News> whyIsTrusted(List<News> newsList) {
        KieSession kieSession = kieContainer.newKieSession("bwKsession");
        
        try {
            List<News> trustedNews = new ArrayList<>();
            
            // Ubaci sve činjenice u radnu memoriju
            for (News news : newsList) {
                kieSession.insert(news);
                if (news.getSource() != null) {
                    kieSession.insert(news.getSource());
                }
            }
            
            // BACKWARD CHAINING sa triggerima
            kieSession.insert("analizirajPouzdanost");
            
            // Pokreni pravila
            int rulesFired = kieSession.fireAllRules();
            log.info("Backward Chaining whyIsTrusted - Pokrenuto {} pravila", rulesFired);
            
            // Analiziraj rezultate
            Collection<?> facts = kieSession.getObjects();
            for (Object fact : facts) {
                if (fact instanceof News) {
                    News news = (News) fact;
                    if (news.getConfidence() != null && 
                        news.getConfidence() == ConfidenceCategory.POUZDANA) {
                        trustedNews.add(news);
                        log.info("Backward Chaining [POUZDANA]: {} - {}", 
                                news.getTitle(), news.getExplanation());
                    }
                }
            }
            
            log.info("Backward Chaining - Hijerarhijska analiza: {} pouzdane vijesti od {} ukupno", 
                    trustedNews.size(), newsList.size());
            return trustedNews;
            
        } finally {
            kieSession.dispose();
        }
    }

    /**
     * Pronalazi sve nepouzdane izvore koristeći backward chaining
     */
    public List<Source> findUntrustedSources(List<Source> sources) {
        KieSession kieSession = kieContainer.newKieSession("bwKsession");
        
        try {
            List<Source> untrustedSources = new ArrayList<>();
            
            // Ubaci sve izvore u radnu memoriju
            for (Source source : sources) {
                kieSession.insert(source);
            }
            
            // BACKWARD CHAINING QUERY - čitaj postojeće činjenice
            QueryResults results = kieSession.getQueryResults("getUntrustedSources", Variable.v);
            
            for (QueryResultsRow row : results) {
                Source foundSource = (Source) row.get("$source");
                // Proveravaj da li je source iz input liste
                for (Source inputSource : sources) {
                    if (inputSource.getName().equals(foundSource.getName())) {
                        untrustedSources.add(foundSource);
                        log.info("Backward Chaining - Nepouzdan izvor: {} ({})", 
                                foundSource.getName(), foundSource.getReputation());
                        break;
                    }
                }
            }
            
            log.info("Backward Chaining - Pronađeno {} nepouzdanih izvora od {} ukupno", 
                    untrustedSources.size(), sources.size());
            return untrustedSources;
            
        } finally {
            kieSession.dispose();
        }
    }

    /**
     * HIJERARHIJSKA backward chaining analiza sa rekurzivnim query-jima
     */
    public java.util.Map<String, Object> performBackwardAnalysis(List<News> newsList) {
            // Dodaj sve vijesti iz ovog requesta u persistentni store
            for (News n : newsList) {
                if (n.getId() != null) {
                    knownNewsStore.put(n.getId(), n);
                }
            }
        KieSession kieSession = kieContainer.newKieSession("bwKsession");
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        try {
            // Kombinuj ulazne vijesti sa poznatim (pohranjenim) vijestima iz in-memory store-a
            List<News> allNews = new ArrayList<>(newsList);
            // Dodaj sve poznate vijesti iz store-a koje nisu već u allNews
            for (News k : knownNewsStore.values()) {
                boolean exists = false;
                for (News n : allNews) {
                    if (n.getId() != null && n.getId().equals(k.getId())) { exists = true; break; }
                }
                if (!exists) allNews.add(k);
            }

            // Ako klijent šalje samo jednu vijest, automatski dodaj demo poznate vijesti (kao u demo endpointu)
            if (newsList.size() == 1) {
                List<News> demo = getKnownNews();
                for (News k : demo) {
                    boolean exists = false;
                    for (News n : allNews) {
                        if (n.getId() != null && n.getId().equals(k.getId())) { exists = true; break; }
                    }
                    if (!exists) {
                        allNews.add(k);
                    }
                }
            }

            // Ubaci sve vijesti i izvore u sesiju
            for (News news : allNews) {
                kieSession.insert(news);
                if (news.getSource() != null) {
                    kieSession.insert(news.getSource());
                }
            }

            // Automatsko prepoznavanje repost-a na osnovu sličnosti naslova
            // Deterministički poredak: uporedi sve parove (i<j) i odluči koji je original a koji repost
            // koristeći informaciju da su vijesti poslane u `newsList` (input) — input vijest će
            // biti tretirana kao repost ako je slična nekoj poznatoj vijesti iz store-a.
            Set<Long> inputIds = new java.util.HashSet<>();
            Map<Long, Integer> inputIndex = new java.util.HashMap<>();
            for (int i = 0; i < newsList.size(); i++) {
                News n = newsList.get(i);
                if (n.getId() != null) {
                    inputIds.add(n.getId());
                    inputIndex.put(n.getId(), i);
                }
            }

            // Keep track of inserted pairs to avoid duplicates
            Set<String> insertedPairs = new java.util.HashSet<>();
            for (int i = 0; i < allNews.size(); i++) {
                for (int j = i + 1; j < allNews.size(); j++) {
                    News a = allNews.get(i);
                    News b = allNews.get(j);
                    // skip identical objects or same id
                    if (a == b) continue;
                    if (a.getId() != null && b.getId() != null && a.getId().equals(b.getId())) continue;
                    boolean similar = areTitlesSimilar(a.getTitle(), b.getTitle());
                    if (similar) {
                        log.info("TITLE-SIMILAR: '{}' <-> '{}' (a.id={}, b.id={})", a.getTitle(), b.getTitle(), a.getId(), b.getId());
                    } else {
                        continue;
                    }

                    // Decide direction: prefer input -> known (input considered repost of known)
                    News reposted = null;
                    News original = null;

                    boolean aIsInput = (a.getId() != null && inputIds.contains(a.getId()));
                    boolean bIsInput = (b.getId() != null && inputIds.contains(b.getId()));

                    if (aIsInput && !bIsInput) {
                        reposted = a; original = b;
                        log.info("Decided direction: input a -> known b => {} reposts {}", reposted.getTitle(), original.getTitle());
                    } else if (bIsInput && !aIsInput) {
                        reposted = b; original = a;
                        log.info("Decided direction: input b -> known a => {} reposts {}", reposted.getTitle(), original.getTitle());
                    } else if (aIsInput && bIsInput) {
                        // both are inputs: use the order they appeared in the client's list
                        Integer ai = inputIndex.getOrDefault(a.getId(), Integer.MAX_VALUE);
                        Integer bi = inputIndex.getOrDefault(b.getId(), Integer.MAX_VALUE);
                        if (ai <= bi) {
                            reposted = b; original = a;
                        } else {
                            reposted = a; original = b;
                        }
                        log.info("Both are inputs; using input order to decide: {} reposts {}", reposted.getTitle(), original.getTitle());
                    } else {
                        // neither are inputs (both known): decide direction deterministically
                        if (a.getId() != null && b.getId() != null) {
                            if (a.getId() < b.getId()) {
                                reposted = b; original = a;
                            } else {
                                reposted = a; original = b;
                            }
                        } else {
                            // fallback: use lexicographic order of titles to decide
                            if (a.getTitle() == null) a.setTitle("");
                            if (b.getTitle() == null) b.setTitle("");
                            if (a.getTitle().compareTo(b.getTitle()) <= 0) {
                                reposted = b; original = a;
                            } else {
                                reposted = a; original = b;
                            }
                        }
                        log.info("Both known items - decided direction: {} reposts {}", reposted.getTitle(), original.getTitle());
                    }

                    // avoid inserting duplicate repost facts (directional)
                    String key = (reposted.getId()!=null?reposted.getId():System.identityHashCode(reposted)) + "->" + (original.getId()!=null?original.getId():System.identityHashCode(original));
                    if (insertedPairs.contains(key)) continue;
                    insertedPairs.add(key);

                    com.ftn.sbnz.model.models.Repost auto = new com.ftn.sbnz.model.models.Repost(reposted, original);
                    kieSession.insert(auto);
                    log.info("[AUTO-REPOST] '{}' -> '{}' (naslov sličan)", reposted.getTitle(), original.getTitle());
                }
            }
            
            // BACKWARD CHAINING - pokreni različita pravila koja testiraju ISTI rekurzivni query
            log.info("=== POKRETANJE BACKWARD CHAINING SA REKURZIVNIM QUERY-JEM ===");
            
            // Triggeri za testiranje rekurzivnog query-ja na različite načine
            kieSession.insert("test1"); // Test rekurzivnog query-ja za konkretnu vijest
            kieSession.insert("test2"); // Pronalazi SVE vijesti sa pouzdanim lancem
            kieSession.insert("test3"); // Prikazuje sve repost lance
            kieSession.insert("test4"); // Kombinuje rekurzivni query sa drugim uslovima
            kieSession.insert("test5"); // Pronalazi vijesti bez pouzdanog lanca
            
            // Standardna analiza
            kieSession.insert("analizirajPouzdanost");
            kieSession.insert("analizirajSumnjivost");
            
            // DEBUG: Ispiši sve činjenice koje su trenutno u sesiji da verifikujemo da li su
            // sve potrebne činjenice (News, Source, Repost) ubačene u ISTU KieSession
            log.info("--- SESIJA FACTS DUMP (pre fireAllRules) ---");
            Collection<?> allFactsBefore = kieSession.getObjects();
            log.info("Ukupno činjenica u sesiji: {}", allFactsBefore.size());
            for (Object f : allFactsBefore) {
                if (f instanceof News) {
                    News nf = (News) f;
                    log.info("FACT: News id={} title='{}' confidence={} distribution={} explanation={}", nf.getId(), nf.getTitle(), nf.getConfidence(), nf.getDistributionType(), nf.getExplanation());
                } else if (f instanceof Source) {
                    Source sf = (Source) f;
                    log.info("FACT: Source name='{}' reputation={}", sf.getName(), sf.getReputation());
                } else if (f instanceof com.ftn.sbnz.model.models.Repost) {
                    com.ftn.sbnz.model.models.Repost r = (com.ftn.sbnz.model.models.Repost) f;
                    log.info("FACT: Repost -> reposted='{}' original='{}'", r.getRepostedNews()!=null?r.getRepostedNews().getTitle():"<null>", r.getOriginalNews()!=null?r.getOriginalNews().getTitle():"<null>");
                } else {
                    log.info("FACT: {}", f.getClass().getSimpleName());
                }
            }
            log.info("--- KRAJ FACTS DUMP ---");

            // Pokreni pravila - sada će se aktivirati kroz triggere i query-jeve
            int rulesFired = kieSession.fireAllRules();
            log.info("Backward Chaining HIJERARHIJSKI - Pokrenuto {} pravila sa triggerima", rulesFired);
            

            // Analiziraj rezultate nakon što su se pravila izvršila
            Collection<?> facts = kieSession.getObjects();
            News inputNews = null;
            News originalTrustedNews = null;
            // Pronađi input vijest (prva iz newsList)
            if (!newsList.isEmpty()) {
                Long inputId = newsList.get(0).getId();
                for (Object fact : facts) {
                    if (fact instanceof News) {
                        News news = (News) fact;
                        if (news.getId() != null && news.getId().equals(inputId)) {
                            inputNews = news;
                        }
                    }
                }
            }
            // Pronađi originalnu trusted vijest kroz repost lanac koristeći Drools query
            if (inputNews != null) {
                // Pokreni query nad KieSession
                org.kie.api.runtime.rule.QueryResults queryResults = kieSession.getQueryResults("isSourceTrustedThroughChain", inputNews, Variable.v);
                for (org.kie.api.runtime.rule.QueryResultsRow row : queryResults) {
                    Object sourceObj = row.get("izvor");
                    if (sourceObj instanceof Source) {
                        Source src = (Source) sourceObj;
                        if (src.getReputation() == Reputation.TRUSTED) {
                            // Pronađi News sa ovim izvorom i istim naslovom
                            for (Object fact : facts) {
                                if (fact instanceof News) {
                                    News news = (News) fact;
                                    if (news.getSource() != null && news.getSource().getReputation() == Reputation.TRUSTED &&
                                        news.getTitle() != null && areTitlesSimilar(inputNews.getTitle(), news.getTitle())) {
                                        originalTrustedNews = news;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (originalTrustedNews != null) break;
                }
            }
            result.put("inputNews", inputNews);
            if (originalTrustedNews != null) {
                result.put("originalTrustedNews", originalTrustedNews);
            }
            // Dodaj totalAnalyzed u rezultat
            result.put("totalAnalyzed", allNews.size());
            
            // Pronađi nepouzdane izvore
            Collection<Source> uniqueSources = new HashSet<>();
            for (News news : newsList) {
                if (news.getSource() != null) {
                    uniqueSources.add(news.getSource());
                }
            }
            
            for (Source source : uniqueSources) {
                if (source.getReputation() != Reputation.TRUSTED) {
                    ((java.util.List<Source>) result.computeIfAbsent("untrustedSources", k -> new java.util.ArrayList<Source>())).add(source);
                    log.info("Backward Chaining - Nepouzdan izvor: {} ({})", 
                            source.getName(), source.getReputation());
                }
            }
                // Pronađi pouzdane izvore
                for (Source source : uniqueSources) {
                    if (source.getReputation() == Reputation.TRUSTED) {
                        ((java.util.List<Source>) result.computeIfAbsent("trustedSources", k -> new java.util.ArrayList<Source>())).add(source);
                        log.info("Backward Chaining - Pouzdan izvor: {} ({})", 
                                source.getName(), source.getReputation());
                    }
                }
            return result;
            
        } finally {
            kieSession.dispose();
        }
    }
    // NewsAnalysisResult is now a top-level DTO in `com.ftn.sbnz.service.NewsAnalysisResult`
}