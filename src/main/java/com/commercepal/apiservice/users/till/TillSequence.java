package com.commercepal.apiservice.users.till;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TillSequence", schema = "dbo", catalog = "CommercePal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TillSequence {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "UniqueId", nullable = false, unique = true)
  private String uniqueId;

  @Column(name = "Series", nullable = false)
  private String series;

  @Column(name = "SeriesLength", nullable = false)
  private Integer seriesLength;

  @Column(name = "Description")
  private String description;

  @Column(name = "StartSequence", nullable = false)
  private Long startSequence;
}
