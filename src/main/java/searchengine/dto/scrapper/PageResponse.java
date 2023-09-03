package searchengine.dto.scrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsoup.nodes.Document;
import searchengine.sitenode.SiteNode;
@Data
@AllArgsConstructor
public class PageResponse {
    private int code;

    private String path;

    private Document document;

    private SiteNode node;

    private Exception exception;
}
