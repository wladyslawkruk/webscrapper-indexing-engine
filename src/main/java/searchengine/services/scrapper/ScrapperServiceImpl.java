package searchengine.services.scrapper;


import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.connection.ConnectionHandler;
import searchengine.db.DbService;
import searchengine.dto.scrapper.Error;
import searchengine.dto.scrapper.ScrapperResponse;
import searchengine.dto.scrapper.SiteResponse;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;
import searchengine.nativesql.NativeSqlHandler;
import searchengine.recursiveaction.SiteNodeRecursiveAction;
import searchengine.sitenode.SiteNode;
import searchengine.sitenode.SiteNodeHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Service
public class ScrapperServiceImpl implements ScrapperService{

    @Autowired
    private SitesList sites;
    private boolean isIndexingOn =false;
    private Set<Thread> listTread;
    private ForkJoinPool forkJoinPool;
    private DbService dbService;
    private ConnectionHandler connectionHandler;
    private final SiteNodeHandler siteNodeHandler;
    private final NativeSqlHandler nativeSqlHandler;
    private final SinglePageHandler singlePageHandler;

    @Autowired
    public ScrapperServiceImpl(DbService dbService, ConnectionHandler connectionHandler, SiteNodeHandler siteNodeHandler, NativeSqlHandler nativeSqlHandler, SinglePageHandler singlePageHandler) {
        this.dbService = dbService;
        this.connectionHandler = connectionHandler;
        this.siteNodeHandler = siteNodeHandler;
        this.nativeSqlHandler = nativeSqlHandler;
        this.singlePageHandler = singlePageHandler;
        forkJoinPool = new ForkJoinPool();
    }

    @Override
    public ScrapperResponse indexing() {
        long startTime = System.currentTimeMillis();
        if(isIndexingOn){
            return new ScrapperResponse(false, Error.INDEXING_ALREADY_STARTED);
        }
        else{
            start(startTime);
            return new ScrapperResponse(true, null);

        }


    }
    private void start(long startTime){
        isIndexingOn =true;
        listTread = new HashSet<>(0);
        for(Site s: sites.getSites()){
            Runnable r = () -> runIndex(new StringBuilder(s.getUrl()),s.getName());
            Thread thread = new Thread(r,s.getName());
            thread.start();
            listTread.add(thread);
            System.out.println("Thread search - started " + thread.getName());
        }
        listTread.stream().forEach(t -> {
            try {

                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println((System.currentTimeMillis()-startTime)/1000+ " seconds");
        System.out.println("Scan finished!");
        isIndexingOn=false;
    }

    @Override
    public ScrapperResponse stop() {
        if(forkJoinPool.isQuiescent()){
            return new ScrapperResponse(false, Error.INDEXING_HAS_NOT_BEEN_STARTED_YET);
        }

        try {
            isIndexingOn=false;
            forkJoinPool.shutdownNow();
            forkJoinPool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace());

        } finally {
            forkJoinPool.shutdownNow();
            forkJoinPool = new ForkJoinPool();

        }
        dbService.setFailedAfterCancellation(sites.getSites());

        return new ScrapperResponse(true,null);
    }

    @Override
    public ScrapperResponse addPage(String url) {
        return singlePageHandler.indexSinglePage(url);
    }


    private void runIndex(StringBuilder url,String name){
        nativeSqlHandler.eraseDataOfSiteEntity();
        SiteResponse siteResponse =connectionHandler.getSiteDoc(url.toString());
        Document document = siteResponse.getDocument();
        SiteEntity siteEntity = new SiteEntity();       //created a new Site Instance
        if(document==null){
            siteEntity.setStatus(SiteStatus.FAILED);
            siteEntity.setLastError(siteResponse.getException().getMessage() + " -> " + "caused by IO exception");
            siteEntity.setName(name);
            siteEntity.setUrl(url.toString());
            siteEntity.setStatusTime(LocalDateTime.now());
            dbService.saveToSiteDb(siteEntity);
        }
        else{
            siteEntity.setStatus(SiteStatus.INDEXING);
            siteEntity.setName(name);
            siteEntity.setUrl(url.toString());
            siteEntity.setStatusTime(LocalDateTime.now());
            dbService.saveToSiteDb(siteEntity);
            SiteNode root = new SiteNode(url.toString());    //created root sitenode
            TempMapService tempMap = new TempMapService(dbService);
            SiteNodeRecursiveAction snra = new SiteNodeRecursiveAction(root,siteNodeHandler,tempMap);
            forkJoinPool.invoke(snra);
            tempMap.writeMapDownToDb(siteEntity);
            tempMap.writeMapDownToIndexDb();
            dbService.setSiteStatusIndexed(url.toString());
            System.out.println("Site "+siteEntity.getName()+" has been successfully scrapped");
        }
    }

}
