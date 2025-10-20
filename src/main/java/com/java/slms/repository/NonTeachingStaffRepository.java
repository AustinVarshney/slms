package com.java.slms.repository;

import com.java.slms.model.NonTeachingStaff;
import com.java.slms.util.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NonTeachingStaffRepository extends JpaRepository<NonTeachingStaff, Long>
{
    boolean existsByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);

    List<NonTeachingStaff> findByStatus(UserStatus userStatus);

    Optional<NonTeachingStaff> findByEmailIgnoreCase(String email);

    List<NonTeachingStaff> findByEmailIn(List<String> emails);

    @Query("SELECT n FROM NonTeachingStaff n " +
            "WHERE LOWER(n.email) = LOWER(:email) " +
            "AND n.school.id = :schoolId " +
            "AND n.status = 'ACTIVE'")
    Optional<NonTeachingStaff> findByEmailIgnoreCaseAndSchoolIdAndStatusActive(
            @Param("email") String email,
            @Param("schoolId") Long schoolId);

    @Query("SELECT nts FROM NonTeachingStaff nts WHERE nts.id = :ntsId AND nts.school.id = :schoolId And nts.status = 'ACTIVE'")
    Optional<NonTeachingStaff> findByNtsIdAndSchoolIdActiveStatus(@Param("ntsId") Long ntsId, @Param("schoolId") Long schoolId);

}
