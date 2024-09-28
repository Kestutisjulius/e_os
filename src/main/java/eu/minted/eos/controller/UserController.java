package eu.minted.eos.controller;

import eu.minted.eos.model.Order;
import eu.minted.eos.model.Product;
import eu.minted.eos.model.User;
import eu.minted.eos.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    OrderServiceImpl orderService;


    @GetMapping("/login")
    public String login() {
        return "login/index";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password, Model model) {
        // Čia galite pridėti logiką, pavyzdžiui, patikrinti papildomas vartotojo teises arba apdoroti klaidas
        // Pastaba: Autentifikacijos procesą turėtų valdyti Spring Security, šis metodas turėtų būti naudojamas tik jei tikrai reikalinga papildoma logika

        // Čia grąžiname puslapį, jei prisijungimas nesėkmingas
        // Sėkmingo prisijungimo scenarijus automatiškai nukreipiamas pagal Spring Security konfigūraciją
        model.addAttribute("error", "Invalid username or password");
        return "login/index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "user/register"; // Rodys registracijos formą
    }

    @PostMapping("/register")
    public String createUser(@ModelAttribute("user") @Valid User user, BindingResult result, Model model) {
        // Patikriname, ar formoje yra klaidų
        if (result.hasErrors()) {
            return "user/register"; // Jei yra klaidų, grįžtame į registracijos formą
        }

        // Patikriname, ar toks vartotojas jau egzistuoja
        if (userService.getUserByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Username is already taken");
            return "user/register";
        }

        // Išsaugome naują vartotoją
        userService.registerUser(user);

        // Galite nukreipti vartotoją į prisijungimo puslapį po registracijos
        return "redirect:/login?success";
    }



    @GetMapping("/dashboard/user")
    public String userDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        System.out.println(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("user", user);

            // Retrieve products owned by the user
            List<Product> products = productService.getProductsByUserId(user.getId());
            if(products == null || products.isEmpty()) {
                model.addAttribute("products", Collections.emptyList());
                model.addAttribute("noProductsMessage", "No have added Products Yet");
            }else {
            model.addAttribute("products", products);
            }


            // Retrieve orders, handle if orders are null or empty
            List<Order> orders = orderService.getOrdersByUserId(user.getId());

            if (orders == null || orders.isEmpty()) {
                model.addAttribute("orders", Collections.emptyList());  // Pass an empty list
                model.addAttribute("noOrdersMessage", "You have no orders yet.");
            } else {
                model.addAttribute("orders", orders);
            }

            return "dashboard/user";
        } else {
            return "error/404";  // Handle user not found case
        }
    }






}
