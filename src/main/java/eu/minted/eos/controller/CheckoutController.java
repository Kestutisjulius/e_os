package eu.minted.eos.controller;

import eu.minted.eos.model.*;
import eu.minted.eos.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private WalletServiceImpl walletService;

    @Autowired
    private TransactionServiceImpl transactionService;

    @Autowired
    EmailService emailService;

    private static final BigDecimal MINIMUM_REQUIRED_BALANCE = BigDecimal.valueOf(10.00); // 10 € dovana
    private static final String SENDER_NAME = "Everything Online Store";


    @GetMapping
    public String showCheckout(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartService.getCartByUser(user).orElseThrow(() -> new RuntimeException("Cart not found"));
        Wallet wallet = walletService.getWalletByUserId(user.getId());

        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cartService.getTotalPrice(user));
        model.addAttribute("walletBalance", wallet.getBalance());
        model.addAttribute("order", new Order());

        return "checkout/checkout";
    }

    @PostMapping
    public String processCheckout(@ModelAttribute Address address, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartService.getCartByUser(user).orElseThrow(() -> new RuntimeException("Cart not found"));
        Wallet wallet = walletService.getWalletByUserId(user.getId());
        BigDecimal totalAmount = BigDecimal.valueOf(cartService.getTotalPrice(user));

        // Patikriname, ar pakanka lėšų (pirkimo suma + 10 €)
        if (wallet.getBalance().compareTo(totalAmount.add(MINIMUM_REQUIRED_BALANCE)) < 0) {
            redirectAttributes.addFlashAttribute("error", "Nepakanka lėšų piniginėje. Reikalinga pirkimo suma ir dar 10 € rezervas.");
            return "redirect:/checkout";
        }

        // Nuskaitymas nuo piniginės
        walletService.withdraw(user.getId(), totalAmount, "Pirkimas už " + totalAmount + " €");

        // Užsakymo kūrimas

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setDeliveryAddress(address);

        // Perkeliame produktus ir jų kiekius iš krepšelio į užsakymą
        Map<Product, Integer> productQuantities = cart.getItems();
        Map<Long, Integer> productQuantitiesWithIds = productQuantities.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getId(),
                        Map.Entry::getValue
                ));
        // Siunčiame el. laiškus pardavėjams
        sendEmailsToSellers(productQuantities);
        log.info(productQuantities.toString());

        order.setProductQuantities(productQuantitiesWithIds);
        order.setTotalAmount(BigDecimal.valueOf(cartService.getTotalPrice(user)));
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.PENDING);

        orderService.createOrder(order);
        transactionService.saveTransaction(new Transaction(wallet, TransactionType.WITHDRAWAL, totalAmount, "pirkimas užsakymui #" + order.getOrderNumber()));
        cartService.clearCart(user);


        redirectAttributes.addFlashAttribute("message", "Apmokėjimas sėkmingai suformuotas!");
        return "redirect:/checkout/success";
    }




    // Ši funkcija siunčia el. laiškus kiekvienam pardavėjui apie užsakymą
    private void sendEmailsToSellers(Map<Product, Integer> productQuantities) {
        Map<User, Map<Product, Integer>> productsBySeller = productQuantities.entrySet()
                .stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().getUser(),
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                ));

        productsBySeller.forEach((seller, sellerProducts) -> {
            if (seller.getEmail() == null || seller.getEmail().isEmpty()) {
                log.warn("Pardavėjas {} neturi galiojančio el. pašto adreso", seller.getUsername());
                return;
            }

            StringBuilder emailContent = new StringBuilder();
            emailContent.append("Sveiki, ").append(seller.getFirstName()).append(",\n\n");
            emailContent.append("Naujas užsakymas buvo pateiktas jūsų parduotuvėje.\n");
            emailContent.append("Užsakymo detalės:\n");

            sellerProducts.forEach((product, quantity) -> {
                emailContent.append("- Prekė: ").append(product.getName())
                        .append(", Kiekis: ").append(quantity)
                        .append(", Kaina: ").append(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                        .append(" EUR\n");
            });

            emailContent.append("\nDėkojame už bendradarbiavimą!\n");

            try {
                emailService.sendEmail(seller.getEmail(), "Naujas užsakymas jūsų parduotuvėje", emailContent.toString());
                log.info("Laiškas sėkmingai išsiųstas pardavėjui {}", seller.getEmail());
            } catch (MailException e) {
                log.error("Nepavyko išsiųsti laiško pardavėjui {}. Klaida: {}", seller.getEmail(), e.getMessage());
            }
        });
    }


    @GetMapping("/success")
    public String showSuccessPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.getUserByUsername(username);
        user.ifPresent(value -> model.addAttribute("user", value));
        return user.isPresent() ? "checkout/success" : "error/404";
    }


}
