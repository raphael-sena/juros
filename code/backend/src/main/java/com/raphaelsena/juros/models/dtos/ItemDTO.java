package com.raphaelsena.juros.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ItemDTO {
    private Double valor;
    private LocalDate limiteDataPagamento;
}
