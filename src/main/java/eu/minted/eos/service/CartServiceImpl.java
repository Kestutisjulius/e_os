package eu.minted.eos.service;

import eu.minted.eos.model.Cart;
import eu.minted.eos.model.Product;
import eu.minted.eos.model.User;
import eu.minted.eos.repository.CartRepository;
import eu.minted.eos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Optional<Cart> getCartByUser(User user) {
        return cartRepository.findByUser(user);
    }

    @Override
    @Transactional
    public Cart addProductToCart(User user, Product product, int quantity) {
        Cart cart = cartRepository.findByUser(user).orElse(new Cart());
        cart.setUser(user);

        // Patikriname, ar krepšelyje jau yra šis produktas
        cart.getItems().entrySet().stream()
                .filter(entry -> entry.getKey().equals(product))
                .findFirst()
                .ifPresentOrElse(
                        existingEntry -> existingEntry.setValue(existingEntry.getValue() + quantity),
                        () -> cart.addProduct(product, quantity)  // Jei nėra, pridedame naują elementą
                );

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart removeProductFromCart(User user, Product product) {
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Krepšelis nerastas"));
        cart.removeProduct(product);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart clearCart(User user) {
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Krepšelis nerastas"));
        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    @Override
    public int getCartItemCount(User user) {
        return cartRepository.findByUser(user)
                .map(Cart::getTotalQuantity)
                .orElse(0);
    }

    @Override
    public double getTotalPrice(User user) {
        return cartRepository.findByUser(user)
                .map(cart -> cart.getTotalPrice().doubleValue())
                .orElse(0.0);
    }

    @Override
    @Transactional
    public Cart updateProductQuantity(User user, Product product, int newQuantity) {
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Cart not found"));

        // Patikriname, ar produktas yra krepšelyje
        if (cart.getItems().containsKey(product)) {
            // Atnaujiname produkto kiekį arba pašaliname produktą, jei kiekis yra mažesnis nei 1
            if (newQuantity > 0) {
                cart.getItems().put(product, newQuantity);
            } else {
                cart.removeProduct(product);
            }
        } else {
            throw new RuntimeException("Product not found in the cart");
        }

        // Išsaugome atnaujintą krepšelį
        return cartRepository.save(cart);
    }

    @Override
    public int getProductQuantity(User user, Product product) {
        Cart cart = getCartByUser(user).orElse(null);

        if (cart != null && cart.getItems().containsKey(product)) {
            return cart.getItems().get(product);
        }

        return 0; // Jei prekės krepšelyje nėra, grąžina 0
    }

}
