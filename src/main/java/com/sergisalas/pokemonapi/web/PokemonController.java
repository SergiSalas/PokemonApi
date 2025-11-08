package com.sergisalas.pokemonapi.web;

import com.sergisalas.pokemonapi.service.PokemonService;
import com.sergisalas.pokemonapi.service.dto.PokemonDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Pokemon", description = "API de gestión de Pokémon")
@RequestMapping("/pokemon")
@RestController
@AllArgsConstructor
@Validated
public class PokemonController {

    private final PokemonService pokemonService;

    @Operation(
            summary = "Get the tallest Pokemon",
            description = "Returns a list of the N tallest Pokemon sorted by height"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PokemonDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter (numPokemon must be >= 1)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/highest")
    public ResponseEntity<List<PokemonDto>> getHighest(
            @Parameter(description = "Number of Pokemon to return", example = "10")
            @RequestParam @Min(value = 1, message = "Number most be higher than 0") Integer numPokemon) {
        return ResponseEntity.ok(this.pokemonService.getHighestPokemon(numPokemon));
    }

    @Operation(
            summary = "Get the heaviest Pokemon",
            description = "Returns a list of the N heaviest Pokemon sorted by weight"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PokemonDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter (numPokemon must be >= 1)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/heaviest")
    public ResponseEntity<List<PokemonDto>> getHeaviest(
            @Parameter(description = "Number of Pokemon to return", example = "10")
            @RequestParam @Min(value = 1, message = "Number most be higher than 0") Integer numPokemon) {
        return ResponseEntity.ok(this.pokemonService.getHeaviestPokemon(numPokemon));
    }

    @Operation(
            summary = "Get Pokemon with highest experience",
            description = "Returns a list of the N Pokemon with highest base experience"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PokemonDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter (numPokemon must be >= 1)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/highestExperience")
    public ResponseEntity<List<PokemonDto>> getHighestExperience(
            @Parameter(description = "Number of Pokemon to return", example = "10")
            @RequestParam @Min(value = 1, message = "Number most be higher than 0") Integer numPokemon) {
        return ResponseEntity.ok(this.pokemonService.getHighestExperiencesPokemon(numPokemon));
    }

    @Operation(
            summary = "Synchronize database",
            description = "Synchronizes the database with information from the Pokemon API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Synchronization completed"),
            @ApiResponse(responseCode = "500", description = "Synchronization error")
    })
    @PostMapping("/sync")
    public ResponseEntity<Void> syncDataBase() {
        this.pokemonService.syncDataBase();
        return ResponseEntity.noContent().build();
    }
}
