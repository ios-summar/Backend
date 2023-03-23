package com.summar.summar.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "Feed_Scrap")
public class FeedScrap extends BaseTimeEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedScrapSeq;

    @ManyToOne
    @JoinColumn(name="feed_seq", referencedColumnName = "feed_seq")
    @JsonIgnoreProperties(value = { "feedScraps" }, allowSetters = true)
    private Feed feed;

    @ManyToOne
    @JoinColumn(name="user_seq", referencedColumnName = "user_seq")
    @JsonIgnoreProperties(value = { "feedScraps" }, allowSetters = true)
    private User user;

    @Type(type = "yes_no")
    private boolean activated;

    public boolean isActivated() {
        return activated;
    }
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    @Builder
    public FeedScrap(Feed feed, User user, boolean activated) {
        this.feed = feed;
        this.user = user;
        this.activated = activated;
    }
}
