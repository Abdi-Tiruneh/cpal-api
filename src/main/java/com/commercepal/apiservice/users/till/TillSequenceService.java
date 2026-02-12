package com.commercepal.apiservice.users.till;

import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.users.enums.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TillSequenceService {

  private final TillSequenceRepository tillSequenceRepository;

  public String generateAccountNumber(UserType userType) {
    return tillSequenceRepository.findByUniqueId(userType.name()).map(sequence -> {
      long next = Long.parseLong(sequence.getSeries()) + 1;
      String nextNumber = String.format("%0" + sequence.getSeriesLength() + "d", next);
      sequence.setSeries(String.valueOf(next));
      tillSequenceRepository.save(sequence);
      log.debug("Account number generated | accountNumber={}", nextNumber);
      return nextNumber;
    }).orElseThrow(() -> {
      log.error("Account sequence configuration not found | userType=CUSTOMER");
      return new ResourceNotFoundException("Account sequence configuration not found.");
    });
  }

}
