package searchengine.sitenode;

import searchengine.dto.scrapper.PageResponse;
import searchengine.services.scrapper.TempMapService;

public interface SiteNodeHandler {
    PageResponse handlePage(SiteNode siteNode, TempMapService tempMap);
    PageResponse handleSinglePage(String url, String root);
}
