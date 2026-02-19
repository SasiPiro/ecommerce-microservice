package com.ecommerce.product.repository;

import com.ecommerce.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Utilizziamo @Modifying per dire a JPA che questa è un'operazione di scrittura (UPDATE),
    // molto più efficiente rispetto a caricare l'intera entità per cambiare un solo numero.
    @Modifying
    @Query("UPDATE Product p SET p.stock = :stock WHERE p.id = :id")
    Integer updateStock(@Param("id") Long id, @Param("stock") Integer stock);

}
