package com.raphaelsena.juros.models.dtos;

import com.raphaelsena.juros.models.Conta;

public record ContaDTO(
        Long id,
        Double valorTotalSemJuros,
        Double valorTotalComJuros,
        Double valorPendente,
        Double valorPago
) {
    public static ContaDTO fromEntity(Conta conta) {
        return new ContaDTO(
                conta.getId(),
                conta.getValorTotalSemJuros(),
                conta.getValorTotalComJuros(),
                conta.getValorPendente(),
                conta.getValorPago()
        );
    }
}
