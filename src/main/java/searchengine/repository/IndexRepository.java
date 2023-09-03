package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import searchengine.model.IndexEntity;

import java.util.Set;

public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {

        @Query(value = "SELECT rank FROM index i JOIN lemma l ON l.lemma_id = i.lemma_id WHERE lemma_word=?1 AND page_id=?2",nativeQuery = true)
        Float getRankOfLemmaByWordAndPage(String lemma, Integer pageId);


}
