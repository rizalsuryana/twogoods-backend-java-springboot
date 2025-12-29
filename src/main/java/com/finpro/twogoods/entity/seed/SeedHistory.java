package com.finpro.twogoods.entity.seed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "seed_history")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeedHistory {

	@Id
	@Column(name = "seed_name", length = 100, nullable = false)
	private String seedName;

	@Column(name = "executed_at", nullable = false)
	private LocalDateTime executedAt;

}

