package eu.minted.eos.controller;

import eu.minted.eos.model.Category;
import eu.minted.eos.model.Product;
import eu.minted.eos.model.User;
import eu.minted.eos.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    CartServiceImpl cartService;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    CategoryServiceImpl categoryService;

    @GetMapping("/dashboard/index")
    public String listProducts(Model model,
                               @RequestParam(value = "categoryId", required = false) Long categoryId,
                               @RequestParam(defaultValue = "1") int pageNumber,
                               @RequestParam(defaultValue = "10") int pageSize,
                               Authentication authentication) {

        // Patikriname, ar kategorija yra pasirinkta, ir filtruojame produktus pagal ją
        Page<Product> productPage;
        if (categoryId != null) {
            productPage = productService.getProductsByCategoryId(categoryId, PageRequest.of(pageNumber - 1, pageSize));
        } else {
            productPage = productService.getAllProducts(PageRequest.of(pageNumber - 1, pageSize));
        }

        // Gauti visas kategorijas filtrui
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        // Pridėti produktus ir paginacijos informaciją į modelį
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("pageSize", pageSize);

        // Pridedame krepšelio elementų skaičių į modelį
        String currentUsername = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(currentUsername);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            int cartItemCount = cartService.getCartItemCount(user);
            model.addAttribute("cartItemCount", cartItemCount);
        }

        return "dashboard/index";
    }


    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Gauti atsitiktines prekes (pvz., 5 atsitiktinės prekės)
        List<Product> randomProducts = productService.getRandomProducts(5);
        model.addAttribute("randomProducts", randomProducts);

        return "shop/index"; // Nukreipti į šį šabloną
    }
}
