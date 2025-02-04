package com.raphaelsena.juros.services;

import com.raphaelsena.juros.models.Conta;
import com.raphaelsena.juros.models.Item;
import com.raphaelsena.juros.models.Pagamento;
import com.raphaelsena.juros.models.dtos.ContaCreateDTO;
import com.raphaelsena.juros.models.dtos.ContaDTO;
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

    private static final Double TAXA_JUROS_DIARIA = 0.02;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Transactional
    public Conta findById(Long id) {
        Conta conta = contaRepository.findById(id).orElse(null);

        if (conta != null) {
            double totalJuros = 0.0;

            for (Item item : conta.getItens()) {
                double jurosCalculado = calcularJuros(item);
                item.setJuros(jurosCalculado);
                itemRepository.save(item);
                totalJuros += jurosCalculado;
            }

            conta.setValorTotalComJuros(conta.getValorTotalSemJuros() + totalJuros);
        }

        return conta;
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
                    item.setDiasAtrasados(calcularDiasAtrasados(item));
                    item.setValorTotal(item.getValor() + calcularJuros(item));
                    item.setValorPendente(item.getValorTotal() - item.getValorPago());
                    contaTemp.setValorTotalComJuros(contaTemp.getValorTotalComJuros() + item.getValorTotal());
                    return itemRepository.save(item);
                }).collect(Collectors.toList());


        conta.setItens(itens);
        conta.setValorTotalSemJuros(calcularValorTotalSemJuros(conta.getItens()));
        conta.setValorTotalComJuros(contaTemp.getValorTotalComJuros());
        conta.setValorPendente(calcularValorPendente(conta));
        conta.setValorPago(calcularValorPago(conta));

        return contaRepository.save(conta);
    }

    private Double calcularValorTotalSemJuros(List<Item> itens) {

        return itens.stream()
                .mapToDouble(Item::getValor)
                .sum();
    }

    private Double calcularValorPago(Conta conta) {
        return conta.getItens().stream()
                .filter(item -> !item.getPagamentos().isEmpty())
                .mapToDouble(i -> i.getPagamentos().stream()
                        .mapToDouble(Pagamento::getValor)
                        .sum())
                .sum();
    }

    private Double calcularValorPendente(Conta conta) {
        return conta.getValorTotalComJuros() - (conta.getValorPago() != null ? conta.getValorPago() : 0.0);
    }

    private Double calcularJuros(Item item) {
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
            return ChronoUnit.DAYS.between(item.getDataLimitePagamento(), LocalDate.now());
        }
        return 0L;
    }

    @Transactional
    public Pagamento efetuarPagamento(Item item, PagamentoDTO obj) {
        Pagamento pagamento = new Pagamento();
        pagamento.setItem(item);
        pagamento.setValor(obj.getValor());
        pagamento.setDataPagamento(obj.getDataPagamento());
        pagamentoRepository.save(pagamento);

        double jurosCalculados = calcularJuros(item);
        item.setJuros(jurosCalculados);

        double novoValorPago = item.getValorPago() + obj.getValor();
        double novoValorPendente = item.getValor() - novoValorPago;

        item.setValorPago(novoValorPago);
        item.setValorPendente(novoValorPendente);
        item.setDiasAtrasados(calcularDiasAtrasados(item));

        if (item.getValorPendente() < 1 || item.isPago()) {
            item.setPago(true);
            item.setValorPendente(0.0);
        }

        itemRepository.save(item);

        Conta conta = item.getConta();

        // Atualizar valores da Conta
        for (Item i : conta.getItens()) {
            i.setConta(conta);
            i.setJuros(calcularJuros(i));
            i.setDiasAtrasados(calcularDiasAtrasados(i));
            conta.setValorTotalComJuros(conta.getValorTotalComJuros() + i.getValorTotal());
            contaRepository.save(conta);
            itemRepository.save(i);
        }

        conta.setValorPendente(calcularValorPendente(conta));
        contaRepository.save(conta);

        return pagamento;
    }
}
