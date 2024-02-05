package com.linkshortener.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "links", schema = "public")
public class Link {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private long id;
    @Basic
    @Column(name = "full_link")
    private String fullLink;
    @Basic
    @Column(name = "alias")
    private String alias;
    @Basic
    @Column(name = "views")
    private Long views;
    @Basic
    @Column(name = "date")
    private LocalDateTime date;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public Link() {
        this.date = LocalDateTime.now();
        this.views = 0L;
    }

    public Link(String fullLink, String alias) {
        this.fullLink = fullLink;
        this.alias = alias;
        this.date = LocalDateTime.now();
        this.views = 0L;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFullLink() {
        return fullLink;
    }

    public void setFullLink(String fullLink) {
        this.fullLink = fullLink;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return id == link.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Link{" +
                "fullLink='" + fullLink + '\'' +
                ", alias='" + alias + '\'' +
                ", date='" + date + '\'' +
                ", views='" + views + '\'' +
                ", id=" + id +
                '}';
    }
}
