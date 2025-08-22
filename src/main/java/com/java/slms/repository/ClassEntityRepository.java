package com.java.slms.repository;

import com.java.slms.model.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long>
{
    ClassEntity findByClassNameIgnoreCase(String name);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ClassEntity c WHERE LOWER(c.className) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    Optional<ClassEntity> findByClassNameIgnoreCaseAndSessionId(String className, Long sessionId);

    Optional<ClassEntity> findByIdAndSessionId(Long classId, Long sessionId);

    @Query("SELECT c FROM ClassEntity c " +
            "WHERE c.id = :classId " +
            "AND c.session.id = :sessionId " +
            "AND c.session.active = true")
    Optional<ClassEntity> findByIdAndSessionIdAndActiveSession(@Param("classId") Long classId,
                                                               @Param("sessionId") Long sessionId);

    @Query("SELECT c FROM ClassEntity c " +
            "WHERE LOWER(c.className) = LOWER(:className) " +
            "AND c.session.id = :sessionId " +
            "AND c.session.active = true")
    Optional<ClassEntity> findByClassNameIgnoreCaseAndSessionIdAndActiveSession(@Param("className") String className,
                                                                                @Param("sessionId") Long sessionId);


}
