package searchengine.services.scrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.connection.ConnectionHandler;
import searchengine.db.DbService;
import searchengine.dto.scrapper.Error;
import searchengine.dto.scrapper.PageResponse;
import searchengine.dto.scrapper.ScrapperResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repository.PageRepository;

import java.util.Optional;

@Service
public class SinglePageHandlerImpl implements SinglePageHandler{


    private final DbService dbService;
    private final ConnectionHandler connectionHandler;
    private final PageRepository pageRepository;


    @Autowired
    public SinglePageHandlerImpl(DbService dbService, ConnectionHandler connectionHandler, PageRepository pageRepository) {
        this.dbService = dbService;
        this.connectionHandler = connectionHandler;
        this.pageRepository = pageRepository;
    }

    @Autowired
    private SitesList sites;

    @Override
    public ScrapperResponse indexSinglePage(String url) {
        for(Site s:sites.getSites()){
            if(url.contains(s.getUrl())){
                runIndex(url,s.getUrl());
                return new ScrapperResponse(true, null);
            }

        }
        return new ScrapperResponse(false, Error.PAGE_DOES_NOT_BELONG_TO_ANY_SITE);
    }

    public void runIndex(String url,String root){
        SiteEntity se = dbService.getSiteEntityByRootUrl(root).get();
        System.out.println(se.getName());
        PageEntity pe = dbService.getPageEntityByPathAndSiteEntity(url.substring(root.length()-1),se);
        System.out.println(pe==null);
        PageResponse pr = connectionHandler.getDocFromSingleUrl(url);
        if(pe!=null){
            System.out.println("Yeahh");
            pageRepository.delete(pe);
        }
        if (pr.getDocument() == null) {
            dbService.savePageWithError(pr,se);
            dbService.setLastError(root,pr.getException().getMessage());
            dbService.updateTime(se);
            return;
        }
        dbService.savePageFromDocument(pr,se);

        dbService.updateTime(se);


        }




}
