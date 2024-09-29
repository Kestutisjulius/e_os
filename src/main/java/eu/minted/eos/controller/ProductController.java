package eu.minted.eos.controller;

import eu.minted.eos.model.Product;
import eu.minted.eos.model.User;
import eu.minted.eos.service.ProductService;
import eu.minted.eos.service.ProductServiceImpl;
import eu.minted.eos.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.security.core.Authentication;



@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/create")
    public String showCreateProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "/product/create-product";
    }

    @PostMapping("/store")
    public String createProduct(@RequestParam("name") String name,
                                @RequestParam("price") String price,
                                @RequestParam("quantity") int quantity,
                                @RequestParam("description") String description,
                                @RequestParam("file") MultipartFile file,
                                Authentication authentication) {
        try {
            // Paversti kainą į BigDecimal
            BigDecimal convertedPrice = new BigDecimal(price);

            // Išvalyti ir nustatyti failo pavadinimą
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            Path targetLocation = Paths.get("src/main/resources/public/img/" + fileName);

            // Kopijuoti failą į nurodytą vietą
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Sukurti nuorodą į paveikslėlį
            String filePath = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/public/img/")
                    .path(fileName)
                    .toUriString();

            // Sukurti naują produktą
            Product product = new Product();
            product.setName(name);
            product.setPrice(convertedPrice);
            product.setQuantity(quantity);
            product.setDescription(description);
            product.setImageUrl(filePath);

            // Gauti vartotoją ir priskirti jį produktui
            String username = authentication.getName();
            User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            product.setUser(user);

            // Išsaugoti produktą
            productService.createProduct(product);

            // Nukreipti į produktų sąrašą po sėkmingo išsaugojimo
            return "redirect:/dashboard/userproducts";

        } catch (NumberFormatException e) {
            // Tvarkyti kainos konvertavimo klaidą
            return "redirect:/product/create?error=priceFormat";
        } catch (IOException e) {
            // Tvarkyti failo įkėlimo klaidą
            return "redirect:/product/create?error=fileUpload";
        } catch (Exception e) {
            // Tvarkyti kitas klaidas
            return "redirect:/product/create?error=unknown";
        }
    }



    @PostMapping("/upload") //kopijuojamas paveikslėlis - pavyzdys
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path targetLocation = Paths.get("src/main/resources/public/img"+fileName);
        try{
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            String fileUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/public/img")
                    .path(fileName)
                    .toUriString();
            return ResponseEntity.status(HttpStatus.CREATED).body(fileUri);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
