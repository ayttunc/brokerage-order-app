package com.brokerage.orderapp.service.impl;

import com.brokerage.orderapp.entity.Asset;
import com.brokerage.orderapp.repository.AssetRepository;
import com.brokerage.orderapp.service.impl.AssetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AssetServiceImplTest {
    @Mock
    private AssetRepository assetRepository;
    @InjectMocks
    private AssetServiceImpl assetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAssetsByCustomer_callsRepository() {
        Long customerId = 1L;
        List<Asset> assets = List.of(new Asset());
        when(assetRepository.findByCustomerId(customerId)).thenReturn(assets);

        List<Asset> result = assetService.getAssetsByCustomer(customerId);
        assertThat(result).isSameAs(assets);
        verify(assetRepository).findByCustomerId(customerId);
    }

    @Test
    void getAssetsByCustomerAndName_callsRepository() {
        Long customerId = 2L;
        String name = "XYZ";
        List<Asset> assets = List.of(new Asset());
        when(assetRepository.findByCustomerIdAndAssetName(customerId, name)).thenReturn(assets);

        List<Asset> result = assetService.getAssetsByCustomerAndName(customerId, name);
        assertThat(result).isSameAs(assets);
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, name);
    }
}