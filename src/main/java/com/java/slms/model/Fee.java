package com.java.slms.model;

import com.java.slms.util.FeeMonth;
import com.java.slms.util.FeeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fee extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double totalAmount;
    private Double amountPaid = 0.0;
    private Double remainingAmount;

    @Enumerated(EnumType.STRING)
    private FeeStatus status;

    private String paymentHistory;

    @Temporal(TemporalType.TIMESTAMP)
    private Date paidOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_pan", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeeMonth month;


}
