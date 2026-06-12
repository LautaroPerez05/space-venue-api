package com.utn.space.venueaapi.model.records;

import java.math.BigDecimal;

public record SpaceFilterDTO(
        Integer idConsumerOwner,
        Integer idLocation,//Esta ubicacion esta por si el usuario no mada latitud ni longitud pero si una ubicacion especifica como la de un edificio o shopping
        String nameSpace,
        Double minPrice,
        Double maxPrice,
        BigDecimal lat, //Latitud del usuario
        BigDecimal lng, //Longitud del usuario
        BigDecimal radious)// Radio de busqueda
{}
