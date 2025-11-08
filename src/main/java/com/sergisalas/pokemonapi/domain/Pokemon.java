package com.sergisalas.pokemonapi.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
@Table(name = "Pokemons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pokemon {

    @Id
    private String id = java.util.UUID.randomUUID().toString();

    private Integer pokeApiId;

    private String name;

    private Integer weight;
    private Integer height;
    private Integer baseExperience;

    @Lob
    private String rawJson;

    private Instant lastSynced;
}
