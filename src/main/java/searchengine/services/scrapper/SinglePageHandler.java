package searchengine.services.scrapper;

import searchengine.dto.scrapper.ScrapperResponse;

import java.io.IOException;

public interface SinglePageHandler {

    ScrapperResponse indexSinglePage(String url);
}
