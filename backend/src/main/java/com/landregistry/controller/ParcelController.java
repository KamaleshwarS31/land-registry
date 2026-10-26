package com.landregistry.controller;

import com.landregistry.model.Parcel;
import com.landregistry.model.User;
import com.landregistry.repository.UserRepository;
import com.landregistry.service.ParcelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parcels")
public class ParcelController {

    private final ParcelService parcelService;
    private final UserRepository userRepository;

    public ParcelController(ParcelService parcelService, UserRepository userRepository) {
        this.parcelService = parcelService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Parcel>> getParcels(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Parcel> parcels = parcelService.searchParcels(query, status, PageRequest.of(page, size));
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Parcel> getParcelById(@PathVariable String id) {
        return parcelService.getParcelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-holdings")
    public ResponseEntity<List<Parcel>> getMyHoldings(Authentication authentication) {
        User user = userRepository.findByLogin(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(parcelService.getParcelsByOwner(user.getId()));
    }

    @GetMapping("/{id}/lineage")
    public ResponseEntity<Map<String, Object>> getParcelLineage(@PathVariable String id) {
        return ResponseEntity.ok(parcelService.getParcelLineageTree(id));
    }
}
