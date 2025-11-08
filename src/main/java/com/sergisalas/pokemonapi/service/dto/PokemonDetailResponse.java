package com.sergisalas.pokemonapi.service.dto;

import lombok.Data;

@Data
public class PokemonDetailResponse {
    private Integer id;
    private String name;
    private Integer height;
    private Integer weight;
    private Integer base_experience;
}