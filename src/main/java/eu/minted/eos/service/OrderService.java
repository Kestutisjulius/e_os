package eu.minted.eos.service;

import eu.minted.eos.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order createOrder(Order order);
    Optional<Order> getOrderById(Long id);
    Optional<Order> getOrderByOrderNumber(String orderId);
    List<Order> getAllOrders();
    Order updateOrder(Order order);
    void deleteOrder(Long id);
}
