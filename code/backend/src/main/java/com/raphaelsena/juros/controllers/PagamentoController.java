package com.raphaelsena.juros.controllers;

import com.raphaelsena.juros.models.Item;
import com.raphaelsena.juros.models.Pagamento;
import com.raphaelsena.juros.models.dtos.PagamentoDTO;
import com.raphaelsena.juros.services.ItemService;
import com.raphaelsena.juros.services.PagamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagamento")
public class PagamentoController {

    @Autowired
    ItemService itemService;

    @Autowired
    PagamentoService pagamentoService;

    @PostMapping("/{itemId}")
    public ResponseEntity<Pagamento> pagar(@RequestBody PagamentoDTO obj, @PathVariable Long itemId) {
        Item item = itemService.findById(itemId);
        Pagamento pagamento = pagamentoService.efetuarPagamento(item, obj);
        return ResponseEntity.ok(pagamento);
    }
}
