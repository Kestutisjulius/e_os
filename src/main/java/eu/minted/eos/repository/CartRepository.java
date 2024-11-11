package eu.minted.eos.repository;

import eu.minted.eos.model.Cart;
import eu.minted.eos.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Gauti krepšelį pagal vartotoją
    Optional<Cart> findByUser(User user);

    List<Cart> findAllByCreatedDateBefore(LocalDateTime expirationTime);
}
