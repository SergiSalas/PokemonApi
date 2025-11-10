package com.sergisalas.pokemonapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergisalas.pokemonapi.persistence.PokemonRepository;
import com.sergisalas.pokemonapi.service.dto.PokemonDetailResponse;
import com.sergisalas.pokemonapi.service.dto.PokemonListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonSyncServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private PokemonRepository pokemonRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PokemonSyncService pokemonSyncService;

    private PokemonListResponse pokemonListResponse;
    private PokemonDetailResponse detailResponse1;
    private String detailJson1;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(pokemonSyncService, "POKEAPI_BASE_URL", "https://pokeapi.co/api/v2");

        PokemonListResponse.PokemonBasic pokemonBasic1 = new PokemonListResponse.PokemonBasic();
        pokemonBasic1.setName("pikachu");
        pokemonBasic1.setUrl("https://pokeapi.co/api/v2/pokemon/25/");

        PokemonListResponse.PokemonBasic pokemonBasic2 = new PokemonListResponse.PokemonBasic();
        pokemonBasic2.setName("charizard");
        pokemonBasic2.setUrl("https://pokeapi.co/api/v2/pokemon/6/");

        pokemonListResponse = new PokemonListResponse();
        pokemonListResponse.setCount(2);
        pokemonListResponse.setResults(Arrays.asList(pokemonBasic1, pokemonBasic2));

        detailResponse1 = new PokemonDetailResponse();
        detailResponse1.setId(25);
        detailResponse1.setName("pikachu");
        detailResponse1.setWeight(60);
        detailResponse1.setHeight(4);
        detailResponse1.setBase_experience(112);

        detailJson1 = "{\"id\":25,\"name\":\"pikachu\"}";
    }

    @Test
    void syncAllPokemons_shouldSyncSuccessfully() throws Exception {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.body(PokemonListResponse.class)).thenReturn(pokemonListResponse);
        when(responseSpec.body(String.class)).thenReturn(detailJson1);

        when(objectMapper.readValue(anyString(), eq(PokemonDetailResponse.class)))
                .thenReturn(detailResponse1);

        when(pokemonRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // When
        pokemonSyncService.syncAllPokemons();

        // Then
        verify(restClient, atLeastOnce()).get();
        verify(pokemonRepository, times(1)).saveAll(anyList());
    }


    @Test
    void syncAllPokemons_shouldThrowException_whenApiClientFails() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PokemonListResponse.class)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> pokemonSyncService.syncAllPokemons());
        verify(pokemonRepository, never()).saveAll(anyList());
    }
    @Test
    void syncAllPokemons_shouldContinue_whenOneDetailFails() throws Exception {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.body(PokemonListResponse.class)).thenReturn(pokemonListResponse);

        when(responseSpec.body(String.class))
                .thenThrow(new RuntimeException("Error al obtener detalle"))
                .thenReturn(detailJson1);

        when(objectMapper.readValue(anyString(), eq(PokemonDetailResponse.class)))
                .thenReturn(detailResponse1);

        when(pokemonRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // When
        pokemonSyncService.syncAllPokemons();

        // Then
        verify(pokemonRepository, times(1)).saveAll(anyList());
        verify(responseSpec, times(2)).body(String.class);
    }

    @Test
    void syncAllPokemons_shouldThrowException_whenResultsIsNull() {
        // Given
        PokemonListResponse emptyResponse = new PokemonListResponse();
        emptyResponse.setResults(null);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PokemonListResponse.class)).thenReturn(emptyResponse);

        // When & Then
        assertThrows(RuntimeException.class, () -> pokemonSyncService.syncAllPokemons());
        verify(pokemonRepository, never()).saveAll(anyList());
    }

    @Test
    void syncAllPokemons_shouldSaveAllPokemons_whenAllDetailsAreValid() throws Exception {
        // Given
        String detailJson2 = "{\"id\":6,\"name\":\"charizard\"}";
        PokemonDetailResponse detailResponse2 = new PokemonDetailResponse();
        detailResponse2.setId(6);
        detailResponse2.setName("charizard");
        detailResponse2.setWeight(905);
        detailResponse2.setHeight(17);
        detailResponse2.setBase_experience(240);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.body(PokemonListResponse.class)).thenReturn(pokemonListResponse);
        when(responseSpec.body(String.class))
                .thenReturn(detailJson1)
                .thenReturn(detailJson2);

        when(objectMapper.readValue(detailJson1, PokemonDetailResponse.class)).thenReturn(detailResponse1);
        when(objectMapper.readValue(detailJson2, PokemonDetailResponse.class)).thenReturn(detailResponse2);

        when(pokemonRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // When
        pokemonSyncService.syncAllPokemons();

        // Then
        verify(pokemonRepository, times(1)).saveAll(anyList());
        verify(responseSpec, times(2)).body(String.class);
    }


}

