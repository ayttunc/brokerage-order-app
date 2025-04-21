package com.brokerage.orderapp.service;

import com.brokerage.orderapp.entity.Asset;
import com.brokerage.orderapp.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "jwt.secret=test-secret",
        "jwt.expiration=3600000",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
@Transactional
class AssetServiceImplIT {

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetRepository assetRepository;

    @BeforeEach
    void setUp() {
        assetRepository.deleteAll();
        assetRepository.save(Asset.builder().customerId(100L).assetName("TRY").size(new BigDecimal("1000")).usableSize(new BigDecimal("1000")).build());
        assetRepository.save(Asset.builder().customerId(100L).assetName("ABC").size(new BigDecimal("500")).usableSize(new BigDecimal("500")).build());
        assetRepository.save(Asset.builder().customerId(101L).assetName("ABC").size(new BigDecimal("200")).usableSize(new BigDecimal("200")).build());
    }

    @Test
    void getAssetsByCustomer_returnsOnlyCustomerAssets() {
        List<Asset> result = assetService.getAssetsByCustomer(100L);
        assertEquals(2, result.size());
    }

    @Test
    void getAssetsByCustomerAndName_filtersCorrectly() {
        List<Asset> result = assetService.getAssetsByCustomerAndName(100L, "ABC");
        assertEquals(1, result.size());
        assertEquals("ABC", result.get(0).getAssetName());
    }
}
