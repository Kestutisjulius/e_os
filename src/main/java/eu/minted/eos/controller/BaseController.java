package eu.minted.eos.controller;

import eu.minted.eos.model.User;
import eu.minted.eos.service.CartServiceImpl;
import eu.minted.eos.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@Controller
public class BaseController {

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private UserServiceImpl userService;

    // Šis metodas prideda cartItemCount kintamąjį į kiekvieną modelį
    @ModelAttribute("cartItemCount")
    public int getCartItemCount(Authentication authentication) {
        if (authentication != null) {
            String username = authentication.getName();
            Optional<User> user = userService.getUserByUsername(username);
            if (user.isPresent()) {
                return cartService.getCartItemCount(user.get());
            }
        }
        return 0; // Jei vartotojas neprisijungęs arba nėra prekių krepšelyje
    }
}

