package com.projectsugarglider.datainitialize.orchestrator.steps;

import org.springframework.stereotype.Component;

import com.projectsugarglider.datainitialize.orchestrator.BasicDataContext;
import com.projectsugarglider.datainitialize.orchestrator.BasicDataStep;
import com.projectsugarglider.datainitialize.orchestrator.BasicDataStepResult;
import com.projectsugarglider.kca.service.KcaLocationData;
import com.projectsugarglider.kca.service.KcaProductInfoService;
import com.projectsugarglider.kca.service.KcaStandardDataSaveService;
import com.projectsugarglider.kca.service.KcaStoreInfoSaveService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KcaBasicDataStep implements BasicDataStep {

    private final KcaStandardDataSaveService KcaService;
    private final KcaLocationData nullData;
    private final KcaStoreInfoSaveService InfoService;
    private final KcaProductInfoService priceInfo;

    @Override
    public String name() {
        return "KCA_BASIC_DATA_UPDATE";
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public BasicDataStepResult run(BasicDataContext context) {
        long start = System.currentTimeMillis();
        try {
            KcaService.saveUnitData();
            KcaService.saveEntpData();
            KcaService.saveTotalData();
            nullData.insertData();
            InfoService.saveStoreInfoData();
            priceInfo.SaveProductInfoData();

            long took = System.currentTimeMillis() - start;
            return BasicDataStepResult.ok(name(), took, "KCA 기본데이터(단위/업체/분류/지역보정/매장/상품) 업데이트 완료");
        } catch (Exception e) {
            long took = System.currentTimeMillis() - start;
            return BasicDataStepResult.fail(name(), took, "KCA 기본데이터 업데이트 실패", e);
        }
    }
}