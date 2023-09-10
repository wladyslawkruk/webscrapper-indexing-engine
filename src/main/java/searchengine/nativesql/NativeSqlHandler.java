package searchengine.nativesql;

import searchengine.model.PageIdRelevanceTuple;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NativeSqlHandler {
    void eraseDataOfSiteEntity();
    List<PageIdRelevanceTuple> getListOfRelevantPagesSorted(List<String> sortedLemmas, Float maxAbsRank);
    Integer getMaxAbsRank(List<String> sortedLemmas);

}
