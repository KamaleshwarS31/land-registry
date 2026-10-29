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
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        Optional<Parcel> parcel = parcelRepository.findById(id);
        if (parcel.isPresent()) {
            return parcel;
        }
        parcel = parcelRepository.findByParcelCode(id);
        if (parcel.isPresent()) {
            return parcel;
        }
        parcel = parcelRepository.findById(id.toLowerCase());
        if (parcel.isPresent()) {
            return parcel;
        }
        return parcelRepository.findByParcelCode(id.toUpperCase());
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
        String resolvedId = parcelId;
        Optional<Parcel> pOpt = getParcelById(parcelId);
        if (pOpt.isPresent()) {
            resolvedId = pOpt.get().getId();
        }

        List<ParcelLineage> parents = parcelLineageRepository.findByChildParcelId(resolvedId);
        List<ParcelLineage> children = parcelLineageRepository.findByParentParcelId(resolvedId);

        result.put("parcelId", resolvedId);
        result.put("parents", parents.stream().map(l -> {
            Map<String, Object> map = new HashMap<>();
            map.put("parcelId", l.getParentParcel() != null ? l.getParentParcel().getId() : "");
            map.put("parcelCode", l.getParentParcel() != null ? l.getParentParcel().getParcelCode() : "");
            map.put("relationType", l.getRelationType() != null ? l.getRelationType() : "");
            map.put("mutationId", l.getMutation() != null ? l.getMutation().getId() : "");
            return map;
        }).toList());
        result.put("children", children.stream().map(l -> {
            Map<String, Object> map = new HashMap<>();
            map.put("parcelId", l.getChildParcel() != null ? l.getChildParcel().getId() : "");
            map.put("parcelCode", l.getChildParcel() != null ? l.getChildParcel().getParcelCode() : "");
            map.put("relationType", l.getRelationType() != null ? l.getRelationType() : "");
            map.put("mutationId", l.getMutation() != null ? l.getMutation().getId() : "");
            return map;
        }).toList());

        return result;
    }
}
