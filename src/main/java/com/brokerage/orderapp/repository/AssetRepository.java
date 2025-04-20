package com.brokerage.orderapp.repository;

import com.brokerage.orderapp.entity.Asset;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByCustomerId(Long customerId);

    List<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName);
}
