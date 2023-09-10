package searchengine.services.search;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.db.DbService;
import searchengine.dto.search.DataResponse;
import searchengine.dto.search.SearchRequest;
import searchengine.dto.search.SearchResponse;
import searchengine.exception.IncorrectQueryException;
import searchengine.lemma.LemmaFinder;
import searchengine.model.PageEntity;
import searchengine.model.PageIdRelevanceTuple;
import searchengine.model.SiteEntity;
import searchengine.nativesql.NativeSqlHandler;
import searchengine.repository.IndexRepository;

import javax.persistence.Tuple;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService{

    private final DbService dbService;
    private final NativeSqlHandler nativeSqlHandler;
    private final IndexRepository indexRepository;

    @Autowired
    public SearchServiceImpl(DbService dbService, NativeSqlHandler nativeSqlHandler, IndexRepository indexRepository) {
        this.dbService = dbService;
        this.nativeSqlHandler = nativeSqlHandler;
        this.indexRepository = indexRepository;
    }

    @Override
    public SearchResponse search(SearchRequest sq) throws IncorrectQueryException {
        SearchResponse searchResponse = new SearchResponse();
        SiteEntity se = null;
        if(sq.getSite()!=null){
            if(dbService.getSiteEntityByRootUrl(sq.getSite()).isEmpty()){
                return new SearchResponse(false,"Nie przeprowadzono indeksacji tej strony webowej");
            }
            se = dbService.getSiteEntityByRootUrl(sq.getSite()).get();
        }
        Integer siteId = se==null?0:se.getSiteId();
        System.out.println(siteId);
        Set<String> lemmasFromQuery = null;
        try {
            lemmasFromQuery = LemmaFinder.getInstance().collectLemmas(sq.getQuery()).keySet();  //get lemmas out of user Query
            List<String> uniqueLemmasSorted = getSortedMapOfLemmas(lemmasFromQuery).keySet().stream().toList();
            for(String s:uniqueLemmasSorted){
                System.out.println(s);
            }
            Float maxAbsRank = dbService.getAbsMaxRelevance(uniqueLemmasSorted);
            System.out.println(maxAbsRank);
            List<Tuple> res = indexRepository.getMapOfRelevantPagesWithRanks(uniqueLemmasSorted,maxAbsRank,uniqueLemmasSorted.get(0));
            System.out.println(res.size());
            Map<Integer, Float> relevantePagesAndRanks = res.stream().collect(Collectors.toMap(
                            tuple -> ((Number) tuple.get("page_id")).intValue(),
                            tuple -> ((Number) tuple.get("relative_rank")).floatValue())).entrySet().stream()
                    .sorted(Map.Entry.<Integer, Float>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            List<DataResponse> dataResponses = getDataResponses(relevantePagesAndRanks, se, uniqueLemmasSorted, sq.getOffset(), sq.getLimit())
                    .stream()
                    .sorted(DataResponse::compareByRelevance).toList();
            searchResponse.setResult(true);
            searchResponse.setCount(relevantePagesAndRanks.size());
            searchResponse.setDataResponse(dataResponses.toArray(new DataResponse[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return searchResponse;
    }


    private Map<String,Integer> getSortedMapOfLemmas (Set<String> queryLemmas){
        Map<String,Integer> result = new HashMap<>();
        for(String s:queryLemmas){
            if(dbService.totalFrequencyOfLemma(s)!=null){
                result.put(s,dbService.totalFrequencyOfLemma(s));
            }
        }
        return result.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

    }
    private List<DataResponse> getDataResponses(Map<Integer, Float> relevanceAbsolutPages,
                                                SiteEntity site, List<String> sortedSetLemmas, int offset, int limit) {
        List<DataResponse> responseList = new ArrayList<>();
        int count = -1;
        for (Map.Entry element : relevanceAbsolutPages.entrySet()) {
            count++;
            if (count < offset){
                continue;
            }
            if (count > offset + limit-1){
                break;
            }
            Optional<PageEntity> optPage = dbService.getPageEntityByPageId((int) element.getKey());
            if(optPage.isEmpty()){
                continue;
            }
            PageEntity page = optPage.get();
            String content = page.getPageContent();
            String snippet = getSnippet(content, sortedSetLemmas);
            SiteEntity siteResult = site != null? site : dbService.getSiteEntityBySiteId(page.getSiteEntity().getSiteId()).get();
            DataResponse dataResponse = new DataResponse();
            dataResponse.setSite(siteResult.getUrl());
            dataResponse.setSiteName(siteResult.getName());
            dataResponse.setUri(page.getPath().substring(1));
            dataResponse.setSnippet(snippet);
            dataResponse.setRelevance((float) element.getValue());
            responseList.add(dataResponse);
        }
        return responseList;
    }

    public String getSnippet(String text, List<String> lemmaSet) {
        if (lemmaSet.size() == 0){
            return null;
        }
        String input = text.toLowerCase();
        Map<String, Integer> snippetsOnPage = new HashMap<>();
        int startSearch = 0;
        Set<String> cutWords = new HashSet<>();
        while(true){
            int countMatchesSearchWords = 0;
            int numberFirstWord = input.length();
            for (String word : lemmaSet) {
                String word1 = word.length() < 2? word : word.substring(0, word.length() - 2);
                cutWords.add(word1);
                int number = StringUtils.indexOfIgnoreCase(input, word1, startSearch);
                if (number != -1) {
                    numberFirstWord = numberFirstWord < number ? numberFirstWord : number;
                }
            }
            if (numberFirstWord == input.length()){
                break;
            }
            int stop = input.indexOf("<", numberFirstWord);
            int stopCustom = numberFirstWord + 700;
            stop = stop == -1?
                    (Math.min(stopCustom, input.length()))
                    : (Math.min(stop, (stopCustom)));
            String result = input.substring(numberFirstWord, stop);

            for (String word : cutWords) {
                countMatchesSearchWords += StringUtils.countMatches(result, word);
                result = result.replaceAll(word,"<b>" + word + "</b>");
            }
            snippetsOnPage.put(result, countMatchesSearchWords);
            startSearch = stop;
        }
        return  snippetsOnPage.size() == 0? null : snippetsOnPage.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).orElse(null).getKey();
    }



}
