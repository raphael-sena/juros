package com.raphaelsena.juros.services;

import com.raphaelsena.juros.models.Conta;
import com.raphaelsena.juros.models.Item;
import com.raphaelsena.juros.models.dtos.ContaCreateDTO;
import com.raphaelsena.juros.models.enums.Tipo;
import com.raphaelsena.juros.repositories.ContaRepository;
import com.raphaelsena.juros.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Transactional
    public Conta findById(Long id) {
        return contaRepository.findById(id).orElse(null);
    }

    @Transactional
    public List<Conta> listar() {
        return contaRepository.findAll();
    }

    @Transactional
    public Conta create(ContaCreateDTO obj) {
        Conta conta = new Conta();
        conta.setValorTotal(0.0);
        conta.setValorPendente(0.0);
        conta.setValorPago(0.0);
        conta = contaRepository.save(conta);

        Conta contaTemp = conta;
        List<Item> itens = obj.getItens().stream()
                .map(item -> {
                    item.setConta(contaTemp);
                    return itemRepository.save(item);
                }).collect(Collectors.toList());

        conta.setItens(itens);
        conta.setValorTotal(calcularValorTotal(conta.getItens()));

        conta.setValorPago(calcularValorPago(conta));
        conta.setValorPendente(calcularValorPendente(conta));
        return contaRepository.save(conta);
    }

    private Double calcularValorTotal(List<Item> itens) {
        return itens.stream()
                .filter(item -> (item.getTipo() == Tipo.ACRESCIMO))
                .mapToDouble(Item::getValor)
                .sum();
    }

    private Double calcularValorPago(Conta conta) {
        return conta.getItens().stream()
                .filter(item -> item.getTipo() == Tipo.DEDUCAO)
                .mapToDouble(Item::getValor)
                .sum();
    }

    private Double calcularValorPendente(Conta conta) {
        return conta.getValorTotal() - (conta.getValorPago() != null ? conta.getValorPago() : 0.0);
    }
}
