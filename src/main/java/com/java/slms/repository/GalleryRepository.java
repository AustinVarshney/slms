package com.java.slms.repository;

import com.java.slms.model.Gallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GalleryRepository extends JpaRepository<Gallery, Long>
{
    List<Gallery> findBySession_Id(Long sessionId);

    @Query("SELECT g FROM Gallery g WHERE g.school.id = :schoolId")
    List<Gallery> findAllBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT g FROM Gallery g WHERE g.id = :id AND g.school.id = :schoolId")
    Optional<Gallery> findByIdAndSchoolId(@Param("id") Long id, @Param("schoolId") Long schoolId);

    @Query("SELECT g FROM Gallery g WHERE g.session.id = :sessionId AND g.school.id = :schoolId")
    List<Gallery> findBySessionIdAndSchoolId(@Param("sessionId") Long sessionId, @Param("schoolId") Long schoolId);

}
