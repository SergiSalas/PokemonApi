package com.sergisalas.pokemonapi.persistence;

import com.sergisalas.pokemonapi.domain.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Long> {
    Optional<Pokemon> findByPokeApiId(Integer pokeapiId);

    @Query("SELECT p FROM Pokemon p ORDER BY p.weight DESC")
    List<Pokemon> findTopPokemonByWeight(Pageable pageable);

    @Query("SELECT p FROM Pokemon p ORDER BY p.height DESC")
    List<Pokemon> findTopPokemonByHeight(Pageable pageable);

    @Query("SELECT p FROM Pokemon p ORDER BY p.baseExperience DESC")
    List<Pokemon> findTopPokemonByBaseExperience(Pageable pageable);

}