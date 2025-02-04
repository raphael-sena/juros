package com.raphaelsena.juros.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_conta")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double valorTotalSemJuros;

    private Double valorTotalComJuros;

    private Double valorPendente;

    private Double valorPago;

    @OneToMany(mappedBy = "conta", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Item> itens = new ArrayList<>();
}
