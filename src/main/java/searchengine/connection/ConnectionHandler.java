package searchengine.connection;

import searchengine.dto.scrapper.PageResponse;
import searchengine.dto.scrapper.SiteResponse;
import searchengine.model.PageEntity;
import searchengine.sitenode.SiteNode;

public interface ConnectionHandler {
    SiteResponse getSiteDoc(String path);

    PageResponse getDoc(SiteNode node);

    PageResponse getDocFromSingleUrl(String url);
}
