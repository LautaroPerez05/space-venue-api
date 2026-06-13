package com.utn.space.venueaapi.service;
import com.utn.space.venueaapi.exceptions.IdNotFoundException;
import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.model.Location;
import com.utn.space.venueaapi.model.records.SpaceDTO;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.records.SpaceFilterDTO;
import com.utn.space.venueaapi.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class SpaceService {
    @Autowired
    SpaceRepository spaceRepository;
    @Autowired
    ConsumerService consumerService;
    @Autowired
    LocationService locationService;
    @Autowired
    CancellationPolicyService cancellationPolicyService;
    @Autowired
    GoogleCalendarService googleCalendarService;


    public List<Space> findAll(){
        return spaceRepository.findAll();
    }

    public List<Space> findAllActives(){
        return spaceRepository.findAllWithOutInactives();
    }

    public Boolean existsById(Integer id){
        return spaceRepository.existsByIdSpaceAndIsActiveTrue(id); //Modifique esta logica para no buscar espacios inactivos
    }

    public Space findById(Integer id){
        return spaceRepository.findById(id).orElseThrow(()-> new IdNotFoundException("Space", id));
    }

    public void deleteById(Integer id){
        if(!spaceRepository.existsById(id)){
            throw new IdNotFoundException("Space", id);
        }
        spaceRepository.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertSpace(SpaceDTO spaceDTO){
        if(spaceDTO.nameSpace().isBlank()){
            throw new InvalidDataException("Por favor ingrese un nombre para su espacio");
        }

        if(spaceDTO.description().isBlank()){
            throw new InvalidDataException("Por favor ingrese una descripcion para su espacio");
        }

        if(spaceDTO.basePrice().compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidDataException("Por favor ingrese un precio valido");
        }

        Space spaceToInsert = new Space(
                null,
                consumerService.findById(spaceDTO.idConsumerOwner()),
                locationService.findById(spaceDTO.idLocation()),
                cancellationPolicyService.findById(spaceDTO.idCancellationPolicies()),
                spaceDTO.googleCalendarId(),
                spaceDTO.nameSpace(),
                spaceDTO.description(),
                spaceDTO.basePrice(),
                spaceDTO.publicationDate(),
                spaceDTO.bufferTime(),
                true);

        spaceRepository.save(spaceToInsert);
    }


    @Transactional(rollbackFor = Exception.class)
    public void modifySpace(Integer id, SpaceDTO spaceDTO){
        if(!spaceRepository.existsById(id)){
            throw new IdNotFoundException("Space: ", id);
        }

        if(spaceDTO.nameSpace().isBlank()){
            throw new InvalidDataException("Por favor ingrese un nombre para el espacio");
        }

        if(spaceDTO.description().isBlank()){
            throw new InvalidDataException("Por favor ingrese una descripcion para el espacio");
        }

        if(spaceDTO.basePrice().compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidDataException("Por favor ingrese un precio valido");
        }

        Space spaceToInsert = new Space(
                id,
                consumerService.findById(spaceDTO.idConsumerOwner()),
                locationService.findById(spaceDTO.idLocation()),
                cancellationPolicyService.findById(spaceDTO.idCancellationPolicies()),
                spaceDTO.googleCalendarId(),
                spaceDTO.nameSpace(),
                spaceDTO.description(),
                spaceDTO.basePrice(),
                spaceDTO.publicationDate(),
                spaceDTO.bufferTime());
               // spaceDTO.isActive());// <--Con esta parte el Admin puede verificar espacios, Fede: para que en una DTO?

        spaceRepository.save(spaceToInsert);
    }

    //Este metodo maneja solo espacios activos
    public List<Space> findAllByFields(SpaceFilterDTO spaceFilterDTO){
        if((spaceFilterDTO.idConsumerOwner() != null) && !consumerService.existsById(spaceFilterDTO.idConsumerOwner())){
            throw new IdNotFoundException("Consumer",spaceFilterDTO.idConsumerOwner());
        }

        if((spaceFilterDTO.idLocation() != null) && !locationService.existsById(spaceFilterDTO.idLocation())){
            throw new IdNotFoundException("Location",spaceFilterDTO.idLocation());
        }

        //Filtro inicial de mi base de datos
        List<Space> spaces = spaceRepository.findAllByFields(
                spaceFilterDTO.idConsumerOwner(),
                spaceFilterDTO.minPrice(),
                spaceFilterDTO.maxPrice(),
                spaceFilterDTO.nameSpace(),
                spaceFilterDTO.idLocation());//Sigo filtrando por localizacion para poder filtrar por lugares como un shpping.

        //Hago un filtro por proximidad al usuario, solo si este mando latitud y longitud
        if(spaceFilterDTO.lat() != null && spaceFilterDTO.lng() != null){
            //Si no encuentro un radio de filtrado en el DTO pongo 5Km de base
            BigDecimal maxRadious = spaceFilterDTO.radious() != null ? spaceFilterDTO.radious() : new BigDecimal("5.0");
            //Uso isSpaceNearBy para filtrar la lista de espacios, primero filtro los espacios que tengan datos de ubicacion incompletos para evitar errores
            spaces = spaces.stream()
                    .filter(space ->space.getLocation()!= null && space.getLocation().getLatitude() != null && space.getLocation().getLongitude() != null)
                    .filter(space -> locationService.isSpaceNearby(spaceFilterDTO.lat(),spaceFilterDTO.lng(),maxRadious,space))
                    .toList();
        }

        return spaces;
    }


    //Este metodo considera espacios inactivos
    public List<Space> findAllByFieldsWithInactives(SpaceFilterDTO spaceFilterDTO){
        if((spaceFilterDTO.idConsumerOwner() != null) && !consumerService.existsById(spaceFilterDTO.idConsumerOwner())){
            throw new IdNotFoundException("Consumer",spaceFilterDTO.idConsumerOwner());
        }

        if((spaceFilterDTO.idLocation() != null) && !locationService.existsById(spaceFilterDTO.idLocation())){
            throw new IdNotFoundException("Location",spaceFilterDTO.idLocation());
        }

        //Filtro inicial de mi base de datos
        List<Space> spaces = spaceRepository.findAllByFieldsWithInactives(
                spaceFilterDTO.idConsumerOwner(),
                spaceFilterDTO.minPrice(),
                spaceFilterDTO.maxPrice(),
                spaceFilterDTO.nameSpace(),
                spaceFilterDTO.idLocation());//Sigo filtrando por localizacion para poder filtrar por lugares como un shpping.

        //Hago un filtro por proximidad al usuario, solo si este mando latitud y longitud
        if(spaceFilterDTO.lat() != null && spaceFilterDTO.lng() != null){
            //Si no encuentro un radio de filtrado en el DTO pongo 5Km de base
            BigDecimal maxRadious = spaceFilterDTO.radious() != null ? spaceFilterDTO.radious() : new BigDecimal("5.0");
            //Uso isSpaceNearBy para filtrar la lista de espacios, primero filtro los espacios que tengan datos de ubicacion incompletos para evitar errores
            spaces = spaces.stream()
                    .filter(space ->space.getLocation()!= null && space.getLocation().getLatitude() != null && space.getLocation().getLongitude() != null)
                    .filter(space -> locationService.isSpaceNearby(spaceFilterDTO.lat(),spaceFilterDTO.lng(),maxRadious,space))
                    .toList();
        }

        return spaces;
    }

    public List<Space> findAllForOwner(){
        Integer loggedOwnerId = consumerService.getLoggedConsumerId();
        SpaceFilterDTO auxDTO = new SpaceFilterDTO(
                loggedOwnerId,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );//Creo un nuevo record porque no se pueden modificar directamente

        return findAllByFieldsWithInactives(auxDTO);
    }

    public List<Space> findAllByFieldsForOwner(SpaceFilterDTO spaceFilterDTO){
        Integer loggedOwnerId = consumerService.getLoggedConsumerId();
        SpaceFilterDTO auxDTO = new SpaceFilterDTO(
                loggedOwnerId,
                spaceFilterDTO.idLocation(),
                spaceFilterDTO.nameSpace(),
                spaceFilterDTO.minPrice(),
                spaceFilterDTO.maxPrice(),
                spaceFilterDTO.lat(),
                spaceFilterDTO.lng(),
                spaceFilterDTO.radious()
        );//Creo un nuevo record porque no se pueden modificar directamente

        return findAllByFieldsWithInactives(auxDTO);
    }

    public void deleteOwnedSpace(Integer id){
        Integer loggedOwnerId = consumerService.getLoggedConsumerId();
        Space spaceToDelete = findById(id);

        if(!Objects.equals(spaceToDelete.getConsumerOwner().getIdConsumer(), loggedOwnerId)){
            throw new InvalidDataException("Debe ser duenio de el espacio que desdea eliminar");
        }
        deleteById(id);
    }

    public void insertOwnedSpace(SpaceDTO spaceDTO) {
        Space space = new Space();

        // Mapeas los campos normales del DTO...
        space.setNameSpace(spaceDTO.nameSpace());
        space.setDescription(spaceDTO.description());
        space.setBasePrice(spaceDTO.basePrice());
        space.setBufferTime(spaceDTO.bufferTime());
        space.setIsActive(true); //Lo dejo en true por default para no comerme le coco

        // 1. FECHA AUTOMÁTICA: Asignamos la fecha del día de hoy del servidor
        space.setPublicationDate(java.time.LocalDate.now());

        // 2. GOOGLE CALENDAR ID:
        //ESTA PARTE HAY QUE CONFIGURARLA
        String idAutomatico = "cal_" + java.util.UUID.randomUUID().toString();
        space.setGoogleCalendarId(idAutomatico);

        // 3. LEAFLET
        //ESTA PARTE HAY QUE CONFIGURARLA
        space.setLocation(new Location(1,"Calle1","12334","Muncipalidad","7600","MDQ",BigDecimal.TEN,BigDecimal.TEN));

        space.setConsumerOwner(consumerService.findById(consumerService.getLoggedConsumerId()));
        spaceRepository.save(space);
    }

    public void modifyOwnedSpace(Integer id, SpaceDTO spaceDTO){
        Integer loggedOwnerId = consumerService.getLoggedConsumerId();
        Space spaceToModify = findById(id);

        if(!Objects.equals(spaceToModify.getConsumerOwner().getIdConsumer(), loggedOwnerId)){
            throw new InvalidDataException("Debe ser duenio de el espacio que desea modificar");
        }

        SpaceDTO spaceDTOAux = new SpaceDTO(
                id,
                spaceDTO.idConsumerOwner(),
                spaceDTO.idLocation(),
                spaceDTO.idCancellationPolicies(),
                spaceDTO.googleCalendarId(),
                spaceDTO.nameSpace(),
                spaceDTO.description(),
                spaceDTO.basePrice(),
                spaceDTO.publicationDate(),
                spaceDTO.bufferTime(),
                false //De base cualquier modificacion hace que el espacio requiera una nueva verificacion
        );

        modifySpace(id, spaceDTOAux);
    }

}