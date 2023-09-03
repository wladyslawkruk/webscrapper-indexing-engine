package searchengine.services.search;

import searchengine.dto.search.SearchRequest;
import searchengine.dto.search.SearchResponse;
import searchengine.exception.IncorrectQueryException;

public interface SearchService {
    SearchResponse search(SearchRequest sq) throws IncorrectQueryException;
}
