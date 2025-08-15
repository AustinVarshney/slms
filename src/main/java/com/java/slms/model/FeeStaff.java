package com.java.slms.model;

import jakarta.persistence.*;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeeStaff extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String department;

    private String qualification;


    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
