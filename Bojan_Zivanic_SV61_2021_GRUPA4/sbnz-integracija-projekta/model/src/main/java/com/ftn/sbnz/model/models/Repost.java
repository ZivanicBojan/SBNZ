package com.ftn.sbnz.model.models;

public class Repost {
    private News repostedNews;  // Ponovljena vijest
    private News originalNews;  // Originalna vijest
    
    public Repost() {}
    
    public Repost(News repostedNews, News originalNews) {
        this.repostedNews = repostedNews;
        this.originalNews = originalNews;
    }
    
    // Getteri i setteri
    public News getRepostedNews() {
        return repostedNews;
    }

    public News getOriginalNews() {
        return originalNews;
    }
    public void setRepostedNews(News repostedNews) {
        this.repostedNews = repostedNews;
    }

    public void setOriginalNews(News originalNews) {
        this.originalNews = originalNews;
    }

    @Override
    public String toString() {
        return "Repost{" +
                "repostedNews=" + repostedNews +
                ", originalNews=" + originalNews +
                '}';
    } 
}