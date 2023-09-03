package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.Set;

public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
   @Query(value = "SELECT SUM(frequency) FROM lemma WHERE lemma_word=?1",nativeQuery = true)
    Integer totalFrequencyOfLemma(String lemmas);

    @Query(value = "SELECT i.page_id FROM index i JOIN page p ON p.page_id=i.page_id JOIN lemma l ON l.lemma_id = i.lemma_id WHERE lemma_word=?1 AND l.site_id=?2", nativeQuery = true)
    Set<Integer> findPagesForLemmaOnSingleSite(String lemma, Integer siteId);
    @Query(value = "SELECT i.page_id FROM index i JOIN page p ON p.page_id=i.page_id JOIN lemma l ON l.lemma_id = i.lemma_id WHERE lemma_word=?1", nativeQuery = true)
    Set<Integer> findPagesForLemmaOnAllSites(String lemma);


    @Query(value = "SELECT COUNT(*) FROM lemma l JOIN site s ON l.site_id = s.site_id WHERE s.url = ?1",nativeQuery = true)
    Integer countLemmaBySite(String root);

    LemmaEntity findLemmaEntityByLemmaAndSiteEntity(String lemma, SiteEntity se);
}
