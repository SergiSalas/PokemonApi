package com.sergisalas.pokemonapi.service;

import com.sergisalas.pokemonapi.domain.Pokemon;
import com.sergisalas.pokemonapi.persistence.PokemonRepository;
import com.sergisalas.pokemonapi.service.dto.PokemonDto;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PokemonService {

    private final PokemonRepository pokemonRepository;
    private final PokemonSyncService pokemonSyncService;

    public List<PokemonDto> getHeaviestPokemon(int numPokemon) {
        try {
            List<Pokemon> response = this.pokemonRepository.findTopPokemonByWeight(PageRequest.of(0,numPokemon));
            return response.stream()
                    .map(PokemonDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<PokemonDto> getHighestPokemon(int numPokemon) {
        try {
            List<Pokemon> response = this.pokemonRepository.findTopPokemonByHeight(PageRequest.of(0,numPokemon));
            return response.stream()
                    .map(PokemonDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<PokemonDto> getHighestExperiencesPokemon(int numPokemon) {
        try {
            List<Pokemon> response = this.pokemonRepository.findTopPokemonByBaseExperience(PageRequest.of(0,numPokemon));
            return response.stream()
                    .map(PokemonDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void syncDataBase (){
        try {
            this.pokemonSyncService.syncAllPokemons();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
