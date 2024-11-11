package eu.minted.eos.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.minted.eos.model.*;
import eu.minted.eos.service.*;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

import org.springframework.security.core.Authentication;

import javax.imageio.ImageIO;




@Slf4j
@Controller
@RequestMapping("/product")
public class ProductController {

    private static final java.util.UUID UUID = java.util.UUID.randomUUID();
    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    WalletServiceImpl walletService;

    @Autowired
    CategoryServiceImpl categoryService;

    @Autowired
    TransactionServiceImpl transactionService;

    @Autowired
    EmailService emailService;

    @Autowired
    AddressServiceImpl addressService;

    @GetMapping("/create")
    public String showCreateProductForm(Model model, Authentication authentication) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("product", new Product());

        // Gauti prisijungusį vartotoją
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Pridėti vartotojo adresus į modelį
        List<Address> addresses = addressService.getAddressesByUser(user);
        model.addAttribute("addresses", addresses);  // <-- įsitikinkite, kad šis atributas perduodamas į šabloną

        return "/product/create-product";
    }



    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Optional<Product> productOptional = productService.getProductById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            model.addAttribute("product", product);
            model.addAttribute("address", product.getPickupAddress() != null ? product.getPickupAddress() : new Address()); // Įkeliamas esamas arba naujas adresas
            model.addAttribute("categories", categoryService.getAllCategories());
            return "/product/edit";
        } else {
            return "redirect:/dashboard/userproducts?error=notfound";
        }
    }

    @PostMapping("/update")
    public String updateProduct(@RequestParam("id") Long id,
                                @RequestParam("name") String name,
                                @RequestParam("price") String price,
                                @RequestParam("quantity") int quantity,
                                @RequestParam("description") String description,
                                @RequestParam("file") MultipartFile file,
                                @RequestParam("categoryId") Long categoryId,
                                @ModelAttribute Address pickupAddress, // Naujasis adreso parametras
                                Authentication authentication) {

        try {
            Optional<Product> productOptional = productService.getProductById(id);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();

                // Atnaujiname produkto duomenis
                product.setName(name);
                product.setPrice(new BigDecimal(price));
                product.setQuantity(quantity);
                product.setDescription(description);

                // Atnaujiname arba sukuriame naują paėmimo adresą
                if (pickupAddress != null) {
                    Address updatedAddress = addressService.saveOrUpdateAddress(pickupAddress);
                    product.setPickupAddress(updatedAddress);
                }

                // Patikriname, ar atnaujintas failas
                if (!file.isEmpty()) {
                    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
                    Path targetLocation = Paths.get("src/main/images/" + fileName);
                    Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                    // Jei failas įkeltas, atnaujiname nuorodą į paveikslėlį
                    String filePath = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/images/")
                            .path(fileName)
                            .toUriString();
                    product.setImageUrl(filePath);
                }

                // Išsaugome atnaujintą produktą
                productService.updateProduct(product);

                return "redirect:/dashboard/userproducts";
            } else {
                return "redirect:/dashboard/userproducts?error=notfound";
            }

        } catch (Exception e) {
            log.error("Klaida atnaujinant produktą", e);
            return "redirect:/dashboard/userproducts?error=unknown";
        }
    }



    @PostMapping("/store")
    public String createProduct(@RequestParam("name") String name,
                                @RequestParam("price") String price,
                                @RequestParam("quantity") int quantity,
                                @RequestParam("unit") String unit,
                                @RequestParam("description") String description,
                                @RequestParam("file") MultipartFile file,
                                @RequestParam("categoryId") Long categoryId,
                                @ModelAttribute Address pickupAddress,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {


        log.info("Gautas pavadinimas: {}", name);
        log.info("Gauta kaina: {}", price);
        log.info("Gautas kiekis: {}", quantity);
        log.info("mato vienetas: {}", unit);
        log.info("Gautas aprašymas: {}", description);
        log.info("Gautas failo pavadinimas: {}", file.getOriginalFilename());
        log.info("fileSize: {}", file.getSize());




        try {
            // Patikriname, ar failas turi turinį
            if (file.isEmpty()) {
                log.info("file is empty");
                return "redirect:/product/create?error=fileEmpty";
            }

            // Patikriname, ar failas turi galiojantį paveikslėlio formatą
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = StringUtils.getFilenameExtension(originalFileName).toLowerCase();

            String fileName = UUID + "_." + fileExtension;
            log.info("new file name: {}", fileName);


            if (!isSupportedImageFormat(fileExtension)) {
                log.info("invalid format");
                return "redirect:/product/create?error=invalidFormat";
            }

            // Skaitykime paveikslėlio failą
            BufferedImage originalImage;

            if (fileExtension.equals("webp")) {
                // Naudokime ImageIO kartu su įdiegtu webp palaikymu
                originalImage = ImageIO.read(file.getInputStream());
            } else {
                // Naudokime įprastą skaitymą kitų formatų
                originalImage = ImageIO.read(file.getInputStream());
            }

            if (originalImage == null) {
                log.info("image is empty");
                return "redirect:/product/create?error=invalidImage";
            }

            // Tikriname paveikslėlio rezoliuciją
            int imageWidth = originalImage.getWidth();
            int imageHeight = originalImage.getHeight();
            if (imageWidth < 200 || imageHeight < 200) {
                log.info("image is too small");
                return "redirect:/product/create?error=lowResolution";
            }
            // Paverčiame kainą į BigDecimal
            BigDecimal convertedPrice = new BigDecimal(price);

            // Išvalome failo vardą ir nustatome kelio vietą
            Path targetLocation = Paths.get("src/main/images/" + fileName);

            // Sukurkite naują 24-bit RGB BufferedImage, jei originalus paveikslėlis nėra RGB formato
            BufferedImage rgbImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

            // Nukopijuokite originalų paveikslėlį į naują 24-bit RGB vaizdą
            rgbImage.createGraphics().drawImage(originalImage, 0, 0, null);

            // Išvalome failo vardą ir nustatome kelio vietą
            File outputFile = new File("src/main/images/" + fileName.replaceAll("\\.\\w+$", ".png"));


            Thumbnails.of(rgbImage)
                    .size(800, 600)  // Optimalus dydis jūsų kortelėms
                    .outputFormat("png")  // Konvertuojame į png
                    .toFile(outputFile);

            // Sukuriame nuorodą į paveikslėlį
            String filePath = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/images/")
                    .path(outputFile.getName())
                    .toUriString();

            Category category = categoryService.getCategoryById(categoryId);

            Product product = new Product();
            product.setName(name);
            product.setPrice(convertedPrice);
            product.setQuantity(quantity);
            product.setUnit(unit);
            product.setDescription(description);
            product.setImageUrl(filePath);
            product.setCategory(category);

            // Pridedame paėmimo adresą
            Address savedAddress = addressService.saveAddress(pickupAddress); // Išsaugome adresą per AddressService
            product.setPickupAddress(savedAddress);

            String username = authentication.getName();
            User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            // Patikriname vartotojo piniginės balansą
            Wallet wallet = walletService.getWalletByUserId(user.getId());
            BigDecimal productCreationFee = new BigDecimal("0.30");

            if (wallet.getBalance().compareTo(productCreationFee) < 0) {
                log.info("Nepakankamas balansas piniginėje");
                redirectAttributes.addFlashAttribute("error", "Nepakankamas balansas piniginėje");
                return "redirect:/product/create";
            }
            walletService.withdraw(user.getId(), productCreationFee, "Your ad is now live. A payment of 30 cents has been withdrawn from your account.");

            // Pridedame transakciją
            Transaction transaction = new Transaction(wallet, TransactionType.WITHDRAWAL, productCreationFee, "Apmokėjimas už produkto įkėlimą");
            transactionService.saveTransaction(transaction);

            product.setUser(user);
            System.out.println(product.toString());
            productService.createProduct(product);

            return "redirect:/dashboard/userproducts?message=success";

        } catch (NumberFormatException e) {
            return "redirect:/product/create?error=priceFormat";
        } catch (IOException e) {
            log.error("Klaida įkeliant failą", e);  // Naudojame log.error
            return "redirect:/product/create?error=fileUpload";
        } catch (Exception e) {
            log.error("Nežinoma klaida", e);  // Naudojame log.error
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

    private boolean isSupportedImageFormat(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || extension.equals("webp");
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Authentication authentication) {
        try {
            // Patikriname, ar produktas egzistuoja
            Optional<Product> productOptional = productService.getProductById(id);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();

                // Patikriname, ar šis vartotojas turi teisę ištrinti šį produktą
                String username = authentication.getName();
                User user = userService.getUserByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Jei produktas priklauso vartotojui, tęsiame ištrynimą
                if (product.getUser().getId().equals(user.getId())) {
                    // Jei produktas turi paveikslėlį, ištriname failą iš sistemos
                    if (product.getImageUrl() != null) {
                        deleteProductImage(product.getImageUrl());
                    }

                    // Ištriname produktą iš duomenų bazės
                    productService.deleteProduct(id);
                    log.info("Produktas ištrintas: {}", product.getName());

                    return "redirect:/dashboard/userproducts";
                } else {
                    log.error("Vartotojas neturi teisės ištrinti šio produkto");
                    return "redirect:/dashboard/userproducts?error=unauthorized";
                }
            } else {
                log.error("Produktas nerastas");
                return "redirect:/dashboard/userproducts?error=notfound";
            }
        } catch (Exception e) {
            log.error("Klaida trinant produktą", e);
            return "redirect:/dashboard/userproducts?error=unknown";
        }
    }

    // Papildoma logika paveikslėlio ištrynimui
    private void deleteProductImage(String imageUrl) {
        try {
            // Konvertuojame nuorodą į failo kelią (jei imageUrl buvo sukurtas naudojant ServletUriComponentsBuilder)
            Path filePath = Paths.get("src/main/images", imageUrl.substring(imageUrl.lastIndexOf("/") + 1));
            Files.deleteIfExists(filePath);
            log.info("Paveikslėlis ištrintas: {}", filePath.toString());
        } catch (IOException e) {
            log.error("Klaida trinant paveikslėlį", e);
        }
    }

    @PostMapping("/uploadFolder")
    public String uploadFolder(@RequestParam("folder") MultipartFile[] files, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        log.info("Vartotojas: {}", username);

        MultipartFile jsonFile = null;
        Map<String, MultipartFile> imageFiles = new HashMap<>();

        for (MultipartFile file : files) {
            String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            if (fileName.endsWith(".json")) {
                jsonFile = file;
            } else {
                imageFiles.put(fileName, file);
            }
        }

        if (jsonFile == null) {
            redirectAttributes.addFlashAttribute("error", "JSON failas nerastas.");
            return "redirect:/product/create";
        }

        try (InputStream inputStream = jsonFile.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> products = mapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
            log.info("Produktų kiekis JSON faile: {}", products.size());

            Wallet wallet = walletService.getWalletByUserId(user.getId());
            BigDecimal productUploadFee = new BigDecimal("0.30");
            int uploadedCount = 0;
            int maxFileSize = 5 * 1024 * 1024; // 5 MB limitas

            for (Map<String, Object> productData : products) {
                if (wallet.getBalance().compareTo(productUploadFee) < 0) {
                    log.warn("Nepakanka lėšų piniginėje produktui '{}'", productData.get("name"));
                    continue;
                }

                String name = (String) productData.get("name");
                String description = (String) productData.get("description");
                String imageName = (String) productData.get("imageName");
                String unit = (String) productData.get("unit");
                // Tikriname, ar produktas jau egzistuoja pagal pavadinimą ir aprašymą
                boolean productExists = productService.productExists(productData);
                if (productExists) {
                    log.warn("Produktas '{}' jau egzistuoja.", name);
                    continue;
                }


                MultipartFile imageFile = imageFiles.get(imageName);

                if (imageFile != null) {
                    // Patikriname failo dydį
                    if (imageFile.getSize() > maxFileSize) {
                        log.warn("Paveikslėlis '{}' viršija dydžio limitą ({} MB).", imageName, maxFileSize / 1024 / 1024);
                        redirectAttributes.addFlashAttribute("error", "Paveikslėlis '" + imageName + "' viršija dydžio limitą (5 MB).");
                        continue;
                    }



                    String uniqueFileName = UUID.randomUUID().toString() + "_" + imageName;
                    Path targetLocation = Paths.get("src/main/images/" + uniqueFileName);
                    Files.copy(imageFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                    // Sukuriame adresą pagal JSON duomenis
                    Map<String, String> addressData = (Map<String, String>) productData.get("address");
                    Address address = new Address();

                    address.setStreet(addressData.get("address"));
                    address.setStreet(addressData.get("street"));
                    address.setCity(addressData.get("city"));
                    address.setState(addressData.get("state"));
                    address.setZipCode(addressData.get("zipCode"));
                    address.setCountry(addressData.get("country"));

                    Address pickupAddress = addressService.findOrCreateAddress(address);

                    Product product = new Product();
                    product.setName((String) productData.get("name"));
                    product.setPrice(new BigDecimal((String) productData.get("price")));
                    product.setQuantity((int) productData.get("quantity"));
                    product.setUnit((String) productData.get("unit"));
                    product.setDescription((String) productData.get("description"));
                    product.setCategory(categoryService.getCategoryByName((String) productData.get("productCategory")));
                    product.setUser(user);
                    product.setImageUrl("/images/" + uniqueFileName);
                    product.setPickupAddress(pickupAddress);
                    productService.createProduct(product);

                    // Nuskaičiuojame iš piniginės ir kuriame transakcijos įrašą
                    walletService.withdraw(user.getId(), productUploadFee, "Apmokėjimas už produkto įkėlimą: " + product.getName());
                    createTransaction(wallet, productUploadFee, "Apmokėjimas už produkto įkėlimą: " + product.getName());
                    uploadedCount++;
                }
            }

            redirectAttributes.addFlashAttribute("message", "Įkelta produktų: " + uploadedCount);
            return "redirect:/dashboard/userproducts";

        } catch (IOException e) {
            log.error("Klaida įkeliant produktus", e);
            redirectAttributes.addFlashAttribute("error", "Klaida įkeliant produktus.");
            return "redirect:/product/create";
        }
    }

    // Papildomas metodas, kuris sukuria transakcijos įrašą
    private void createTransaction(Wallet wallet, BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.WITHDRAWAL);  // ar kitas tipas, atitinkantis nuskaičiavimą
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTransactionDate(LocalDateTime.now());
        transactionService.saveTransaction(transaction);
    }

    @PostMapping("/sendProductList")
    public String sendProductList(Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Gauti visus vartotojo produktus
        List<Product> products = productService.getProductsByUserId(user.getId());

        // Paruošti prekių sąrašą el. paštui
        StringBuilder productList = new StringBuilder("Produkto pavadinimas, Kiekis\n");
        for (Product product : products) {
            String imageName = product.getImageUrl().substring(product.getImageUrl().lastIndexOf("/") + 1); // Pavadinimas be UUID
            productList.append(String.format("%s, %d\n", product.getName(), product.getQuantity()));
        }

        // Siųsti el. laišką vartotojui (reikalinga sukurti emailService, jei dar neturite)
        emailService.sendEmail(user.getEmail(), "Jūsų prekių sąrašas", productList.toString());

        redirectAttributes.addFlashAttribute("message", "Prekių sąrašas sėkmingai išsiųstas jums.");
        return "redirect:/dashboard/userproducts";
    }


}
