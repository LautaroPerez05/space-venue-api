package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpaceRepository extends JpaRepository<Space,Long> {
    /*
    List<Space> findAllByConsumerOwner_IdConsumer(Long idConsumer);
    List<Space> findAllByLocation_IdLocation(Long idLocation);
    List<Space> findAllByNameSpace(String nameSpace);
    List<Space> findAllByBasePriceBetween(Double minPrice, Double maxPrice);
    */
    //Dejo comentada la forma anterior de filtrar

    //Con este metodo puedo filtrar por varios parametros a la vez (id_consumer_owner, id_location, name_space, base_price) , los que no uso simplemente seran null
    //Estoy mapeando un objeto de clase Space que denomino con el alias "s" ya con el tipo de objto JPA ya sabe que tabla mirar
    @Query("SELECT s FROM Space s WHERE " +
            "( :name_space IS NULL OR LOWER(s.name_space) LIKE LOWER(CONCAT('%', :name_space, '%')) ) AND " +
            "( :minPrice IS NULL OR s.base_price >= :minPrice ) AND " +
            "( :maxPrice IS NULL OR s.base_price <= :maxPrice ) AND " +
            " :id_consumer_owner IS NULL OR s.id_consumer_owner = :id_consumer_owner AND " +
            ":id_location IS NULL OR s.id_location = :id_location"
    )
    List<Space> findAllByFields(
            @Param("id_consumer_owner") Long id_consumer_owner,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("name_space") Long name_space,
            @Param("id_location") Long id_location
    );
}
