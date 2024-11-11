package eu.minted.eos.service;

import eu.minted.eos.model.Address;
import eu.minted.eos.model.User;

import java.util.List;

public interface AddressService {
    Address saveAddress(Address address);

    Address updateAddress(Address address);

    Address getAddressById(Long id);

    void deleteAddress(Long id);

    Address saveOrUpdateAddress(Address address);

    Address findOrCreateAddress(Address address);


    List<Address> getAddressesByUser(User user);
}
