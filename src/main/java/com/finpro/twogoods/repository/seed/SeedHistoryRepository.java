package com.finpro.twogoods.repository.seed;

import com.finpro.twogoods.entity.seed.SeedHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeedHistoryRepository extends JpaRepository<SeedHistory, String> {

	boolean existsBySeedName(String seedName);
}

