package eu.minted.eos.controller;

import eu.minted.eos.model.*;
import eu.minted.eos.service.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;

@Slf4j
@Controller
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    CartServiceImpl cartService;

    @Autowired
    WalletServiceImpl walletService;


    @GetMapping("/login")
    public String login(@RequestParam(value = "success", required = false) String success,
                        @RequestParam(value = "error", required = false) String error,
                        Model model) {
        if (success != null) {
            model.addAttribute("success", "Successfully registered! Please log in.");
        }
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        return "login/index";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password, Model model) {
                model.addAttribute("error", "Invalid username or password");
        return "login/index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("address", new Address());
        return "user/register"; // Rodys registracijos formą
    }

    @PostMapping("/register")
    public String createUser(@ModelAttribute("user") @Valid User user,
                             @RequestParam(value = "photo", required = false) MultipartFile photo,
                             BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "user/register"; // Jei yra klaidų, grįžtame į registracijos formą
        }

        // Patikriname, ar vartotojo vardas jau naudojamas
        if (userService.getUserByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Username is already taken.");
            return "user/register";
        }

        // Tvarkome nuotrauką, jei ji pateikta
        if (photo != null && !photo.isEmpty()) {
            try {
                user.setPhoto(photo.getBytes());
            } catch (IOException e) {
                model.addAttribute("error", "Failed to upload photo.");
                return "user/register";
            }
        }

        // Išsaugome vartotoją
        userService.registerUser(user, photo);

        // Sukuriame vartotojui piniginę
        walletService.createWalletForUser(user.getId());

        // Pridėsime sėkmės pranešimą po registracijos
        redirectAttributes.addAttribute("success", "Successfully registered! Please log in.");
        return "redirect:/login";
    }


    @PostMapping("/updateProfile")
    public String updateProfile(@RequestParam("firstname") String firstName,
                                @RequestParam("lastname") String lastName,
                                @RequestParam("username") String username,
                                @RequestParam("email") String email,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                @RequestParam(value = "photo", required = false) MultipartFile photo,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        // Gauti dabartinį vartotoją iš autentifikacijos objekto
        String currentUsername = authentication.getName();
        log.info("current username: " + currentUsername);
        User currentUser = userService.getUserByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));
        log.info("current user: " + currentUser);

        // Atnaujinti vartotojo duomenis (jei laukai nėra tušti)
        if (!firstName.isEmpty()) {
            currentUser.setFirstName(firstName);
        }
        if (!lastName.isEmpty()) {
            currentUser.setLastName(lastName);
        }
        if (!username.isEmpty()) {
            currentUser.setUsername(username);
        }
        if (!email.isEmpty()) {
            currentUser.setEmail(email);
        }

        // Tikriname ar naujas slaptažodis ir patvirtintas slaptažodis sutampa
        if (!password.isEmpty() && password.equals(confirmPassword)) {
            currentUser.setPassword(password);
        } else if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Slaptažodžiai nesutampa.");
            return "dashboard/user";  // Grąžina į profilį su klaidos pranešimu
        }

        if (photo != null && !photo.isEmpty()) {
            try {
                currentUser.setPhoto(photo.getBytes());
            } catch (IOException e) {
                log.error("Klaida įkeliant paveikslėlį", e);
                redirectAttributes.addFlashAttribute("errorMessage", "Nepavyko įkelti paveikslėlio.");
                return "redirect:/dashboard/user";
            }
        }

        // Atnaujinti vartotojo duomenis duomenų bazėje
        userService.updateUser(currentUser);

        // Pridėti patvirtinimo pranešimą modelyje
        model.addAttribute("successMessage", "Profilis sėkmingai atnaujintas!");

        return "redirect:/dashboard/user";
    }



    @GetMapping("/dashboard/user")
    public String userDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("user", user);

            // Konvertuojame nuotrauką į Base64, jei ji egzistuoja
            if (user.getPhoto() != null) {
                String base64Photo = Base64.getEncoder().encodeToString(user.getPhoto());
                model.addAttribute("base64Photo", base64Photo);
            } else {
                model.addAttribute("base64Photo", null);
            }

            // Pridedame krepšelio elementų skaičių į modelį
            int cartItemCount = cartService.getCartItemCount(user);
            model.addAttribute("cartItemCount", cartItemCount);

            // Patikriname piniginę
            Wallet wallet = walletService.getWalletByUserId(user.getId());
            model.addAttribute("wallet", wallet != null ? wallet : new Wallet());

            // Patikriname, ar vartotojas turi adresą, ir pridedame jį prie modelio
            Address address = user.getAddress();
            model.addAttribute("address", address != null ? address : new Address());

            return "dashboard/user";
        } else {
            return "error/404";  // Vartotojas nerastas
        }
    }





    @GetMapping("/dashboard/userorders")
    public String userOrders(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        Map<Long, Product> productMap = new HashMap<>();

        if (orders == null || orders.isEmpty()) {
            model.addAttribute("ordersWithProducts", Collections.emptyList());
            model.addAttribute("noOrdersMessage", "Jūs neturite jokių užsakymų.");
        } else {
            for (Order order : orders) {
                for (Map.Entry<Long, Integer> entry : order.getProductQuantities().entrySet()) {
                    Long productId = entry.getKey();
                    Product product = productService.getProductById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    productMap.put(productId, product);
                }
            }
            model.addAttribute("orders", orders);
            model.addAttribute("productMap", productMap);
        }

        return "dashboard/userorders";
    }




    @GetMapping("/dashboard/userproducts")
    public String userProducts(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Product> products = productService.getProductsByUserId(user.getId());

            // Pridedame krepšelio elementų skaičių į modelį
            int cartItemCount = cartService.getCartItemCount(user);
            model.addAttribute("cartItemCount", cartItemCount);

            model.addAttribute("products", products);
            return "dashboard/userproducts";
        } else {
            return "error/404"; // jei vartotojas nerastas, rodyti klaidos puslapį
        }
    }






}
