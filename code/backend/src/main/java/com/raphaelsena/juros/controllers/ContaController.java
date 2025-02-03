package com.raphaelsena.juros.controllers;

import com.raphaelsena.juros.models.Conta;
import com.raphaelsena.juros.models.dtos.ContaCreateDTO;
import com.raphaelsena.juros.services.ContaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conta")
public class ContaController {

    @Autowired
    private ContaService contaService;

    @PostMapping
    public ResponseEntity<Conta> create(@RequestBody ContaCreateDTO obj) {
        Conta conta = contaService.create(obj);
        return ResponseEntity.ok(conta);
    }
}
