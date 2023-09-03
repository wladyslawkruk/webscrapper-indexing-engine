package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.scrapper.Error;
import searchengine.dto.scrapper.ScrapperResponse;
import searchengine.dto.search.DataResponse;
import searchengine.dto.search.SearchRequest;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.exception.IncorrectQueryException;
import searchengine.services.scrapper.ScrapperService;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final ScrapperService scrapperService;
    private final SearchService searchService;

    @Autowired
    public ApiController(StatisticsService statisticsService, ScrapperService scrapperService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.scrapperService = scrapperService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<ScrapperResponse> startIndexing(Model model) {
        return ResponseEntity.ok(scrapperService.indexing());
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<ScrapperResponse> stopIndexing(){
        return ResponseEntity.ok(scrapperService.stop());
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam("query")String query, @RequestParam(value = "site", required = false) String site,
                                                 @RequestParam(value = "offset", required = false) Integer offset
            , @RequestParam(value="limit", required = false) Integer limit) throws IncorrectQueryException {

        return ResponseEntity.ok(searchService.search(new SearchRequest(query,site,offset,limit)));
    }

    @PostMapping("/indexPage")
    public ResponseEntity<ScrapperResponse> indexPage(@RequestParam String url){
        return ResponseEntity.ok(scrapperService.addPage(url));
    }
}
