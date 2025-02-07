package com.raphaelsena.juros.services;

import com.raphaelsena.juros.models.Conta;
import com.raphaelsena.juros.models.Item;
import com.raphaelsena.juros.models.Pagamento;
import com.raphaelsena.juros.models.dtos.ContaCreateDTO;
import com.raphaelsena.juros.models.dtos.PagamentoDTO;
import com.raphaelsena.juros.repositories.ContaRepository;
import com.raphaelsena.juros.repositories.ItemRepository;
import com.raphaelsena.juros.repositories.PagamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContaService {

    private static final Double TAXA_JUROS_DIARIA = 0.0016;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

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
        conta.setJuros(0.0);
        conta.setTitulo("");
        conta.setDescricao("");
        conta = contaRepository.save(conta);

        Conta contaTemp = conta;
        List<Item> itens = obj.getItens().stream()
                .map(item -> {
                    item.setConta(contaTemp);
                    item.setJuros(calcularJuros(item));
                    item.setDiasAtrasados(calcularDiasAtrasados(item));
                    item.setValorTotal(item.getValor() + calcularJuros(item));
                    item.setValorPendente(item.getValorTotal() - item.getValorPago());
                    contaTemp.setValorTotalComJuros(contaTemp.getValorTotalComJuros() + item.getValorTotal());
                    return itemRepository.save(item);
                }).collect(Collectors.toList());


        conta.setTitulo(obj.getTitulo());
        conta.setDescricao(obj.getDescricao());
        conta.setItens(itens);
        conta.setValorTotalSemJuros(calcularValorTotalSemJuros(conta.getItens()));
        conta.setValorTotalComJuros(contaTemp.getValorTotalComJuros());
        conta.setValorPendente(calcularValorPendente(conta));
        conta.setValorPago(calcularValorPago(conta));
        conta.setJuros(calcularValorJurosTotal(conta));

        return contaRepository.save(conta);
    }

    public Double calcularValorJurosTotal(Conta conta) {

        return conta.getItens().stream()
                .mapToDouble(Item::getJuros)
                .sum();
    }

    public Double calcularValorTotalSemJuros(List<Item> itens) {

        return itens.stream()
                .mapToDouble(Item::getValor)
                .sum();
    }

    public Double calcularValorPago(Conta conta) {

        return conta.getItens().stream()
                .mapToDouble(Item::getValorPago)
                .sum();
    }

    public double calcularValorPendente(Conta conta) {

        return conta.getItens().stream()
                .mapToDouble(Item::getValorPendente)
                .sum();
    }


    public Double calcularJuros(Item item) {

        if (item.getDataLimitePagamento() == null || item.isPago()) {
            return 0.0;
        }

        LocalDate dataLimite = item.getDataLimitePagamento();
        double totalJuros = 0.0;

        if (item.getPagamentos().isEmpty()) {
            if (LocalDate.now().isAfter(dataLimite)) {
                long diasAtraso = ChronoUnit.DAYS.between(dataLimite, LocalDate.now());
                totalJuros = item.getValor() * (Math.pow(1 + TAXA_JUROS_DIARIA, diasAtraso) - 1);
            }
        } else {
            for (Pagamento pagamento : item.getPagamentos()) {
                if (pagamento.getDataPagamento().isAfter(dataLimite)) {
                    long diasAtraso = ChronoUnit.DAYS.between(dataLimite, pagamento.getDataPagamento());
                    double jurosItem = item.getValor() * (Math.pow(1 + TAXA_JUROS_DIARIA, diasAtraso) - 1);
                    totalJuros += jurosItem;
                }
            }
        }

        return totalJuros;
    }


    public Long calcularDiasAtrasados(Item item) {

        if (item.getDataLimitePagamento() != null || !item.isPago()) {
            assert item.getDataLimitePagamento() != null;
            if (item.getDataLimitePagamento().isBefore(LocalDate.now())) {
                return ChronoUnit.DAYS.between(item.getDataLimitePagamento(), LocalDate.now());
            }
        }
        return 0L;
    }
}
