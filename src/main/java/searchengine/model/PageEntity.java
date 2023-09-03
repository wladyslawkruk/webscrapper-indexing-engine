package searchengine.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "page",indexes = {

        @Index(columnList = "path",name = "path_id")
})
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_id", nullable = false)
    private int pageId;

    @ManyToOne
    @JoinColumn(name = "site_id",nullable=false)
    private SiteEntity siteEntity;

    @Column(name = "path",nullable = false,columnDefinition = "text")
    private String path;

    @Column(name = "code")
    private int httpResponseCode;

    @Column(name = "content",nullable = false,columnDefinition = "text")
    private String pageContent;


    public PageEntity(SiteEntity siteEntity, String path, int httpResponseCode, String pageContent) {
        this.siteEntity = siteEntity;
        this.path = path;
        this.httpResponseCode = httpResponseCode;
        this.pageContent = pageContent;
    }


}
