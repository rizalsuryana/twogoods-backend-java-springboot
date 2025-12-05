package com.finpro.twogoods.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "transaction")
@NoArgsConstructor
//@AllArgsConstructor
@Getter
@Setter
//@Builder
public class Transaction extends BaseEntity {
}
