package eu.minted.eos.controller;

import eu.minted.eos.model.Product;
import eu.minted.eos.service.ProductService;
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

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/create")
    public String showCreateProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "create-product";
    }

    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@RequestParam("name") String name,
                                                 @RequestParam("price") String price,
                                                 @RequestParam("quantity") int quantity,
                                                 @RequestParam("description") String description,
                                                 @RequestParam("file") MultipartFile file){
        try {

            BigDecimal convertedPrice = new BigDecimal(price);
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            Path targetLocation = Paths.get("src/main/resources/public/img" + fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            String filePath = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/public/img/")
                    .path(fileName)
                    .toUriString();
            Product product = new Product();
            product.setName(name);
            product.setPrice(convertedPrice);
            product.setQuantity(quantity);
            product.setDescription(description);
            product.setImageUrl(filePath);

            Product savedProduct = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);

        }catch (NumberFormatException e){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
