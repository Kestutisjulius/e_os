package eu.minted.eos.service;

import eu.minted.eos.model.Cart;
import eu.minted.eos.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartCleanupService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductServiceImpl productService;

    @Scheduled(fixedRate = 86400000) // Vykdoma kas 24 valandas (86400000 ms)
    public void removeExpiredCarts() {
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(72);
        List<Cart> expiredCarts = cartRepository.findAllByCreatedDateBefore(expirationTime);

        for (Cart cart : expiredCarts) {
            // Gražiname kiekvieną prekę į sandėlį
            cart.getItems().forEach((product, quantity) -> {
                productService.increaseStock(product.getId(), quantity);
            });

            cartRepository.delete(cart);
        }
    }
}
