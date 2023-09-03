package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.Optional;

public interface PageRepository extends JpaRepository<PageEntity, Integer> {

//       @Query(value = "SELECT * FROM page WHERE path=?1 AND site_id=?2",nativeQuery = true)
//       PageEntity getPageEntityByPathAndSiteId(String path,Integer siteId);

       @Query(value = "DELETE FROM page WHERE path=?1 AND site_id =?2",nativeQuery = true)
       void deleteByPathAndSiteId(String path, Integer siteId);

       PageEntity getPageEntityByPathAndSiteEntity(String path, SiteEntity siteEntity);



}
