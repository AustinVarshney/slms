package com.java.slms.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.java.slms.util.FeeMonth;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Session extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    public FeeMonth getStartMonth()
    {
        return FeeMonth.valueOf(this.getStartDate().getMonth().name());
    }


    @OneToMany(mappedBy = "session")
    @JsonManagedReference
    private List<ClassEntity> classes;

}
