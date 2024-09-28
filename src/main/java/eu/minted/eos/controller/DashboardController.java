package eu.minted.eos.controller;

import eu.minted.eos.model.Product;
import eu.minted.eos.service.ProductService;
import eu.minted.eos.service.ProductServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private ProductServiceImpl productService;

    @GetMapping("/")
    public String index(Model model) {
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
