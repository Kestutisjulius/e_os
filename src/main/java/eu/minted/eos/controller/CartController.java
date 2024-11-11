package eu.minted.eos.controller;

import eu.minted.eos.model.Cart;
import eu.minted.eos.model.CartItem;
import eu.minted.eos.model.Product;
import eu.minted.eos.model.User;
import eu.minted.eos.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private UserServiceImpl userService;

    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        // Gauti prisijungusio vartotojo informaciją
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Gauti vartotojo krepšelį
        Optional<Cart> cartOptional = cartService.getCartByUser(user);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            Map<Product, Integer> items = cart.getItems();
            List<CartItem> cartItems = new ArrayList<>();
            BigDecimal totalPrice = BigDecimal.ZERO;

            // Skaičiuojame kiekvieno elemento bendrą kainą ir bendrą krepšelio sumą
            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                Product product = entry.getKey();
                Integer quantity = entry.getValue();
                BigDecimal itemTotalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                cartItems.add(new CartItem(product.getName(), product.getPrice(), quantity, itemTotalPrice, product.getId()));
                totalPrice = totalPrice.add(itemTotalPrice);
            }

            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalPrice", totalPrice);
        } else {
            model.addAttribute("cartItems", Collections.emptyList());
            model.addAttribute("totalPrice", BigDecimal.ZERO);
        }

        // Pridedame krepšelio elementų skaičių į modelį
        int cartItemCount = cartService.getCartItemCount(user);
        model.addAttribute("cartItemCount", cartItemCount);


        return "cart/view";
    }




    @PostMapping("/update")
    public String updateProductQuantity(@RequestParam("productId") Long productId,
                                        @RequestParam("quantity") int quantity,
                                        Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productService.getProductById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        cartService.addProductToCart(user, product, quantity);

        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Map<String, String> quantities, Authentication authentication, RedirectAttributes redirectAttributes) {
        log.info("Gautas produktų kiekis: " + quantities.entrySet());

        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        boolean updatedSuccessfully = false;

        for (Map.Entry<String, String> entry : quantities.entrySet()) {
            try {
                // Panaikiname "quantities[" ir "]"
                String productIdStr = entry.getKey().replaceAll("[^0-9]", "");
                Long productId = Long.parseLong(productIdStr);

                Integer newQuantity = Integer.parseInt(entry.getValue());

                Product product = productService.getProductById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
                cartService.updateProductQuantity(user, product, newQuantity);

                updatedSuccessfully = true; // pažymime kaip sėkmingą, jei nėra klaidų
            } catch (NumberFormatException e) {
                log.error("Klaida konvertuojant produktą arba kiekį: ", e);
                continue; // Jei kyla klaida, pereikite prie kito produkto
            }
        }

        // Jei buvo sėkmingų atnaujinimų, rodome pranešimą
        if (updatedSuccessfully) {
            redirectAttributes.addFlashAttribute("message", "Krepšelis sėkmingai atnaujintas.");
        } else {
            redirectAttributes.addFlashAttribute("message", "Nepavyko atnaujinti krepšelio. Patikrinkite duomenis.");
        }

        // Nukreipiame į krepšelio peržiūros puslapį
        return "redirect:/cart";
    }


    @PostMapping("/add")
    public String addProductToCart(@RequestParam("productId") Long productId,
                                   @RequestParam("quantity") int quantity,
                                   Authentication authentication) {

        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        cartService.addProductToCart(user, product, quantity);

        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeProductFromCart(@RequestParam("productId") Long productId, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productService.getProductById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        // Gauti pašalinamos prekės kiekį iš krepšelio
        int removedQuantity = cartService.getProductQuantity(user, product);

        if (removedQuantity > 0) {
            // Grąžinkite prekę į sandėlį
            product.setQuantity(product.getQuantity() + removedQuantity);
            productService.updateProduct(product); // Atnaujinti produkto informaciją DB

            // Pašalinti prekę iš krepšelio
            cartService.removeProductFromCart(user, product);

            redirectAttributes.addFlashAttribute("message", "Produktas sėkmingai pašalintas iš krepšelio. Kiekis grąžintas į sandėlį.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Nepavyko pašalinti produkto, nes jo kiekis krepšelyje nerastas.");
        }

        return "redirect:/cart";

    }


    @PostMapping("/clear")
    public String clearCart(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        cartService.clearCart(user);

        return "redirect:/cart";
    }

    @PostMapping("/addToCart")
    public String addToCart(@RequestParam("productId") Long productId, @RequestParam("quantity") int quantity, RedirectAttributes redirectAttributes, Authentication authentication) {

        Product product = productService.getProductById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (product.getQuantity() < quantity) {
            redirectAttributes.addFlashAttribute("error", "Nepakankamas prekės likutis.");
            return "redirect:/dashboard/index";
        }

        cartService.addProductToCart(user, product, quantity);

        // Nuskaičiuojame likutį
        product.setQuantity(product.getQuantity() - quantity);
        productService.updateProduct(product);

        redirectAttributes.addFlashAttribute("message", "Prekė sėkmingai įdėta į krepšelį.");
        return "redirect:/dashboard/index";
    }


}
