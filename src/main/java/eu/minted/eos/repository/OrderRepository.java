package eu.minted.eos.repository;

import eu.minted.eos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Order findByOrderId(String orderId);

    Order findByOrderNumber(String orderNumber);
}
