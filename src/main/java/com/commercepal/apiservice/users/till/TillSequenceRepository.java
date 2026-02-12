package com.commercepal.apiservice.users.till;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TillSequenceRepository extends JpaRepository<TillSequence, Long> {

  Optional<TillSequence> findByUniqueId(String uniqueId);
}