package com.java.slms.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gallery extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    private String imageUrl; // Cloudinary URL
    private String cloudinaryPublicId; // For deletion
    
    @Column(name = "uploaded_by_type")
    private String uploadedByType; // "TEACHER" or "ADMIN"
    
    @Column(name = "uploaded_by_id")
    private Long uploadedById; // ID of teacher or admin
    
    @Column(name = "uploaded_by_name")
    private String uploadedByName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;
}

