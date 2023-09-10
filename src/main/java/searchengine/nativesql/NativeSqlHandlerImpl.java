package searchengine.nativesql;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.PageIdRelevanceTuple;
import searchengine.repository.SiteRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NativeSqlHandlerImpl implements NativeSqlHandler{

    private final EntityManagerFactory entityManagerFactory;

    private final SiteRepository siteRepository;
    private EntityManager entityManager;

    private StringBuilder forLemmaData = new StringBuilder();

    @Autowired
    public NativeSqlHandlerImpl(EntityManagerFactory entityManagerFactory, SiteRepository siteRepository) {
        this.entityManagerFactory = entityManagerFactory;
        this.siteRepository = siteRepository;
    }



    @Override
    public void eraseDataOfSiteEntity() {
        entityManager=entityManagerFactory.createEntityManager();
        Session session = entityManager.unwrap(Session.class);
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            String sql = String.format("TRUNCATE site RESTART IDENTITY CASCADE;TRUNCATE page RESTART IDENTITY CASCADE");
            session.createSQLQuery(sql).executeUpdate();
            tx.commit();
        } catch (HibernateException hex) {
            if (tx != null) {
                tx.rollback();
            } else {
                hex.printStackTrace();
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<PageIdRelevanceTuple> getListOfRelevantPagesSorted(List<String> sortedLemmas, Float maxAbsRank) {
        entityManager=entityManagerFactory.createEntityManager();
        Session session = entityManager.unwrap(Session.class);
        StringBuilder query = new StringBuilder();
        List<Integer> result;
        for(String s:sortedLemmas){
            query.append(appendQuote(s));
        }
        query.deleteCharAt(query.length()-1);
        Transaction tx = null;
        try{
            String hql ="SELECT page_id,abs_rank/:maxAbsRank AS relative_rank FROM (SELECT page_id, SUM(rank) as abs_rank FROM lemma l JOIN index i ON i.lemma_id = l.lemma_id WHERE page_id IN (SELECT page_id FROM ( SELECT DISTINCT page_id, i.lemma_id FROM lemma l JOIN index i ON i.lemma_id = l.lemma_id WHERE lemma_word =:firstEl) as pages_pool) AND lemma_word IN :lemmas GROUP BY i.page_id ) AS foo ORDER BY relative_rank DESC";
            List<PageIdRelevanceTuple> resultList = session.createQuery(hql).setParameter("maxAbsRank",maxAbsRank).setParameter("firstEl",sortedLemmas.get(0)).setParameter("lemmas",query.toString()).stream().toList();
            return resultList;
        } catch (HibernateException hex) {
            if (tx != null) {
                tx.rollback();
            } else {
                hex.printStackTrace();
            }
        } finally {
            session.close();
        }
        return null;
    }

    @Override
    public Integer getMaxAbsRank(List<String> sortedLemmas) {
        return null;
    }

//    @Override
//    public Float getMaxAbsRank(List<String> sortedLemmas) {
//        entityManager=entityManagerFactory.createEntityManager();
//        Session session = entityManager.unwrap(Session.class);
//        StringBuilder query = new StringBuilder();
//        Integer result;
//        for(String s:sortedLemmas){
//            query.append(appendQuote(s));
//        }
//        query.deleteCharAt(query.length()-1);
//        Transaction tx = null;
//        try{
//            Query sqlQuery = session.createQuery("SELECT MAX(relative_rank) FROM( SELECT page_id,SUM(rank) as relative_rank FROM lemma l JOIN index i ON i.lemma_id = l.lemma_id WHERE page_id IN (SELECT page_id FROM ( SELECT page_id, i.lemma_id FROM lemma l JOIN index i ON i.lemma_id = l.lemma_id WHERE lemma_word ='"+sortedLemmas.get(0)+"' ) as pages_pool) AND lemma_word IN ("+query.toString()+") GROUP BY page_id ) AS foo");
//            return (float)sqlQuery.getFirstResult();
//        } catch (HibernateException hex) {
//            if (tx != null) {
//                tx.rollback();
//            } else {
//                hex.printStackTrace();
//            }
//        } finally {
//            session.close();
//        }
//        return null;
//
//    }


    private String appendQuote(String s) {
        return new StringBuilder()
                .append('\'')
                .append(s)
                .append('\'')
                .append(',')
                .toString();
    }
}
