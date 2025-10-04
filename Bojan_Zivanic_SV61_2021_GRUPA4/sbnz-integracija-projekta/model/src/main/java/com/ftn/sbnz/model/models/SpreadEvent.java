package com.ftn.sbnz.model.models;

import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
public class SpreadEvent {
    private Long id;
    private Long newsId;
    private String sourceName;
    private long timestamp;

    public SpreadEvent() {}

    public SpreadEvent(Long id, Long newsId, String sourceName, long timestamp) {
        this.id = id;
        this.newsId = newsId;
        this.sourceName = sourceName;
        this.timestamp = timestamp;
    }

    // getteri i setteri ostaju isti
    public Long getId() { return id; }
    public Long getNewsId() { return newsId; }
    public String getSourceName() { return sourceName; }
    public long getTimestamp() { return timestamp; }

    public void setId(Long id) { this.id = id; }
    public void setNewsId(Long newsId) { this.newsId = newsId; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}