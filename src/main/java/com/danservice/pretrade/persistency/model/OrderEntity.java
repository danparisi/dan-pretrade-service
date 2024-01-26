package com.danservice.pretrade.persistency.model;

import com.danservice.pretrade.domain.OrderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Enumerated(STRING)
    private OrderType type;

    @Min(0)
    @NotNull
    private int quantity;

    @Min(0)
    @NotNull
    private BigDecimal price;

    @NotBlank
    private String instrument;

}
