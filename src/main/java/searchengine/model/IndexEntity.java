package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "index")
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "index_id", nullable = false)
    private int indexId;

    @ManyToOne
    @JoinColumn(name = "page_id",nullable=false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PageEntity pageEntity;

    @ManyToOne
    @JoinColumn(name = "lemma_id",nullable=false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LemmaEntity lemmaEntity;

    @Column(nullable = false)
    private Float rank;

    public IndexEntity(PageEntity pageEntity, LemmaEntity lemmaEntity, Float rank) {
        this.pageEntity = pageEntity;
        this.lemmaEntity = lemmaEntity;
        this.rank = rank;
    }

}
