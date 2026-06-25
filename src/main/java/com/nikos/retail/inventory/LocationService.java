package com.nikos.retail.inventory;

import java.util.List;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nikos.retail.common.exception.ResourceNotFoundException;

@Service
public class LocationService {
    private final LocationRepository locationRepository;


    public LocationService(LocationRepository locationRepository){
        this.locationRepository = locationRepository;
    }

    public LocationResponse getLocationById(Long id){
        Location location = locationRepository.findById(id)
            .orElseThrow(()-> new ResourceNotFoundException("Location does not exist with id: "+id));
        return LocationResponse.fromEntity(location);
    }

    public List<LocationResponse> getAllLocations(){
        return locationRepository.findAll()
            .stream()
            .map(LocationResponse::fromEntity)
            .collect(Collectors.toList());
    }

    public LocationResponse createLocation(LocationRequest request){
        if (locationRepository.findByName(request.getName()).isPresent()){
            throw new IllegalStateException("Location with this name already exists.");
        }

        Location location = new Location();
        location.setName(request.getName());
        location.setType(request.getType());

        Location saved = locationRepository.save(location);
        return LocationResponse.fromEntity(saved);
    }

}
