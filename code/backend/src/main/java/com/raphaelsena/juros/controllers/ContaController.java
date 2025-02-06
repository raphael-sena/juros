package com.raphaelsena.juros.controllers;

import com.raphaelsena.juros.models.Conta;
import com.raphaelsena.juros.models.Item;
import com.raphaelsena.juros.models.Pagamento;
import com.raphaelsena.juros.models.dtos.ContaCreateDTO;
import com.raphaelsena.juros.models.dtos.PagamentoDTO;
import com.raphaelsena.juros.services.ContaService;
import com.raphaelsena.juros.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conta")
public class ContaController {

    @Autowired
    private ContaService contaService;

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Conta>> listar() {
        List<Conta> contas = contaService.listar();
        return ResponseEntity.ok(contas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conta> getConta(@PathVariable Long id) {
        Conta conta = contaService.findById(id);
        return ResponseEntity.ok(conta);
    }

    @GetMapping("/{id}/itens")
    public ResponseEntity<List<Item>> getContaItens(@PathVariable Long id) {
        Conta conta = contaService.findById(id);
        List<Item> itens = conta.getItens();
        return ResponseEntity.ok(itens);
    }

    @PostMapping
    public ResponseEntity<Conta> create(@RequestBody ContaCreateDTO obj) {
        Conta conta = contaService.create(obj);
        return ResponseEntity.ok(conta);
    }

    @PostMapping("/pagamentos/{itemId}")
    public ResponseEntity<Pagamento> pagar(@RequestBody PagamentoDTO obj, @PathVariable Long itemId) {
        Item item = itemService.findById(itemId);
        Pagamento pagamento = contaService.efetuarPagamento(item, obj);
        return ResponseEntity.ok(pagamento);
    }

}
