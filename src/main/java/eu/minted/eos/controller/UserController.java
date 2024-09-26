package eu.minted.eos.controller;

import eu.minted.eos.model.User;
import eu.minted.eos.service.OrderService;
import eu.minted.eos.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;


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



    @GetMapping("/dashboard/user")
    public String userDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.getUserByUsername(username);
        model.addAttribute("user", user);
        return "dashboard/user";
    }
}
