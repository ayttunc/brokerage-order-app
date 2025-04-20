package com.brokerage.orderapp.service;

import com.brokerage.orderapp.entity.Asset;
import java.util.List;

public interface AssetService {
    List<Asset> getAssetsByCustomer(Long customerId);
    List<Asset> getAssetsByCustomerAndName(Long customerId, String assetName);
}