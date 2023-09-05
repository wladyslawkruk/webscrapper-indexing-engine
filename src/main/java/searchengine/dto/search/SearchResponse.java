package searchengine.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import searchengine.dto.scrapper.Error;

@Data
@NoArgsConstructor
public class SearchResponse {


    private boolean result;
    private int count;
    @JsonProperty(value = "data")
    private DataResponse[] dataResponse;

    private String errorText;
    public SearchResponse(boolean result, int count, DataResponse[] dataResponse) {
        this.result = result;
        this.count = count;
        this.dataResponse = dataResponse;
    }

    public SearchResponse(boolean result, String error) {
        this.result = result;
        this.errorText = error;
    }
}
