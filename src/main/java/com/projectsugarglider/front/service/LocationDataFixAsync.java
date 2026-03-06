package com.projectsugarglider.front.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.projectsugarglider.kca.entity.KcaStoreInfoEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationDataFixAsync {

    private final LocationDataFixWorker locationDataFixWorker;

    @Async("locationFixExecutor")
    public CompletableFuture<Integer> processRecordAsync(KcaStoreInfoEntity record) {
        try {
            boolean updated = locationDataFixWorker.processRecord(record);
            if (updated) {
                return CompletableFuture.completedFuture(1); // success
            }
            return CompletableFuture.completedFuture(0);     // fail
        } catch (Exception e) {
            log.warn("보정 실패 entp='{}'", record.getEntpName(), e);
            return CompletableFuture.completedFuture(-1);    // error
        }
    }
}