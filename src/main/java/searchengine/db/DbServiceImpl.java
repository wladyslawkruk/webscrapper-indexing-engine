package searchengine.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.dto.scrapper.PageResponse;
import searchengine.lemma.LemmaFinder;
import searchengine.model.*;
import searchengine.nativesql.NativeSqlHandler;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.scrapper.TempMapService;
import searchengine.sitenode.SiteNode;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DbServiceImpl implements DbService{
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final NativeSqlHandler nativeSqlHandler;


    @Autowired
    public DbServiceImpl(SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, NativeSqlHandler nativeSqlHandler) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;

        this.nativeSqlHandler = nativeSqlHandler;
    }

    @Override
    public void setSiteStatusIndexed(String siteUrl) {
        if(siteRepository.findByUrl(siteUrl).isPresent()){
            SiteEntity se = siteRepository.findByUrl(siteUrl).get();
            se.setStatusTime(LocalDateTime.now());
            se.setStatus(SiteStatus.INDEXED);
            siteRepository.save(se);
        }

    }

    @Override
    public void saveToSiteDb(SiteEntity siteEntity) {
        siteRepository.save(siteEntity);
    }

    @Override
    public Optional<SiteEntity> getSiteEntity(SiteNode node) {
        SiteEntity se= siteRepository.findByUrl(node.getRootElement().getUrl()).orElse(null);
        return Optional.of(se);
    }

    @Override
    public void updateTime(SiteNode node) {
        if(siteRepository.findByUrl(node.getRootElement().getUrl()).isPresent()){
            SiteEntity se = siteRepository.findByUrl(node.getRootElement().getUrl())
                    .get();
            se.setStatusTime(LocalDateTime.now());
            siteRepository.save(se);
        }
    }

    @Override
    public void updateTime(SiteEntity se) {
            se.setStatusTime(LocalDateTime.now());
            siteRepository.save(se);
        }


    @Override
    public void setLastError(String root, String error) {
        if(siteRepository.findByUrl(root).isPresent()){
            SiteEntity se = siteRepository.findByUrl(root).get();
            se.setLastError(error);
            siteRepository.save(se);
        }
    }

    @Override
    public void savePageWithError(PageResponse pageResponse, SiteEntity siteEntity) {
        pageRepository.save(new PageEntity(
                siteEntity,
                pageResponse.getPath().substring(siteEntity.getUrl().length()-1),
                pageResponse.getCode(),
                pageResponse.getException().getMessage()
        ));
    }

    @Override
    public void savePageFromDocument(PageResponse pageResponse, SiteEntity siteEntity, TempMapService tempMap) {
        int responseCode = pageResponse.getCode();
        PageEntity page = new PageEntity(
                siteEntity,
                pageResponse.getPath().substring(siteEntity.getUrl().length()-1),
                responseCode,
                pageResponse.getDocument().toString()
        );
        pageRepository.save(page);
        tempMap.getMapOfLemmas(page,pageResponse.getDocument().getElementsByTag("body").text().replaceAll("<[^>]*>", "")
                ,siteEntity);
    }

    @Override
    public void savePageFromDocument(PageResponse pageResponse, SiteEntity siteEntity) {
        int responseCode = pageResponse.getCode();
        PageEntity page = new PageEntity(
                siteEntity,
                pageResponse.getPath().substring(siteEntity.getUrl().length()-1),
                responseCode,
                pageResponse.getDocument().toString()
        );
        pageRepository.save(page);
        String text = pageResponse.getDocument().getElementsByTag("body").text().replaceAll("<[^>]*>", "");
        try {
            Map<String, Integer> lemmasFrequencyOnPage = LemmaFinder.getInstance().collectLemmas(text);

            for (Map.Entry<String, Integer> entry : lemmasFrequencyOnPage.entrySet()){
                LemmaEntity le = lemmaRepository.findLemmaEntityByLemmaAndSiteEntity(entry.getKey(),siteEntity);
                indexRepository.save(new IndexEntity(
                       page, le,entry.getValue().floatValue()
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public void saveLemmaToDb(LemmaEntity lemmaEntity) {
        lemmaRepository.saveAndFlush(lemmaEntity);
    }

    @Override
    public void saveIndexToDb(IndexEntity indexEntity) {
        indexRepository.saveAndFlush(indexEntity);

    }

    @Override
    public void saveAllIndexesToDb(Set<IndexEntity> indexMap) {
        indexRepository.saveAllAndFlush(indexMap);
    }

    @Override
    public void saveAllLemmasToDb(Map<String, LemmaEntity> lemmaMap) {
        lemmaRepository.saveAllAndFlush(lemmaMap.values());
    }

    @Override
    public void setFailedAfterCancellation(List<Site> siteList) {
        System.out.println("Setting FAILED STATUS");
        for(Site s:siteList){
            SiteEntity se = siteRepository.findByUrl(s.getUrl()).orElse(null);
            if(se==null) return;
            if(se.getStatus().equals(SiteStatus.INDEXING)){
                System.out.println(se.getUrl());
                se.setStatus(SiteStatus.FAILED);
                se.setLastError("DUE TO CANCELLED INDEXING");
                siteRepository.save(se);
            }

        }
    }



    @Override
    public Optional<SiteEntity> getSiteEntityByRootUrl(String root) {
        return siteRepository.findByUrl(root);
    }

    @Override
    public Integer totalFrequencyOfLemma(String sorterLemmas) {
        return lemmaRepository.totalFrequencyOfLemma(sorterLemmas);
    }

    @Override
    public Set<Integer> findPagesForLemma(String lemma, Integer siteId) {
        return siteId == 0?  lemmaRepository.findPagesForLemmaOnAllSites(lemma)
                : lemmaRepository.findPagesForLemmaOnSingleSite(lemma, siteId);
    }

    @Override
    public Float getRankOfLemmaByWordAndPage(String lemma, Integer pageId) {
        return indexRepository.getRankOfLemmaByWordAndPage(lemma,pageId);
    }

    @Override
    public Optional<PageEntity> getPageEntityByPageId(Integer pageId) {
        return pageRepository.findById(pageId);
    }

    @Override
    public Optional<SiteEntity> getSiteEntityBySiteId(Integer siteId) {
        return siteRepository.findById(siteId);
    }

    @Override
    public Integer countSites() {
        return siteRepository.countSites();
    }

    @Override
    public Integer countSitePages(String root) {
        return siteRepository.countSitePages(root);
    }

    @Override
    public Integer countAllPages() {
        return siteRepository.countAllPages();
    }

    @Override
    public Integer countLemmaBySite(String root) {
        return lemmaRepository.countLemmaBySite(root);
    }

    @Override
    public String getSiteStatus(String root) {
        return siteRepository.getSiteStatus(root);
    }

    @Override
    public String getLastError(String root) {
        return siteRepository.getLastError(root);
    }

    @Override
    public LocalDateTime getStatusTime(String root) {
        return siteRepository.getStatusTime(root);
    }

    @Override
    public PageEntity getPageEntityByPathAndSiteEntity(String path, SiteEntity se) {
        return pageRepository.getPageEntityByPathAndSiteEntity(path,se);
    }


//    @Override
//    public Float getAbsRankForPage(Integer pageId, Set<String> sortedSetLemmas) {
//        StringBuilder query = new StringBuilder();
//        for(String s:sortedSetLemmas){
//            query.append(appendQuote(s));
//        }
//        query.deleteCharAt(query.length()-1);
//        System.out.println(query);
//        Float result = nativeSqlHandler.getAbsRankForPage(pageId,query.toString());
//        System.out.println(result);
//        return result==null?0:result;
//    }

    public String appendQuote(String s) {
        return new StringBuilder()
                .append('\'')
                .append(s)
                .append('\'')
                .append(',')
                .toString();
    }


}
