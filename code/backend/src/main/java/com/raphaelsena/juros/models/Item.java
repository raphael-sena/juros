package com.raphaelsena.juros.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    private LocalDate dataLimitePagamento;

    private Double valor;

    private Double valorTotal;

    private Double valorPago = 0.0;

    private Double valorPendente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "conta_id")
    @JsonIgnore
    private Conta conta;

    private Long diasAtrasados;

    private boolean pago;

    private Double juros;

    @OneToMany(mappedBy = "item", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Pagamento> pagamentos = new ArrayList<>();
}
