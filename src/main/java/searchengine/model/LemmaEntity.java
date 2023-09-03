package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "lemma")
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lemma_id", nullable = false)
    private int lemmaId;

    @ManyToOne
    @JoinColumn(name = "site_id",nullable=false)
    private SiteEntity siteEntity;

    @Column(name = "lemma_word",nullable = false,columnDefinition = "varchar(255)")
    private String lemma;

    @Column(name = "frequency",nullable = false)
    private int frequency;


    public LemmaEntity(SiteEntity siteEntity, String lemma, int frequency) {
        this.siteEntity = siteEntity;
        this.lemma = lemma;
        this.frequency = frequency;
    }
}
