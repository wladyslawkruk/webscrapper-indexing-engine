package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "site")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private int siteId;

    @OneToMany()
    @JoinColumn(name ="site_id")
    private Set<PageEntity> pages;

    @Column(name = "status",nullable = false)
    @Enumerated(EnumType.STRING)
    private SiteStatus status;

    @Column(name = "status_time",nullable = false,columnDefinition = "timestamp without time zone")
    private LocalDateTime statusTime;

    @Column(name = "last_error",columnDefinition = "text")
    private String lastError;

    @Column(name = "url",nullable = false,columnDefinition = "text")
    private String url;

    @Column(name = "name",nullable = false,columnDefinition = "varchar(255)")
    private String name;



    public SiteEntity(SiteStatus status, LocalDateTime statusTime, String lastError, String url, String name) {
        pages = new HashSet<>();
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
    }
}
