package hello.springcommunity.dao.post;

import hello.springcommunity.domain.post.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /**
     * 삭제
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from Notice n where n.id = :noticeId")
    void deleteOne(@Param("noticeId") Long id);

}
