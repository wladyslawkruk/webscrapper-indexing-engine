package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
    Optional<SiteEntity> findByUrl(String url);

    @Query(value = "SELECT COUNT(*) FROM site",nativeQuery = true)
    Integer countSites();

    @Query(value = "SELECT COUNT(*) FROM page p JOIN site s ON s.site_id = p.site_id WHERE s.url = ?1",nativeQuery = true)
    Integer countSitePages(String root);

    @Query(value = "SELECT COUNT(*) FROM page p",nativeQuery = true)
    Integer countAllPages();

    @Query(value = "SELECT site.status FROM site WHERE url = ?1",nativeQuery = true)
    String getSiteStatus(String root);

    @Query(value = "SELECT site.last_error FROM site WHERE url = ?1",nativeQuery = true)
    String getLastError(String root);

    @Query(value = "SELECT site.status_time FROM site WHERE url = ?1",nativeQuery = true)
    LocalDateTime getStatusTime(String root);
}
