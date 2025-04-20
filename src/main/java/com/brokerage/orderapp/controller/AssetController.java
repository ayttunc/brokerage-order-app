package com.brokerage.orderapp.controller;

import com.brokerage.orderapp.entity.Asset;
import com.brokerage.orderapp.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<List<Asset>> getAssets(@RequestParam Long customerId,
                                                 @RequestParam(required = false) String assetName) {
        List<Asset> assets = (assetName != null)
                ? assetService.getAssetsByCustomerAndName(customerId, assetName)
                : assetService.getAssetsByCustomer(customerId);
        return ResponseEntity.ok(assets);
    }
}
