package searchengine.recursiveaction;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.dto.scrapper.PageResponse;
import searchengine.services.scrapper.TempMapService;
import searchengine.sitenode.SiteNode;
import searchengine.sitenode.SiteNodeHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

public class SiteNodeRecursiveAction extends RecursiveAction {
    private final SiteNode node;
    private SiteNodeHandler siteNodeHandler;
    private final TempMapService tempMap;



    public SiteNodeRecursiveAction(SiteNode node, SiteNodeHandler siteNodeHandler, TempMapService tempMap) {
        this.node = node;
        this.siteNodeHandler=siteNodeHandler;
        this.tempMap = tempMap;

    }
    protected void compute() {
        try {
            Thread.sleep((int) ((Math.random() * (4000 - 150)) + 150));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Set<SiteNodeRecursiveAction> taskSet = new HashSet<>();
        PageResponse pageResponse = siteNodeHandler.handlePage(node,tempMap);
        if(pageResponse!=null){
            Elements elements = pageResponse.getDocument().select("body").select("a");
            for(Element a:elements){
                String childUrl = a.absUrl("href");
                if (this.isCorrect(childUrl)) {
                    childUrl = this.stripParams(childUrl);
                    SiteNode nSN = new SiteNode(childUrl);
                    this.node.addChild(nSN);
                }
            }
        }
        for(SiteNode node: node.getChildren()) {
            SiteNodeRecursiveAction task = new SiteNodeRecursiveAction(node,siteNodeHandler,tempMap);
            task.fork();
            taskSet.add(task);
        }
        for(SiteNodeRecursiveAction task:taskSet){
            task.join();
        }
    }

    private String stripParams(String url) {
        return url.replaceAll("\\?.+", "");
    }

    private boolean isCorrect(String url) {
        return (!url.isEmpty() && url.startsWith(node.getRootElement().getUrl()) && !url.contains("#")
                && !url.matches("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|pdf|jpeg|mp4))$)"));
    }
}
