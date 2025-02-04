package com.raphaelsena.juros.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PagamentoDTO {
    private Double valor;
    private LocalDate dataPagamento;
}
