package searchengine.dto.scrapper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScrapperResponse {
    private boolean result;
    private Error error;
}
