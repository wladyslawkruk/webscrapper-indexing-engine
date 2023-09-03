package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchRequest {
    private String query;
    private String site;
    private Integer offset;
    private Integer limit;

    public SearchRequest(String query) {
        this.query = query;
    }
}
