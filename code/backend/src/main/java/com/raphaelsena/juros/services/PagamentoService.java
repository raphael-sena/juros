package com.raphaelsena.juros.services;

import com.raphaelsena.juros.models.Conta;
import com.raphaelsena.juros.models.Item;
import com.raphaelsena.juros.models.Pagamento;
import com.raphaelsena.juros.models.dtos.PagamentoDTO;
import com.raphaelsena.juros.repositories.ContaRepository;
import com.raphaelsena.juros.repositories.ItemRepository;
import com.raphaelsena.juros.repositories.PagamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagamentoService {

    @Autowired
    PagamentoRepository pagamentoRepository;

    @Autowired
    ContaService contaService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ContaRepository contaRepository;

    @Transactional
    public Pagamento efetuarPagamento(Item item, PagamentoDTO obj) {

        Pagamento pagamento = new Pagamento();
        pagamento.setItem(item);
        pagamento.setValor(obj.getValor());
        pagamento.setDataPagamento(obj.getDataPagamento());
        pagamentoRepository.save(pagamento);

        double novoValorPago = item.getValorPago() + obj.getValor();
        double novoValorPendente = item.getValorTotal() - novoValorPago;

        item.setValorPago(novoValorPago);
        item.setValorPendente(novoValorPendente);
        item.setJuros(contaService.calcularJuros(item));
        item.setDiasAtrasados(contaService.calcularDiasAtrasados(item));

        if (item.getValorPendente() < 1 || item.isPago()) {
            item.setPago(true);
            item.setValorPendente(0.0);
        }

        itemRepository.save(item);

        Conta conta = item.getConta();

        conta.setValorTotalComJuros(0.0);
        for (Item i : conta.getItens()) {
            i.setJuros(contaService.calcularJuros(i));
            i.setDiasAtrasados(contaService.calcularDiasAtrasados(i));

            conta.setValorTotalComJuros(conta.getValorTotalComJuros() + i.getValorTotal());
            conta.setValorPago(contaService.calcularValorPago(conta));
            itemRepository.save(i);
        }

        conta.setValorPendente(contaService.calcularValorPendente(conta));
        contaRepository.save(conta);

        return pagamento;
    }
}
