package com.landregistry.service;

import com.landregistry.model.Parcel;
import com.landregistry.model.ParcelLineage;
import com.landregistry.repository.ParcelLineageRepository;
import com.landregistry.repository.ParcelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ParcelService {

    private final ParcelRepository parcelRepository;
    private final ParcelLineageRepository parcelLineageRepository;

    public ParcelService(ParcelRepository parcelRepository, ParcelLineageRepository parcelLineageRepository) {
        this.parcelRepository = parcelRepository;
        this.parcelLineageRepository = parcelLineageRepository;
    }

    @Transactional(readOnly = true)
    public Page<Parcel> searchParcels(String query, String status, Pageable pageable) {
        String q = (query != null && !query.isBlank()) ? query.trim() : null;
        String s = (status != null && !status.isBlank()) ? status.trim() : null;
        return parcelRepository.searchParcels(q, s, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Parcel> getParcelById(String id) {
        return parcelRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Parcel> getParcelByCode(String code) {
        return parcelRepository.findByParcelCode(code);
    }

    @Transactional(readOnly = true)
    public List<Parcel> getParcelsByOwner(String ownerId) {
        return parcelRepository.findByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getParcelLineageTree(String parcelId) {
        Map<String, Object> result = new HashMap<>();
        List<ParcelLineage> parents = parcelLineageRepository.findByChildParcelId(parcelId);
        List<ParcelLineage> children = parcelLineageRepository.findByParentParcelId(parcelId);

        result.put("parcelId", parcelId);
        result.put("parents", parents.stream().map(l -> Map.of(
                "parcelId", l.getParentParcel().getId(),
                "parcelCode", l.getParentParcel().getParcelCode(),
                "relationType", l.getRelationType(),
                "mutationId", l.getMutation().getId()
        )).toList());
        result.put("children", children.stream().map(l -> Map.of(
                "parcelId", l.getChildParcel().getId(),
                "parcelCode", l.getChildParcel().getParcelCode(),
                "relationType", l.getRelationType(),
                "mutationId", l.getMutation().getId()
        )).toList());

        return result;
    }
}
