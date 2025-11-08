package com.sergisalas.pokemonapi.web;

import com.sergisalas.pokemonapi.domain.Pokemon;
import com.sergisalas.pokemonapi.service.PokemonService;
import com.sergisalas.pokemonapi.service.dto.PokemonDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PokemonControllerTest {

    @Mock
    private PokemonService pokemonService;

    @InjectMocks
    private PokemonController pokemonController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pokemonController).build();
    }

    @Test
    void getHighest_shouldReturnPokemonList() throws Exception {
        // Given
        List<PokemonDto> pokemonsList = Arrays.asList(
                createPokemonDto("onix", 87, null, null),
                createPokemonDto("steelix", 92, null, null)
        );
        when(pokemonService.getHighestPokemon(anyInt())).thenReturn(pokemonsList);

        // When & Then
        mockMvc.perform(get("/pokemon/highest")
                        .param("numPokemon", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(pokemonService, times(1)).getHighestPokemon(2);
    }

    @Test
    void getHeaviest_shouldReturnPokemonList() throws Exception {
        // Given
        List<PokemonDto> pokemons = Arrays.asList(
                createPokemonDto("snorlax", null, 4600, null),
                createPokemonDto("groudon", null, 9500, null)
        );
        when(pokemonService.getHeaviestPokemon(anyInt())).thenReturn(pokemons);

        // When & Then
        mockMvc.perform(get("/pokemon/heaviest")
                        .param("numPokemon", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(pokemonService, times(1)).getHeaviestPokemon(2);
    }

    @Test
    void getHighestExperience_shouldReturnPokemonList() throws Exception {
        // Given
        List<PokemonDto> pokemons = Arrays.asList(
                createPokemonDto("blissey", null, null, 608),
                createPokemonDto("chansey", null, null, 395)
        );
        when(pokemonService.getHighestExperiencesPokemon(anyInt())).thenReturn(pokemons);

        // When & Then
        mockMvc.perform(get("/pokemon/highestExperience")
                        .param("numPokemon", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(pokemonService, times(1)).getHighestExperiencesPokemon(2);
    }

    @Test
    void syncDataBase_shouldCallServiceSync() throws Exception {
        // Given
        doNothing().when(pokemonService).syncDataBase();

        // When & Then
        mockMvc.perform(post("/pokemon/sync"))
                .andExpect(status().isNoContent());

        verify(pokemonService, times(1)).syncDataBase();
    }

    private PokemonDto createPokemonDto(String name, Integer height, Integer weight, Integer baseExperience) {
        Pokemon pokemon = createPokemon(name, height, weight, baseExperience);
        return new PokemonDto(pokemon);
    }

    private Pokemon createPokemon(String name, Integer height, Integer weight, Integer baseExperience) {
        Pokemon pokemon = new Pokemon();
        pokemon.setName(name);
        if (height != null) pokemon.setHeight(height);
        if (weight != null) pokemon.setWeight(weight);
        if (baseExperience != null) pokemon.setBaseExperience(baseExperience);
        return pokemon;
    }

}
