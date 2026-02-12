package com.commercepal.apiservice.users.customer.address;

import com.commercepal.apiservice.shared.BaseAuditEntity;
import com.commercepal.apiservice.users.customer.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CustomerAddress
 * <p>
 * A globally standardized, professional-grade shipping address model supporting Ethiopia, Kenya,
 * UAE, and Somalia, while remaining compatible with any international logistics platform
 * (AliExpress, Amazon, DHL).
 * <p>
 * All location fields use universal naming that maps cleanly across countries:
 * <p>
 * state → Ethiopia Region, Kenya County, UAE Emirate, Somalia Region county → Ethiopia Zone, Kenya
 * Subcounty, UAE Municipality, Somalia District city → Universal city name (Addis Ababa, Nairobi,
 * Dubai, Hargeisa) district→ Ethiopia Subcity/Woreda, Kenya Ward, UAE District, Somalia Subdistrict
 * street → Universal street/area/road
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_addresses", indexes = {
    @Index(name = "idx_customer_default", columnList = "customer_id,is_default"),
    @Index(name = "idx_customer_status", columnList = "customer_id,status"),
    @Index(name = "idx_country_city", columnList = "country,city")
})
public class CustomerAddress extends BaseAuditEntity {

  @Column(name = "old_address_id")
  private Long oldAddressId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_address_customer"))
  private Customer customer;

  // Receiver Contact Info
  @Column(name = "receiver_name", length = 120, nullable = false)
  private String receiverName;

  @Column(name = "phone_number", length = 32, nullable = false)
  private String phoneNumber;

  // Country
  @Column(name = "country", length = 4, nullable = false)
  private String country;

  // Global Location Structure
  /**
   * STATE (Global) - Ethiopia: Regional State (e.g., Addis Ababa, Oromia) - Kenya: County (e.g.,
   * Nairobi County) - UAE: Emirate (e.g., Dubai, Abu Dhabi) - Somalia: Region (e.g., Banaadir)
   */
  @Column(name = "state", length = 120)
  private String state;

  /**
   * CITY (Universal) - Works the same across all countries
   */
  @Column(name = "city", length = 120)
  private String city;

  /**
   * DISTRICT (Third level) - Ethiopia: Subcity or Woreda - Kenya: Ward - UAE: District area -
   * Somalia: Subdistrict
   */
  @Column(name = "district", length = 120)
  private String district;

  /**
   * STREET (Fourth level) - Universal street name, road name, or area - Ethiopia: neighborhood /
   * sefer / street
   */
  @Column(name = "street", length = 255)
  private String street;

  // Detailed Address
  @Column(name = "house_number", length = 60)
  private String houseNumber;

  @Column(name = "landmark", length = 255)
  private String landmark; // Example: “near Edna Mall”

  @Column(name = "address_line1", length = 255)
  private String addressLine1;

  @Column(name = "address_line2", length = 255)
  private String addressLine2;

  // Geo Information
  @Column(name = "latitude", length = 50)
  private String latitude;

  @Column(name = "longitude", length = 50)
  private String longitude;

  // Status + Source
  /**
   * How the address was added: - MANUAL - GOOGLE_MAPS - PIN_LOCATION - DELIVERY_AGENT
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "address_source", length = 30, nullable = false)
  private AddressSourceType addressSource;

  /**
   * Mark this as the default shipping address
   */
  @Builder.Default
  @Column(name = "is_default", nullable = false)
  private Boolean isDefault = false;
}
