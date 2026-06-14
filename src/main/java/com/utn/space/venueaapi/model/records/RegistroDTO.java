package com.utn.space.venueaapi.model.records;
//DTO para cundo un usuario se loggea por primera vez
public record RegistroDTO(
        // Datos del Consumer
         String firstname,
         String lastname,
         String email,
         String phone,
         //Datos de la crendencial
         String username,
         String password
) { }
