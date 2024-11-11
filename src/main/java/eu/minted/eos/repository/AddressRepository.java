package eu.minted.eos.repository;

import eu.minted.eos.model.Address;
import eu.minted.eos.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    Optional<Address> findByAddressAndStreetAndCityAndStateAndZipCodeAndCountry(
            String address,
            String street,
            String city,
            String state,
            String zipCode,
            String country
    );

    @Query("SELECT DISTINCT p.pickupAddress FROM Product p WHERE p.user = :user")
    List<Address> findAddressesByUser(@Param("user") User user);
}
