package com.raphaelsena.juros.services;

import com.raphaelsena.juros.models.Item;
import com.raphaelsena.juros.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public Item findById(Long id) {
        return itemRepository.findById(id).orElse(null);
    }
}
