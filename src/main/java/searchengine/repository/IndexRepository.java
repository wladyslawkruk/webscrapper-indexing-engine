package searchengine.repository;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import searchengine.model.IndexEntity;
import searchengine.model.PageIdRelevanceTuple;

import javax.persistence.Tuple;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {

        @Query(value = "SELECT rank FROM index i JOIN lemma l ON l.lemma_id = i.lemma_id WHERE lemma_word=?1 AND page_id=?2",nativeQuery = true)
        Float getRankOfLemmaByWordAndPage(String lemma, Integer pageId);

        @Query(value ="SELECT MAX(relative_rank) FROM( SELECT page_id,SUM(rank) as relative_rank FROM lemma l JOIN index i ON i.lemma_id = l.lemma_id WHERE page_id IN (SELECT page_id FROM ( SELECT page_id, i.lemma_id FROM lemma l JOIN index i ON i.lemma_id = l.lemma_id WHERE l.lemma_word=:firstEl ) as pages_pool) AND lemma_word IN :lemmas GROUP BY page_id ) AS foo",nativeQuery = true )
        Float getAbsMaxRelevance(@Param("firstEl") String firstElement, @Param("lemmas") List<String> uniqueLemmas);

        @Query(value = "SELECT page_id,abs_rank/:maxAbsRank AS relative_rank FROM ( SELECT page_id, SUM(rank) as abs_rank FROM lemma l JOIN index i ON i.lemma_id = l.lemma_id WHERE page_id IN (SELECT page_id FROM ( SELECT DISTINCT page_id, i.lemma_id FROM lemma l JOIN index i ON i.lemma_id = l.lemma_id WHERE lemma_word =:firstEl) as pages_pool) AND lemma_word IN :lemmas GROUP BY i.page_id ) AS foo ORDER BY relative_rank DESC",nativeQuery = true)
        List<Tuple> getMapOfRelevantPagesWithRanks(@Param("lemmas") List<String> uniqueLemmas, @Param("maxAbsRank") Float maxAbsRank, @Param("firstEl")String firstElement);

}
