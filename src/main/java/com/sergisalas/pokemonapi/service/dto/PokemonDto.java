package com.sergisalas.pokemonapi.service.dto;

import com.sergisalas.pokemonapi.domain.Pokemon;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class PokemonDto {
    private Integer pokeApiId;

    private String name;

    private Integer weight;
    private Integer height;
    private Integer baseExperience;


    public PokemonDto(Pokemon pokemon) {
        this.pokeApiId = pokemon.getPokeApiId();
        this.name = pokemon.getName();
        this.weight = pokemon.getWeight();
        this.height = pokemon.getHeight();
        this.baseExperience = pokemon.getBaseExperience();
    }
}
