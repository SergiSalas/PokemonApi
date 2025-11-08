package com.sergisalas.pokemonapi.service.dto;

import lombok.Data;
import java.util.List;

@Data
public class PokemonListResponse {
    private Integer count;
    private String next;
    private String previous;
    private List<PokemonBasic> results;

    @Data
    public static class PokemonBasic {
        private String name;
        private String url;
    }
}

