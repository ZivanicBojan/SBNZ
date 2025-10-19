package com.ftn.sbnz.model.models;

import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
public class SpreadEvent {
    private Long id;
    private Long newsId;
    private Long sourceId;
    private String sourceName;
    private long timestamp;

    public SpreadEvent() {}

    public SpreadEvent(Long id, Long newsId, String sourceName, long timestamp) {
        this.id = id;
        this.newsId = newsId;
        this.sourceName = sourceName;
        this.timestamp = timestamp;
    }

    public SpreadEvent(Long id, Long newsId, Long sourceId, String sourceName, long timestamp) {
        this.id = id;
        this.newsId = newsId;
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.timestamp = timestamp;
    }

    // Getters
    public Long getId() { return id; }
    public Long getNewsId() { return newsId; }
    public Long getSourceId() { return sourceId; }
    public String getSourceName() { return sourceName; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setNewsId(Long newsId) { this.newsId = newsId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}