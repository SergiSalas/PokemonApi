package com.sergisalas.pokemonapi.service;

import com.sergisalas.pokemonapi.domain.Pokemon;
import com.sergisalas.pokemonapi.persistence.PokemonRepository;
import com.sergisalas.pokemonapi.service.dto.PokemonDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock
    private PokemonRepository pokemonRepository;

    @Mock
    private PokemonSyncService pokemonSyncService;

    @InjectMocks
    private PokemonService pokemonService;

    private Pokemon pokemon1;
    private Pokemon pokemon2;

    @BeforeEach
    void setUp() {
        pokemon1 = new Pokemon();
        pokemon1.setId("1");
        pokemon1.setName("Pikachu");
        pokemon1.setWeight(60);
        pokemon1.setHeight(4);
        pokemon1.setBaseExperience(112);

        pokemon2 = new Pokemon();
        pokemon2.setId("2");
        pokemon2.setName("Charizard");
        pokemon2.setWeight(905);
        pokemon2.setHeight(17);
        pokemon2.setBaseExperience(240);
    }

    @Test
    void getHeaviestPokemon_shouldReturnPokemonList() {
        // Given
        int numPokemon = 2;
        List<Pokemon> mockPokemons = Arrays.asList(pokemon2, pokemon1);
        when(pokemonRepository.findTopPokemonByWeight(PageRequest.of(0, numPokemon)))
                .thenReturn(mockPokemons);

        // When
        List<PokemonDto> result = pokemonService.getHeaviestPokemon(numPokemon);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(pokemonRepository, times(1)).findTopPokemonByWeight(any(PageRequest.class));
    }

    @Test
    void getHeaviestPokemon_shouldThrowException_whenRepositoryFails() {
        // Given
        when(pokemonRepository.findTopPokemonByWeight(any(PageRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> pokemonService.getHeaviestPokemon(5));
    }

    @Test
    void getHighestPokemon_shouldReturnPokemonList() {
        // Given
        int numPokemon = 2;
        List<Pokemon> mockPokemons = Arrays.asList(pokemon2, pokemon1);
        when(pokemonRepository.findTopPokemonByHeight(PageRequest.of(0, numPokemon)))
                .thenReturn(mockPokemons);

        // When
        List<PokemonDto> result = pokemonService.getHighestPokemon(numPokemon);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(pokemonRepository, times(1)).findTopPokemonByHeight(any(PageRequest.class));
    }

    @Test
    void getHighestPokemon_shouldThrowException_whenRepositoryFails() {
        // Given
        when(pokemonRepository.findTopPokemonByHeight(any(PageRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> pokemonService.getHighestPokemon(5));
    }

    @Test
    void getHighestExperiencesPokemon_shouldReturnPokemonList() {
        // Given
        int numPokemon = 2;
        List<Pokemon> mockPokemons = Arrays.asList(pokemon2, pokemon1);
        when(pokemonRepository.findTopPokemonByBaseExperience(PageRequest.of(0, numPokemon)))
                .thenReturn(mockPokemons);

        // When
        List<PokemonDto> result = pokemonService.getHighestExperiencesPokemon(numPokemon);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(pokemonRepository, times(1)).findTopPokemonByBaseExperience(any(PageRequest.class));
    }

    @Test
    void getHighestExperiencesPokemon_shouldThrowException_whenRepositoryFails() {
        // Given
        when(pokemonRepository.findTopPokemonByBaseExperience(any(PageRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> pokemonService.getHighestExperiencesPokemon(5));
    }

    @Test
    void syncDataBase_shouldCallSyncService() throws Exception {
        // Given
        doNothing().when(pokemonSyncService).syncAllPokemons();

        // When
        pokemonService.syncDataBase();

        // Then
        verify(pokemonSyncService, times(1)).syncAllPokemons();
    }

    @Test
    void syncDataBase_shouldThrowException_whenSyncFails() throws Exception {
        // Given
        doThrow(new RuntimeException("Sync error")).when(pokemonSyncService).syncAllPokemons();

        // When & Then
        assertThrows(RuntimeException.class, () -> pokemonService.syncDataBase());
    }
}

