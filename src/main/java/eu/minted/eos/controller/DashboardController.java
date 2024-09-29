package eu.minted.eos.controller;

import eu.minted.eos.model.Product;
import eu.minted.eos.service.ProductService;
import eu.minted.eos.service.ProductServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private ProductServiceImpl productService;

    @GetMapping("/dashboard/index")
    public String listProducts(Model model,
                               @RequestParam(defaultValue = "1") int pageNumber,
                               @RequestParam(defaultValue = "10") int pageSize) {

        // Gauti produktus su paginacija
        Page<Product> productPage = productService.getAllProducts(PageRequest.of(pageNumber - 1, pageSize));

        // Pridėti produktus į modelį
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("pageSize", pageSize);

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
