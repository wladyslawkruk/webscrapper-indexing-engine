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
import searchengine.model.SiteEntity;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService{

    private final DbService dbService;

    @Autowired
    public SearchServiceImpl(DbService dbService) {
        this.dbService = dbService;
    }

    @Override
    public SearchResponse search(SearchRequest sq) throws IncorrectQueryException {

        if(sq.getSite()==null){
            return new SearchResponse(false);
        }
        SearchResponse searchResponse = new SearchResponse();
        SiteEntity se = null;
        if(sq.getSite()!=null){
            if(dbService.getSiteEntityByRootUrl(sq.getSite()).isEmpty()){
                throw new IncorrectQueryException("Wrong website");
            }
            se = dbService.getSiteEntityByRootUrl(sq.getSite()).get();
        }
        int siteId = sq.getSite()!=null?dbService.getSiteEntityByRootUrl(sq.getSite()).get().getSiteId()
                :0;
        Set<String> lemmasFromQuery = null;
        try {
            lemmasFromQuery = LemmaFinder.getInstance().collectLemmas(sq.getQuery()).keySet();  //get lemmas out of user Query
            Set<String> uniqueLemmasSorted = getSortedMapOfLemmas(lemmasFromQuery).keySet();
            Map<String,Set<Integer>> lemmaMap = getFilteredPagesByRareLemma(uniqueLemmasSorted,siteId);//карта лемма-списки со страницами
            Set<Integer> pageIdFiltered = getDemandedPagesSet(lemmaMap);
            Map<Integer, Float> relevantPages = getPagesWithRelevance(pageIdFiltered,uniqueLemmasSorted);
            for (Map.Entry<Integer, Float> entry : relevantPages.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }

            List<DataResponse> dataResponses = getDataResponses(relevantPages, se, uniqueLemmasSorted, sq.getOffset(), sq.getLimit())
                    .stream()
                    .sorted(DataResponse::compareByRelevance).toList();
            searchResponse.setResult(true);
            searchResponse.setCount(relevantPages.size());
            searchResponse.setDataResponse(dataResponses.toArray(new DataResponse[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return searchResponse;
    }
    private Map<Integer, Float> getPagesWithRelevance(Set<Integer> idPagesFiltered, Set<String> sortedSetLemmas) {
        Map<Integer, Float> relevanceAbsolutPages = new HashMap<>();
        float maxRelevance = 0f;
        for(Integer i:idPagesFiltered){
            float pageRank = 0f;
            for(String s:sortedSetLemmas){
                pageRank+=dbService.getRankOfLemmaByWordAndPage(s,i)==null?0:dbService.getRankOfLemmaByWordAndPage(s,i);
                //получаем rank всех лемм на странице, складываем

            }
            relevanceAbsolutPages.put(i,pageRank);
            maxRelevance = pageRank > maxRelevance ? pageRank : maxRelevance;

        }
        float finalMaxRelevance = maxRelevance;
        return relevanceAbsolutPages.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue() / finalMaxRelevance, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
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
    private Map<String,Set<Integer>> getFilteredPagesByRareLemma(Set<String> sortedSet, int siteId) {
        Map<String,Set<Integer>> result = new HashMap<>();
        Set<Integer> pagesIdFiltered= new HashSet<>();
        Set<Integer> temp = new HashSet<>();
        int count = 0;
        for (String lemma : sortedSet) {
            if(count==0){
                count++;
                Set<Integer> pagesForLemma = dbService.findPagesForLemma(lemma, siteId);
                temp = pagesForLemma;
                result.put(lemma,pagesForLemma);
            }
            else{
                pagesIdFiltered = dbService.findPagesForLemma(lemma,siteId);
                pagesIdFiltered.retainAll(temp);
                result.put(lemma,pagesIdFiltered);
                temp = pagesIdFiltered;
            }
        }
        return result;
    }
    private List<DataResponse> getDataResponses(Map<Integer, Float> relevanceAbsolutPages,
                                                SiteEntity site, Set<String> sortedSetLemmas, int offset, int limit) {
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




    private Set<Integer> getDemandedPagesSet(Map<String,Set<Integer>> result){
        Set<Integer> resultSet = new HashSet<>();
        for(Set<Integer> pages : result.values()){
            resultSet.addAll(pages);
        }
        return resultSet;
    }

    public String getSnippet(String text, Set<String> lemmaSet) {
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
                result = result.replaceAll(word,"<b>" + word + "<b>");
            }
            snippetsOnPage.put(result, countMatchesSearchWords);
            startSearch = stop;
        }
        return  snippetsOnPage.size() == 0? null : snippetsOnPage.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).orElse(null).getKey();
    }



}
