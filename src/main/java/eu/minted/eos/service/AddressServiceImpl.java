package eu.minted.eos.service;

import eu.minted.eos.model.Address;
import eu.minted.eos.model.User;
import eu.minted.eos.repository.AddressRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    @Transactional
    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address updateAddress(Address address) {
        if (address.getId() == null) {
            throw new IllegalArgumentException("Address ID cannot be null for update");
        }
        return addressRepository.save(address);
    }

    @Override
    public Address getAddressById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + id));
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Address saveOrUpdateAddress(Address address) {
        if (address.getId() != null && addressRepository.existsById(address.getId())) {
            return updateAddress(address);
        } else {
            return saveAddress(address);
        }
    }

    @Override
    @Transactional
    public Address findOrCreateAddress(Address address) {
        // Tikriname, ar adresas jau yra duomenų bazėje
        Optional<Address> existingAddress = addressRepository.findByAddressAndStreetAndCityAndStateAndZipCodeAndCountry(
                address.getAddress(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry()
        );

        return existingAddress.orElseGet(() -> addressRepository.save(address));
    }

    @Override
    public List<Address> getAddressesByUser(User user) {
        return addressRepository.findAddressesByUser(user);
    }
}
