package com.sergisalas.pokemonapi.integration;

import com.sergisalas.pokemonapi.domain.Pokemon;
import com.sergisalas.pokemonapi.persistence.PokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PokemonIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PokemonRepository pokemonRepository;

    @BeforeEach
    void setUp() {
        pokemonRepository.deleteAll();

        // Datos de prueba
        pokemonRepository.save(createPokemon("pikachu", 4, 60, 112));
        pokemonRepository.save(createPokemon("charizard", 17, 905, 240));
        pokemonRepository.save(createPokemon("blastoise", 16, 855, 239));
        pokemonRepository.save(createPokemon("venusaur", 20, 1000, 236));
        pokemonRepository.save(createPokemon("snorlax", 21, 4600, 189));
    }

    @Test
    void getHighest_shouldReturnHighestPokemon() throws Exception {
        mockMvc.perform(get("/pokemon/highest")
                        .param("numPokemon", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is("snorlax")))
                .andExpect(jsonPath("$[0].height", is(21)))
                .andExpect(jsonPath("$[1].name", is("venusaur")))
                .andExpect(jsonPath("$[2].name", is("charizard")));
    }

    @Test
    void getHeaviest_shouldReturnHeaviestPokemon() throws Exception {
        mockMvc.perform(get("/pokemon/heaviest")
                        .param("numPokemon", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("snorlax")))
                .andExpect(jsonPath("$[0].weight", is(4600)))
                .andExpect(jsonPath("$[1].name", is("venusaur")));
    }

    @Test
    void getHighestExperience_shouldReturnHighestExperiencePokemon() throws Exception {
        mockMvc.perform(get("/pokemon/highestExperience")
                        .param("numPokemon", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is("charizard")))
                .andExpect(jsonPath("$[0].baseExperience", is(240)))
                .andExpect(jsonPath("$[1].baseExperience", is(239)))
                .andExpect(jsonPath("$[2].baseExperience", is(236)));
    }

    @Test
    void getHighest_withInvalidParameter_shouldHandleError() throws Exception {
        mockMvc.perform(get("/pokemon/highest")
                        .param("numPokemon", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void syncDataBase_shouldSyncSuccessfully() throws Exception {
        mockMvc.perform(post("/pokemon/sync"))
                .andExpect(status().isNoContent());
    }

    private Pokemon createPokemon(String name, Integer height, Integer weight, Integer baseExperience) {
        Pokemon pokemon = new Pokemon();
        pokemon.setName(name);
        pokemon.setHeight(height);
        pokemon.setWeight(weight);
        pokemon.setBaseExperience(baseExperience);
        return pokemon;
    }
}

