package searchengine.dto.search;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class DataResponse {
    private String site;
    private String siteName;
    private String uri;
    private String snippet;
    private float relevance;

    public static int compareByRelevance(DataResponse d1, DataResponse d2){
        return Float.compare(d2.getRelevance(), d1.getRelevance());
    }
}
