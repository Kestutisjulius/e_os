package eu.minted.eos.service;

import eu.minted.eos.model.Cart;
import eu.minted.eos.model.Product;
import eu.minted.eos.model.User;

import java.util.Optional;

public interface CartService {
    Optional<Cart> getCartByUser(User user);
    Cart addProductToCart(User user, Product product, int quantity);
    Cart updateProductQuantity(User user, Product product, int newQuantity);
    Cart removeProductFromCart(User user, Product product);
    Cart clearCart(User user);
    int getCartItemCount(User user);
    double getTotalPrice(User user);

    int getProductQuantity(User user, Product product);

}
