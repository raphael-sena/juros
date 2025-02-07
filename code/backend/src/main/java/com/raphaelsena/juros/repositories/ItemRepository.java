package com.raphaelsena.juros.repositories;

import com.raphaelsena.juros.models.Conta;
import com.raphaelsena.juros.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByConta(Conta conta);
}
