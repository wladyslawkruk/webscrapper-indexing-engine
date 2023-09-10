package searchengine.db;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.jpa.repository.Query;
import searchengine.config.Site;
import searchengine.dto.scrapper.PageResponse;
import searchengine.dto.scrapper.ScrapperResponse;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.scrapper.TempMapService;
import searchengine.sitenode.SiteNode;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DbService {
    void setSiteStatusIndexed(String siteUrl);   //set INDEXED status
    void saveToSiteDb(SiteEntity siteEntity);   //save to Site table
    Optional<SiteEntity> getSiteEntity (SiteNode node);  // get SiteEntity knowing its node
    void updateTime(SiteNode node); // update time
    void updateTime(SiteEntity se);
    void setLastError(String root,String error);  //set last found error
    void savePageWithError(PageResponse pageResponse, SiteEntity siteEntity);  //save page if scrap failure

    void savePageFromDocument(PageResponse pageResponse, SiteEntity siteEntity, TempMapService tempMap);// regular page save
    void savePageFromDocument(PageResponse pageResponse, SiteEntity siteEntity);  //for a single page
    void saveLemmaToDb(LemmaEntity lemmaEntity);  //save lemma
    void saveIndexToDb(IndexEntity indexEntity); //save index
    void saveAllIndexesToDb(Set<IndexEntity> indexMap);
    void saveAllLemmasToDb(Map<String, LemmaEntity> lemmaMap);
    void setFailedAfterCancellation(List<Site> siteList);
    Optional<SiteEntity> getSiteEntityByRootUrl(String root);

    Integer totalFrequencyOfLemma (String sorterLemmas);
    Set<Integer> findPagesForLemma(String lemma,Integer siteId);
    Float getRankOfLemmaByWordAndPage(String lemma,Integer pageId);
   // Float getAbsRankForPage(Integer pageId,Set<String> sortedSetLemmas);
    Optional<PageEntity> getPageEntityByPageId(Integer pageId);
    Optional<SiteEntity> getSiteEntityBySiteId(Integer siteId);
    Integer countSites();
    Integer countSitePages(String root);
    Integer countAllPages();
    Integer countLemmaBySite(String root);
    String getSiteStatus(String root);
    String getLastError(String root);
    LocalDateTime getStatusTime(String root);
    PageEntity getPageEntityByPathAndSiteEntity(String path, SiteEntity se);

    Float getAbsMaxRelevance(List<String> sortedLemmas);








}
