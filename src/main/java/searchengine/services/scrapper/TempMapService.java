package searchengine.services.scrapper;

import org.springframework.stereotype.Service;
import searchengine.db.DbService;
import searchengine.lemma.LemmaFinder;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TempMapService {
    private final ConcurrentHashMap<String,Integer> lemmaWords = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, LemmaEntity> lemmaMap = new ConcurrentHashMap<>();

    private final Set<IndexEntity> indexMap = ConcurrentHashMap.newKeySet();

    private final DbService dbService;

    public TempMapService(DbService dbService) {
        this.dbService = dbService;
    }

    public void getMapOfLemmas(PageEntity page, String text, SiteEntity siteEntity) {
        try {
            Map<String, Integer> lemmasFrequencyOnPage = LemmaFinder.getInstance().collectLemmas(text);
            for (Map.Entry<String, Integer> entry : lemmasFrequencyOnPage.entrySet()) {
                LemmaEntity le;
                if(lemmaWords.containsKey(entry.getKey())){
                    int frequency = lemmaWords.get(entry.getKey())+1;
                    lemmaWords.put(entry.getKey(),frequency);
                    le = lemmaMap.get(entry.getKey());
                    le.setFrequency(le.getFrequency()+1);
                    lemmaMap.put(entry.getKey(), le);
                }
                else{
                    lemmaWords.put(entry.getKey(), 1);
                    le = new LemmaEntity(siteEntity,entry.getKey(),1);
                    lemmaMap.put(entry.getKey(),le);
                }
                 indexMap.add(new IndexEntity(page,le,entry.getValue().floatValue()));

            }
        }

        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void writeMapDownToDb(SiteEntity siteEntity) {
        dbService.saveAllLemmasToDb(lemmaMap);
    }


    public void writeMapDownToIndexDb() {
        dbService.saveAllIndexesToDb(indexMap);
    }

    public int getLemmaMapSize(){
        return lemmaMap.size();
    }

    public int getIndexMapSize(){
        return indexMap.size();
    }
}
