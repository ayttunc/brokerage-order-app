package com.brokerage.orderapp.service.impl;

import com.brokerage.orderapp.entity.Asset;
import com.brokerage.orderapp.repository.AssetRepository;
import com.brokerage.orderapp.service.AssetService;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

    @Override
    public List<Asset> getAssetsByCustomer(Long customerId) {
        return assetRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Asset> getAssetsByCustomerAndName(Long customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName);
    }
}