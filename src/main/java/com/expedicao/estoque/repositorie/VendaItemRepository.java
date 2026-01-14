package com.expedicao.estoque.repositorie;

import org.springframework.data.jpa.repository.JpaRepository;
import com.expedicao.estoque.model.VendaItem;

public interface VendaItemRepository extends JpaRepository<VendaItem, Long> {
}
