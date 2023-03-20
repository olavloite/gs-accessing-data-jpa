package com.example.accessingdatajpa;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
  private static final Logger log = LoggerFactory.getLogger(AccessingDataJpaApplication.class);
  private final Random random = new Random();

  private final CustomerRepository repository;
  
  public CustomerService(CustomerRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public List<Customer> updateAndSearchCustomer(long id1, long id2) {
    log.info("Looking up customer with id " + id1);
    // This will trigger a SELECT statement, as this customer is not in the current Hibernate session.
    Customer customer1 = repository.findById(id1);
    // Set a new random last name. This will only buffer the modification in the Hibernate session,
    // but not actually send it to the database.
    String newLastName = "New last name " + random.nextLong();
    customer1.setLastName(newLastName);
    
    // Getting another customer by ID can be done without flushing the session.
    Customer customer2 = repository.findById(id2);
    log.info("Found customer2 with last name " + customer2.getLastName());
    Customer customer3 = repository.findById(id1);
    if (!customer3.getLastName().equals(newLastName)) {
      throw new IllegalStateException("Last names differ!");
    }
    
    // This will trigger an automatic flush of the current Hibernate session. That is; the UPDATE
    // statement that was buffered by the customer1.setLastName(..) above will now be sent to the
    // database without the transaction being committed. The returned list will include customer1,
    // as that customer has this last name and the modification has been sent to the database.
    // That is; Hibernate supports Read-Your-Writes in a transaction in its default setup.
    List<Customer> customers = repository.findByLastName(newLastName);
    if (customers.size() != 1) {
      throw new IllegalStateException(String.format("Unexpected number of customers with last name %s: %d", newLastName, customers.size()));
    }
    return customers;
  }

}
