package searchengine.sitenode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.connection.ConnectionHandler;
import searchengine.db.DbService;
import searchengine.dto.scrapper.PageResponse;
import searchengine.model.SiteEntity;
import searchengine.services.scrapper.TempMapService;

@Service
public class SiteNodeHandlerImpl implements SiteNodeHandler{

    private final ConnectionHandler connectionHandler;
    private final DbService dbService;

    @Autowired
    public SiteNodeHandlerImpl(ConnectionHandler connectionHandler, DbService dbService) {
        this.connectionHandler = connectionHandler;
        this.dbService = dbService;
    }

    @Override
    public PageResponse handlePage(SiteNode siteNode, TempMapService tempMap) {
        SiteEntity siteEntity= dbService.getSiteEntity(siteNode).get();
        PageResponse pageResponse = connectionHandler.getDoc(siteNode);
        if (pageResponse.getDocument() == null) {
            dbService.savePageWithError(pageResponse,siteEntity);
            dbService.setLastError(siteNode.getRootElement().getUrl(),pageResponse.getException().getMessage());
            dbService.updateTime(siteNode);
            return null;
        }
        dbService.savePageFromDocument(pageResponse,siteEntity,tempMap);

        dbService.updateTime(siteNode);
        return pageResponse;
    }

    @Override
    public PageResponse handleSinglePage(String url, String root) {
        return null;
    }


}
