package com.java.slms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class School extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String schoolName;

    private String schoolEmail;

    private String schoolWebsite;

    private String schoolAddress;

    private String schoolContactNumber;
}
