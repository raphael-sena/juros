package com.raphaelsena.juros.services;

import com.raphaelsena.juros.models.Conta;
import com.raphaelsena.juros.models.Item;
import com.raphaelsena.juros.models.dtos.ContaCreateDTO;
import com.raphaelsena.juros.repositories.ContaRepository;
import com.raphaelsena.juros.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
        conta.setValorTotalSemJuros(0.0);
        conta.setValorTotalComJuros(0.0);
        conta.setValorPendente(0.0);
        conta.setValorPago(0.0);
        conta = contaRepository.save(conta);

        Conta contaTemp = conta;
        List<Item> itens = obj.getItens().stream()
                .map(item -> {
                    item.setConta(contaTemp);
                    item.setJuros(calcularJuros(item));
                    contaTemp.setValorTotalComJuros(contaTemp.getValorTotalComJuros() + item.getJuros());
                    item.setPago(false);
                    return itemRepository.save(item);
                }).collect(Collectors.toList());


        conta.setItens(itens);
        conta.setValorTotalSemJuros(calcularValorTotalSemJuros(conta.getItens()));

        conta.setValorPago(calcularValorPago(conta));
        conta.setValorTotalComJuros(contaTemp.getValorTotalComJuros());
        conta.setValorPendente(calcularValorPendente(conta));

        return contaRepository.save(conta);
    }

    private Double calcularValorTotalSemJuros(List<Item> itens) {

        return itens.stream()
                .mapToDouble(Item::getValor)
                .sum();
    }

    private Double calcularValorPago(Conta conta) {
        return conta.getItens().stream()
                .mapToDouble(Item::getValor)
                .sum();
    }

    private Double calcularValorPendente(Conta conta) {
        return conta.getValorTotalComJuros() - (conta.getValorPago() != null ? conta.getValorPago() : 0.0);
    }

    private static final double TAXA_JUROS_DIARIA = 0.02;

    private Double calcularJuros(Item item) {
        double totalJuros = 0.0;

            if (item.getDataLimitePagamento() != null && item.getDataPagamento() != null) {
                if (item.getDataPagamento().isAfter(item.getDataLimitePagamento())) {
                    long diasAtraso = ChronoUnit.DAYS.between(item.getDataLimitePagamento(), LocalDate.now());

                    double saldoDevedor = item.getValor();

                    // formula juros compostos: M = P * (1 + i)^t
                    double jurosItem = saldoDevedor * (Math.pow(1 + TAXA_JUROS_DIARIA, diasAtraso) - 1);

                    totalJuros += jurosItem;
                }
            }


        return totalJuros;
    }

}
