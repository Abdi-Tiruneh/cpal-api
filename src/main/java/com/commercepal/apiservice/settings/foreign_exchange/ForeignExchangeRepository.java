package com.commercepal.apiservice.settings.foreign_exchange;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForeignExchangeRepository extends JpaRepository<ForeignExchange, Long> {

  Optional<ForeignExchange> findByBaseCurrencyAndTargetCurrency(SupportedCurrency base,
      SupportedCurrency target);

  Optional<ForeignExchange> findByTargetCurrency(SupportedCurrency target);

  List<ForeignExchange> findByBaseCurrency(SupportedCurrency currency);
}

