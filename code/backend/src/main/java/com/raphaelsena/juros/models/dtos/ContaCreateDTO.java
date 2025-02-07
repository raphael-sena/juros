package com.raphaelsena.juros.models.dtos;

import com.raphaelsena.juros.models.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ContaCreateDTO {
    private List<Item> itens;
    private String titulo;
    private String descricao;
}
