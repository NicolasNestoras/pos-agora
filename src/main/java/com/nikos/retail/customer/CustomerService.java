package com.nikos.retail.customer;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nikos.retail.common.exception.ResourceNotFoundException;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository){
        this.customerRepository = customerRepository;
    }
    

    public List<CustomerResponse> getAllCustomers(){
        return customerRepository.findAll()
            .stream()
            .map(CustomerResponse::fromEntity)
            .collect(Collectors.toList());
    }

    public CustomerResponse getCustomerById(Long id){
        Customer customer = customerRepository.findById(id)
            .orElseThrow(()->new ResourceNotFoundException("Customer with this id does not exist."));
        return CustomerResponse.fromEntity(customer);
    }

    public CustomerResponse createCustomer(CustomerRequest request){
        if (request.getEmail()!=null && customerRepository.findByEmail(request.getEmail()).isPresent()){
            throw new IllegalStateException("A customer with this email already exists.");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCustomerType(request.getCustomerType());

        Customer saved = customerRepository.save(customer);
        return CustomerResponse.fromEntity(saved);

    }
}
