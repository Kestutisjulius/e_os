package eu.minted.eos.repository;

import eu.minted.eos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> getOrdersByUserId(Long userId);
    Optional<Order> findByOrderNumber(String orderNumber);
}
