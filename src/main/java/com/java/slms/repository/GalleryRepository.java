package com.java.slms.repository;

import com.java.slms.model.Gallery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GalleryRepository extends JpaRepository<Gallery, Long>
{
    List<Gallery> findBySession_Id(Long sessionId);

}
