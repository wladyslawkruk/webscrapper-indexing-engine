package searchengine.dto.scrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SiteResponse {
    private int code;

    private Document document;


    private Exception exception;
}
