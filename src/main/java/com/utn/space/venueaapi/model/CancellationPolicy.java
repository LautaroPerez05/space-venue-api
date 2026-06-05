package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
@Table(name = "cancellationpolicies")
@Entity
@NoArgsConstructor
public class CancellationPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_cancellation_policies; // Se cambió de Long a Integer por exigencias de Hibernate

    @Enumerated(EnumType.STRING)
    private EPolicyType type;

    private Integer days_anticipation;
    private BigDecimal refund_percentage;
}
