package com.brokerage.orderapp.controller;

import com.brokerage.orderapp.entity.Asset;
import com.brokerage.orderapp.service.AssetService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId.toString() == principal.username)")
    public ResponseEntity<List<Asset>> getAssets(@RequestParam @NotNull(message = "Customer ID is required") Long customerId) {
        List<Asset> assets = assetService.getAssetsByCustomer(customerId);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/{assetName}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId.toString() == principal.username)")
    public ResponseEntity<List<Asset>> getAssetsByName(
            @RequestParam @NotNull(message = "Customer ID is required") Long customerId,
            @PathVariable String assetName
    ) {
        List<Asset> assets = assetService.getAssetsByCustomerAndName(customerId, assetName);
        return ResponseEntity.ok(assets);
    }
}
