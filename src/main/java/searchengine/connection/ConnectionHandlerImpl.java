package searchengine.connection;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.scrapper.PageResponse;
import searchengine.dto.scrapper.SiteResponse;
import searchengine.sitenode.SiteNode;

import java.io.IOException;
import java.net.UnknownHostException;

@Service
public class ConnectionHandlerImpl implements ConnectionHandler{

    @Override
    public SiteResponse getSiteDoc(String path) {
        Document document = null;
        SiteResponse siteResponse = new SiteResponse();
        try{
            document = Jsoup.connect(path).maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36").referrer("http://www.google.com").get();
        }
        catch(HttpStatusException ex){
            siteResponse.setCode(ex.getStatusCode());
            siteResponse.setException(ex);
            return siteResponse;
        }
        catch (IOException e){
            siteResponse.setException(e);
            return siteResponse;
        }
        siteResponse.setDocument(document);
        return siteResponse;
    }

    @Override
    public PageResponse getDoc(SiteNode node) {
        PageResponse pageResponse;
        Document document;
        Connection connection;
        try{
            connection = Jsoup.connect(node.getUrl()).maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                    .referrer("http://www.google.com");
            document = connection.get();
            pageResponse=new PageResponse(
                    connection.response().statusCode(),
                    node.getUrl(),
                    document,
                    node,
                    null
            );
        }
        catch (UnknownHostException ex){
            pageResponse = new PageResponse(
                    ex.hashCode(),
                    node.getUrl(),
                    null,
                    node,
                    ex
            );
        }
        catch (HttpStatusException ex){
            pageResponse = new PageResponse(
                    ex.getStatusCode(),
                    node.getUrl(),
                    null,
                    node,
                    ex
            );
        }
        catch (IOException e) {
            pageResponse = new PageResponse(
                    e.hashCode(),
                    node.getUrl(),
                    null,
                    node,
                    e
            );
        }
        return pageResponse;
    }

    @Override
    public PageResponse getDocFromSingleUrl(String url) {
        PageResponse pageResponse;
        Document document;
        Connection connection;
        try{
            connection = Jsoup.connect(url).maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                    .referrer("http://www.google.com");
            document = connection.get();
            pageResponse=new PageResponse(
                    connection.response().statusCode(),
                    url,
                    document,
                    null,
                    null
            );
        }
        catch (UnknownHostException ex){
            pageResponse = new PageResponse(
                    ex.hashCode(),
                    url,
                    null,
                    null,
                    ex
            );
        }
        catch (HttpStatusException ex){
            pageResponse = new PageResponse(
                    ex.getStatusCode(),
                    url,
                    null,
                    null,
                    ex
            );
        }
        catch (IOException e) {
            pageResponse = new PageResponse(
                    e.hashCode(),
                    url,
                    null,
                    null,
                    e
            );
        }
        return pageResponse;
    }





}

