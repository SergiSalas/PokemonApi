package com.sergisalas.pokemonapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergisalas.pokemonapi.domain.Pokemon;
import com.sergisalas.pokemonapi.persistence.PokemonRepository;
import com.sergisalas.pokemonapi.service.dto.PokemonDetailResponse;
import com.sergisalas.pokemonapi.service.dto.PokemonListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PokemonSyncService {

    private static final int LIMIT = 1500;

    @Value("${pokeapi.base-url:https://pokeapi.co/api/v2}")
    private String POKEAPI_BASE_URL;

    private final RestClient restClient;
    private final PokemonRepository pokemonRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 */12 * * *")
    public void syncAllPokemons(){
        String listUrl = POKEAPI_BASE_URL + "/pokemon?limit=" + LIMIT;
        PokemonListResponse listResponse = restClient.get()
                .uri(listUrl)
                .retrieve()
                .body(PokemonListResponse.class);

        if (listResponse == null || listResponse.getResults() == null) {
            throw new RuntimeException("Failed to fetch Pokemon list from PokeAPI");
        }

        List<Pokemon> pokemons = listResponse.getResults().stream()
                .map(this::fetchAndMapPokemon)
                .filter(pokemon -> pokemon != null)
                .toList();

        pokemonRepository.saveAll(pokemons);
    }

    private Pokemon fetchAndMapPokemon(PokemonListResponse.PokemonBasic basicInfo) {
        try {
            String detailJson = restClient.get()
                    .uri(basicInfo.getUrl())
                    .retrieve()
                    .body(String.class);

            PokemonDetailResponse detail = objectMapper.readValue(detailJson, PokemonDetailResponse.class);

            Pokemon pokemon = new Pokemon();
            pokemon.setPokeApiId(detail.getId());
            pokemon.setName(detail.getName());
            pokemon.setWeight(detail.getWeight());
            pokemon.setHeight(detail.getHeight());
            pokemon.setBaseExperience(detail.getBase_experience());
            pokemon.setRawJson(detailJson);
            pokemon.setLastSynced(Instant.now());

            return pokemon;

        } catch (Exception e) {
            return null;
        }
    }
}
