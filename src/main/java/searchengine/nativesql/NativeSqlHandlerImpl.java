package searchengine.nativesql;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.repository.SiteRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

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
            // System.out.println(se.getName()+" has been deleted");

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

//    @Override
//    public Float getAbsRankForPage(Integer pageId, String query) {
//        entityManager=entityManagerFactory.createEntityManager();
//        Query sqlquery = entityManager
//                .createNativeQuery("SELECT SUM(rank) FROM index i JOIN lemma l ON l.lemma_id = i.lemma_id WHERE page_id=:id AND lemma_word IN("+query+")");
//        sqlquery.setParameter("id", 1);
//        if(sqlquery.getSingleResult()==null){
//            return 0f;
//        }
//        return ((Number) sqlquery.getSingleResult()).floatValue();
//    }
}
