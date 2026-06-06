package com.utn.space.venueaapi.model.records;

import java.math.BigDecimal;

public record SpaceFilterDTO(
        Integer id_consumer_owner,
        Integer id_location,//Esta ubicacion esta por si el usuario no mada latitud ni longitud pero si una ubicacion especifica como la de un edificio o shopping
        String name_space,
        Double minPrice,
        Double maxPrice,
        BigDecimal lat, //Latitud del usuario
        BigDecimal lng, //Longitud del usuario
        BigDecimal radious) // Radio de busqueda
{}
