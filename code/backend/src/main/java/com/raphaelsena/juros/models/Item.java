package com.raphaelsena.juros.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.raphaelsena.juros.models.enums.Tipo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_item")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data;

    private Double valor;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    @JsonIgnore
    private Conta conta;

    private Tipo tipo;
}
