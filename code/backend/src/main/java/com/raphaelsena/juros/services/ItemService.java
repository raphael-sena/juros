package com.raphaelsena.juros.services;

import com.raphaelsena.juros.models.Conta;
import com.raphaelsena.juros.models.Item;
import com.raphaelsena.juros.models.dtos.ItemDTO;
import com.raphaelsena.juros.repositories.ContaRepository;
import com.raphaelsena.juros.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ContaService contaService;

    @Autowired
    private ContaRepository contaRepository;

    public Item findById(Long id) {
        return itemRepository.findById(id).orElse(null);
    }

    @Transactional
    public Item atualizarItem(Conta conta, Item item, ItemDTO obj) {
        item.setConta(conta);
        item.setValor(obj.getValor());
        item.setDataLimitePagamento(obj.getLimiteDataPagamento());

        itemRepository.save(item);

        List<Item> itensAtualizados = itemRepository.findByConta(conta);
        conta.setItens(itensAtualizados);

        item.setJuros(contaService.calcularJuros(item));
        item.setValorTotal(item.getValor() + item.getJuros());
        item.setValorPendente(item.getValorTotal() - item.getValorPago());
        item.setDiasAtrasados(contaService.calcularDiasAtrasados(item));

        itemRepository.save(item);

        conta.setValorTotalSemJuros(contaService.calcularValorTotalSemJuros(conta.getItens()));
        conta.setJuros(contaService.calcularValorJurosTotal(conta));
        conta.setValorTotalComJuros(conta.getValorTotalSemJuros() + conta.getJuros());
        conta.setValorPendente(contaService.calcularValorPendente(conta));

        contaRepository.save(conta);

        return item;
    }
}
