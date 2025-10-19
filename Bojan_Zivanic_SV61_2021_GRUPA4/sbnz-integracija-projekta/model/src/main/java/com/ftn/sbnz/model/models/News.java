package com.ftn.sbnz.model.models;

import java.util.Date;

public class News {
    private Long id;
    private Source source;
    private String title;
    private String content;
    private Date publishDate;
    private ConfidenceCategory confidence;
    private String explanation;
    private DistributionType distributionType;

    public News() {
    }

    public News(Long id, Source source, String title, String content, Date publishDate, ConfidenceCategory confidence, String explanation, DistributionType distributionType) {
        this.id = id;
        this.source = source;
        this.title = title;
        this.content = content;
        this.publishDate = publishDate;
        this.confidence = confidence;
        this.explanation = explanation;
        this.distributionType = distributionType;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Source getSource() {
        return source;
    }
    public void setSource(Source source) {
        this.source = source;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Date getPublishDate() {
        return publishDate;
    }
    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }
    public ConfidenceCategory getConfidence() {
        return confidence;
    }
    public void setConfidence(ConfidenceCategory confidence) {
        this.confidence = confidence;
    }
    public String getExplanation() {
        return explanation;
    }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    public DistributionType getDistributionType() {
        return distributionType;
    }
    public void setDistributionType(DistributionType distributionType) {
        this.distributionType = distributionType;
    }

    @Override
    public String toString() {
        return "News{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", confidence=" + confidence +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}