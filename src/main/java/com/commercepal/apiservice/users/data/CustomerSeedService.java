package com.commercepal.apiservice.users.data;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.credential.CredentialRepository;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.users.customer.CustomerRepository;
import com.commercepal.apiservice.users.customer.address.AddressRepository;
import com.commercepal.apiservice.users.customer.address.AddressSourceType;
import com.commercepal.apiservice.users.customer.address.CustomerAddress;
import com.commercepal.apiservice.users.enums.IdentityProvider;
import com.commercepal.apiservice.users.enums.UserStatus;
import com.commercepal.apiservice.users.enums.UserType;
import com.commercepal.apiservice.config.AppInitProperties;
import com.commercepal.apiservice.users.referral.ReferralCodeUtils;
import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.role.RoleDefinition;
import com.commercepal.apiservice.users.role.RoleDefinitionRepository;
import com.commercepal.apiservice.users.till.TillSequenceService;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for seeding test customers with random data.
 * Provides API-callable methods for creating test customer data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerSeedService {

  private static final String[] FIRST_NAMES = {
      "Abebe", "Bekele", "Chala", "Dawit", "Elias", "Frehiwot", "Girma", "Hana", "Ibrahim", "Jalene",
      "Kebede", "Lemlem", "Meseret", "Nardos", "Obsa", "Petros", "Rahel", "Solomon", "Tigist", "Yonas",
      "Zerihun", "Alem", "Biruk", "Chaltu", "Daniel", "Eden", "Fikadu", "Getachew", "Helen", "Ismail",
      "Jemal", "Kiros", "Liya", "Meron", "Natnael", "Olana", "Paulos", "Ruth", "Samuel", "Tsion",
      "Wondwosen", "Yared", "Zekarias", "Amanuel", "Bethlehem", "Dereje", "Emebet", "Fasil", "Genet",
      "Hailu", "Israel", "Johannes", "Kidist", "Lidya", "Mulugeta", "Nahom", "Omega", "Peniel", "Ribka",
      "Senait", "Tadesse", "Urgessa", "Veronica", "Wendim", "Yeshimebet", "Zufan", "Aster", "Biniam",
      "Caalaa", "Degu", "Eyob", "Frezer", "Girmay", "Hiwot", "Isayas", "Joseph", "Kaleb", "Lulit",
      "Martha", "Nebiyu", "Oromia", "Philipos", "Rediet", "Selamawit", "Tesfaye", "Urael", "Worku",
      "Yemisrach", "Zeleke", "Abel", "Belaynesh", "Desta", "Ermias", "Fana", "Gebremedhin", "Henok"
  };

  private static final String[] LAST_NAMES = {
      "Abate", "Belay", "Chernet", "Demissie", "Eshetu", "Fekadu", "Gebre", "Haile", "Issa", "Jebessa",
      "Kassahun", "Legesse", "Mengistu", "Negash", "Oljira", "Petros", "Regassa", "Shiferaw", "Tefera",
      "Urgessa", "Wolde", "Yimer", "Zewde", "Ayele", "Bekele", "Debebe", "Endale", "Feyissa", "Gidey",
      "Habte", "Ibrahim", "Jirata", "Kedir", "Lemma", "Mekuria", "Negussie", "Olani", "Paulos", "Reta",
      "Seifu", "Tadesse", "Umer", "Worku", "Yohannes", "Zeleke", "Alemu", "Birru", "Dagne", "Erko",
      "Fulas", "Getahun", "Hundessa", "Ismail", "Jiru", "Kassa", "Lemi", "Moges", "Nuru", "Oromo",
      "Fikre", "Girma", "Habtamu", "Jaleta", "Kelbessa", "Leta", "Mohammed", "Nigatu", "Obsi", "Petru",
      "Rorisa", "Sileshi", "Tolessa", "Utura", "Wako", "Yisak", "Zena", "Ahmed", "Biru", "Dejene",
      "Edo", "Fita", "Gudina", "Hunde", "Idris", "Jarso", "Ketema", "Liban", "Mulatu", "Nagasa"
  };

  private static final String[] ETHIOPIAN_CITIES = {
      "Addis Ababa", "Dire Dawa", "Mekelle", "Adama", "Gondar", "Hawassa", "Bahir Dar", "Jimma",
      "Dessie", "Jijiga", "Shashamane", "Bishoftu", "Sodo", "Arba Minch", "Hosaena", "Harar",
      "Dilla", "Nekemte", "Debre Birhan", "Asella"
  };

  private static final String[] ETHIOPIAN_STATES = {
      "Addis Ababa", "Oromia", "Amhara", "SNNPR", "Tigray", "Somali", "Afar", "Benishangul-Gumuz",
      "Gambela", "Harari", "Dire Dawa", "Sidama"
  };

  private static final String[] DISTRICTS = {
      "Bole", "Kirkos", "Yeka", "Arada", "Lideta", "Gulele", "Kolfe Keranio", "Akaki Kality",
      "Nifas Silk-Lafto", "Addis Ketema", "Woreda 1", "Woreda 2", "Woreda 3", "Woreda 4", "Woreda 5"
  };

  private static final String[] STREETS = {
      "Africa Avenue", "Bole Road", "Churchill Avenue", "Meskel Square", "Arat Kilo", "Piassa",
      "Mexico Square", "Stadium Area", "Gerji", "CMC", "Megenagna", "22 Mazoria", "Sarbet",
      "Saris", "Kaliti", "Kera", "Bethel", "Gotera", "Lebu", "Summit"
  };

  private static final String[] LANDMARKS = {
      "Near Edna Mall", "Opposite Friendship Mall", "Behind Bole Medhanialem Church",
      "Near Millennium Hall", "Across from National Museum", "Next to Hilton Hotel",
      "Near Meskel Square", "Behind Black Lion Hospital", "Near Merkato", "Opposite Stadium",
      "Near Airport", "Behind Unity Park", "Next to Skylight Hotel", "Near Entoto Park",
      "Opposite Grand Palace Hotel"
  };

  private final CustomerRepository customerRepository;
  private final CredentialRepository credentialRepository;
  private final RoleDefinitionRepository roleDefinitionRepository;
  private final AddressRepository addressRepository;
  private final PasswordEncoder passwordEncoder;
  private final TillSequenceService tillSequenceService;
  private final ReferralCodeUtils referralCodeUtils;
  private final EntityManager entityManager;
  private final AppInitProperties appInit;

  private final Random random = new Random();

  private String getCustomerDefaultPassword() {
    String p = appInit.getCustomerDefaultPassword();
    return (p != null && !p.isBlank()) ? p : "123456";
  }

  /**
   * Seed customers with default settings.
   *
   * @param count number of customers to seed (default 1000)
   * @return seeding result with statistics
   */
  @Transactional
  public SeedResult seedCustomers(int count) {
    return seedCustomers(SeedRequest.builder()
        .count(count)
        .password(getCustomerDefaultPassword())
        .build());
  }

  /**
   * Seed customers with custom settings.
   *
   * @param request seed request with custom options
   * @return seeding result with statistics
   */
  @Transactional
  public SeedResult seedCustomers(SeedRequest request) {
    if (!appInit.isEnabled()) {
      log.warn("Customer seeding skipped: app.init.enabled=false");
      throw new IllegalStateException(
          "Data initialization is disabled (app.init.enabled=false). Set APP_INIT_ENABLED=true to allow seeding.");
    }

    int count = request.count() > 0 ? request.count() : 1000;
    String password = request.password() != null ? request.password() : getCustomerDefaultPassword();

    log.info("==========================================================");
    log.info("Starting Customer Seeding via API...");
    log.info("Target customer count: {}", count);
    log.info("==========================================================");

    RoleDefinition customerRole = roleDefinitionRepository.findByCode(RoleCode.ROLE_CUSTOMER)
        .orElseThrow(() -> new IllegalStateException(
            "ROLE_CUSTOMER not found. Roles must be initialized first."));

    String encodedPassword = passwordEncoder.encode(password);

    int createdCount = 0;
    int existingCount = 0;
    int failedCount = 0;
    int batchSize = 50;

    long startTime = System.currentTimeMillis();

    for (int i = 1; i <= count; i++) {
      try {
        CustomerSeedResult result = createCustomerWithAddress(i, customerRole, encodedPassword);
        if (result.created()) {
          createdCount++;
        } else {
          existingCount++;
        }

        // Flush and clear in batches to avoid memory issues
        if (i % batchSize == 0) {
          entityManager.flush();
          entityManager.clear();
          log.info("Processed {} customers ({} created, {} existing)...", i, createdCount, existingCount);
        }
      } catch (Exception e) {
        log.warn("Failed to create customer {}: {}", i, e.getMessage());
        failedCount++;
        entityManager.clear();
      }
    }

    long duration = System.currentTimeMillis() - startTime;

    log.info("==========================================================");
    log.info("Customer seeding complete: {} created, {} existing, {} failed", 
        createdCount, existingCount, failedCount);
    log.info("Duration: {} ms", duration);
    log.info("==========================================================");

    return SeedResult.builder()
        .totalRequested(count)
        .created(createdCount)
        .existing(existingCount)
        .failed(failedCount)
        .durationMs(duration)
        .password(password)
        .build();
  }

  /**
   * Create a single customer with address.
   */
  private CustomerSeedResult createCustomerWithAddress(int index, RoleDefinition customerRole, String encodedPassword) {
    String email = generateEmail(index);
    String phone = generatePhoneNumber(index);

    // Check if customer already exists by email or phone
    if (credentialRepository.existsByEmailAddressAndDeletedFalse(email)) {
      log.debug("Customer already exists | email={}", email);
      ensureCustomerHasAddress(email);
      return new CustomerSeedResult(false, null);
    }

    if (credentialRepository.existsByPhoneNumberAndDeletedFalse(phone)) {
      log.debug("Customer already exists | phone={}", phone);
      ensureCustomerHasAddressByPhone(phone);
      return new CustomerSeedResult(false, null);
    }

    LocalDateTime now = LocalDateTime.now();
    String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];

    // Generate account number
    String accountNumber = tillSequenceService.generateAccountNumber(UserType.CUSTOMER);
    String commissionAccount = accountNumber + "1";
    String referralCode = referralCodeUtils.generateCustomerReferralCode();

    // Create credential first
    Credential credential = Credential.builder()
        .userType(UserType.CUSTOMER)
        .emailAddress(email)
        .phoneNumber(phone)
        .passwordHash(encodedPassword)
        .status(UserStatus.ACTIVE)
        .deleted(false)
        .version(0L)
        .createdAt(now)
        .createdBy("SYSTEM_SEED")
        .identityProvider(IdentityProvider.LOCAL)
        .failedSignInAttempts(0)
        .passwordResetFailedAttempts(0)
        .requiresPasswordChange(false)
        .mfaEnabled(false)
        .emailVerified(true)
        .phoneVerified(true)
        .emailVerifiedAt(now)
        .phoneVerifiedAt(now)
        .build();

    credential.assignRole(customerRole);
    Credential savedCredential = credentialRepository.save(credential);

    // Create customer entity using builder
    Customer customer = Customer.builder()
        .oldCustomerId(0L)
        .accountNumber(accountNumber)
        .commissionAccount(commissionAccount)
        .firstName(firstName)
        .lastName(lastName)
        .country(SupportedCountry.ETHIOPIA.getCode())
        .city(ETHIOPIAN_CITIES[random.nextInt(ETHIOPIAN_CITIES.length)])
        .preferredLanguage("en")
        .preferredCurrency(SupportedCurrency.ETB)
        .referralCode(referralCode)
        .registrationChannel(Channel.WEB)
        .createdAt(now)
        .createdBy("SYSTEM_SEED")
        .isDeleted(false)
        .version(0L)
        .credential(savedCredential)
        .build();

    Customer savedCustomer = customerRepository.save(customer);

    // Create address for the customer
    createAddressForCustomer(savedCustomer, firstName, lastName, phone);

    log.debug("Customer created | id={} | email={} | phone={}", savedCustomer.getId(), email, phone);
    return new CustomerSeedResult(true, savedCustomer);
  }

  /**
   * Create address for a customer.
   */
  private void createAddressForCustomer(Customer customer, String firstName, String lastName, String phone) {
    CustomerAddress address = CustomerAddress.builder()
        .customer(customer)
        .receiverName(firstName + " " + lastName)
        .phoneNumber(phone)
        .country(SupportedCountry.ETHIOPIA.getCode())
        .state(ETHIOPIAN_STATES[random.nextInt(ETHIOPIAN_STATES.length)])
        .city(ETHIOPIAN_CITIES[random.nextInt(ETHIOPIAN_CITIES.length)])
        .district(DISTRICTS[random.nextInt(DISTRICTS.length)])
        .street(STREETS[random.nextInt(STREETS.length)])
        .houseNumber("House " + ThreadLocalRandom.current().nextInt(1, 999))
        .landmark(LANDMARKS[random.nextInt(LANDMARKS.length)])
        .addressLine1("Building " + ThreadLocalRandom.current().nextInt(1, 50))
        .addressLine2("Floor " + ThreadLocalRandom.current().nextInt(1, 10))
        .latitude(String.format("%.6f", 8.9 + random.nextDouble() * 0.2))
        .longitude(String.format("%.6f", 38.7 + random.nextDouble() * 0.2))
        .addressSource(AddressSourceType.MANUAL)
        .isDefault(true)
        .build();

    address.setCreatedBy("SYSTEM_SEED");
    address.setIsDeleted(false);

    addressRepository.save(address);
    log.debug("Address created for customer | customerId={}", customer.getId());
  }

  /**
   * Ensure an existing customer has an address (by email).
   */
  private void ensureCustomerHasAddress(String email) {
    credentialRepository.findByEmailAddress(email).ifPresent(credential -> {
      customerRepository.findByCredential_Id(credential.getId()).ifPresent(customer -> {
        if (addressRepository.findByCustomer_IdAndIsDeletedFalse(customer.getId()).isEmpty()) {
          createAddressForCustomer(customer, customer.getFirstName(), customer.getLastName(),
              credential.getPhoneNumber() != null ? credential.getPhoneNumber() : generatePhoneNumber(random.nextInt(10000)));
          log.info("Created missing address for existing customer | customerId={}", customer.getId());
        }
      });
    });
  }

  /**
   * Ensure an existing customer has an address (by phone).
   */
  private void ensureCustomerHasAddressByPhone(String phone) {
    credentialRepository.findByPhoneNumber(phone)
        .flatMap(credential -> customerRepository.findByCredential_Id(credential.getId()))
        .ifPresent(customer -> {
          if (addressRepository.findByCustomer_IdAndIsDeletedFalse(customer.getId()).isEmpty()) {
            createAddressForCustomer(customer, customer.getFirstName(), customer.getLastName(),
                phone);
            log.info("Created missing address for existing customer | customerId={}",
                customer.getId());
          }
        });
  }

  /**
   * Generate a unique email for customer.
   */
  private String generateEmail(int index) {
    String firstName = FIRST_NAMES[index % FIRST_NAMES.length].toLowerCase();
    String lastName = LAST_NAMES[index % LAST_NAMES.length].toLowerCase();
    return String.format("%s.%s.%04d@testcustomer.com", firstName, lastName, index);
  }

  /**
   * Generate a unique Ethiopian phone number.
   */
  private String generatePhoneNumber(int index) {
    int prefix = 91 + (index % 9);
    int suffix = 1000000 + index;
    return String.format("+251%d%07d", prefix, suffix % 10000000);
  }

  // ==================== DTOs ====================

  /**
   * Request for seeding customers.
   */
  @Builder
  public record SeedRequest(
      int count,
      String password
  ) {}

  /**
   * Result of customer seeding operation.
   */
  @Builder
  public record SeedResult(
      int totalRequested,
      int created,
      int existing,
      int failed,
      long durationMs,
      String password
  ) {}

  /**
   * Internal result of creating a single customer.
   */
  private record CustomerSeedResult(boolean created, Customer customer) {}
}
