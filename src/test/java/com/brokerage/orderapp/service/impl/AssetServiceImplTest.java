package com.brokerage.orderapp.service.impl;

import com.brokerage.orderapp.entity.Asset;
import com.brokerage.orderapp.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceImplTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetServiceImpl assetService;

    @Test
    void getAssetsByCustomer_returnsList() {
        List<Asset> expected = List.of(
                Asset.builder().id(1L).customerId(100L).assetName("TRY").size(BigDecimal.TEN).usableSize(BigDecimal.TEN).build(),
                Asset.builder().id(2L).customerId(100L).assetName("ABC").size(BigDecimal.ONE).usableSize(BigDecimal.ONE).build()
        );
        when(assetRepository.findByCustomerId(100L)).thenReturn(expected);
        List<Asset> result = assetService.getAssetsByCustomer(100L);
        assertEquals(expected, result);
        verify(assetRepository).findByCustomerId(100L);
    }

    @Test
    void getAssetsByCustomerAndName_returnsList() {
        List<Asset> expected = List.of(
                Asset.builder().id(3L).customerId(100L).assetName("ABC").size(BigDecimal.ONE).usableSize(BigDecimal.ONE).build()
        );
        when(assetRepository.findByCustomerIdAndAssetName(100L, "ABC")).thenReturn(expected);
        List<Asset> result = assetService.getAssetsByCustomerAndName(100L, "ABC");
        assertEquals(expected, result);
        verify(assetRepository).findByCustomerIdAndAssetName(100L, "ABC");
    }
}