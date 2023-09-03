package searchengine.services.scrapper;

import searchengine.dto.scrapper.ScrapperResponse;

import java.io.IOException;

public interface ScrapperService {
    ScrapperResponse indexing();
    ScrapperResponse stop();
    ScrapperResponse addPage(String url);
}
