package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Table(name = "CancellationPolicies")
@Entity
public class CancellationPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_cancellation_policy;
    private EPolicyType type;
    private Long days_anticipation;
    private Double refund_percentage;
}
