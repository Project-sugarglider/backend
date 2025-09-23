package com.projectsugarglider.front.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.projectsugarglider.datainitialize.entity.LowerLocationEntity;
import com.projectsugarglider.datainitialize.repository.LowerLocationCodeRepository;

import lombok.RequiredArgsConstructor;

/**
 * Front Page를 제공할때, model에 Location 데이터를 추가합니다.
 */
@Service
@RequiredArgsConstructor
public class LocationDataInsert {
    
        private final LowerLocationCodeRepository lowerRepo;

        public Model dataSave(Model model){
            List<LowerLocationEntity> all = getAll();
            List<String> upperCodes = getUpperCodes(all);
            List<Map<String, String>> upperList = getUpperList(upperCodes);
            Map<String, List<Map<String, String>>> lowersByUpper = getLowersByUpper(all);

            String selectedUpper = getSelectedUpper(upperCodes);
            String selectedLower = getSelectedLower(lowersByUpper, selectedUpper);

            model.addAttribute("upperList", upperList);
            model.addAttribute("lowersByUpper", lowersByUpper);
            model.addAttribute("selectedUpper", selectedUpper);
            model.addAttribute("selectedLower", selectedLower);

            return model;

        }

        /**
         * selectedUpper에 맞는 LowerCode를 반환
         * 
         * @param lowersByUpper
         * @param selectedUpper
         * @return
         */
        private String getSelectedLower(Map<String, List<Map<String, String>>> lowersByUpper, String selectedUpper) {
            String selectedLower = "";
            if (!selectedUpper.isEmpty()) {
                List<Map<String, String>> firstLowers = lowersByUpper.getOrDefault(selectedUpper, List.of());
                if (!firstLowers.isEmpty()) {
                    selectedLower = firstLowers.get(0).get("code");
                }
            }
            return selectedLower;
        }

        /**
         * Upper 지역데이터를 추가합니다
         * 
         * @param upperCodes
         * @return
         */
        private String getSelectedUpper(List<String> upperCodes) {
            String selectedUpper = upperCodes.isEmpty() ? "" : upperCodes.get(0);
            return selectedUpper;
        }

        /**
         * Location 데이터로 Map<Upper, List<Map<LowerCode, LowerName>>> 으로 변환하는 함수
         * 
         * @param all
         * @return
         */
        private Map<String, List<Map<String, String>>> getLowersByUpper(List<LowerLocationEntity> all) {
            Map<String, List<Map<String, String>>> lowersByUpper = new LinkedHashMap<>();
            for (LowerLocationEntity e : all) {
                lowersByUpper.computeIfAbsent(e.getUpperCode(), k -> new ArrayList<>())
                             .add(Map.of("code", e.getLowerCode(), "name", e.getLowerCode()));
            }
            return lowersByUpper;
        }

        /**
         * UpperCode를 List<Map<UpperCode, UpperName>> 으로 변환하는 함수
         */
        private List<Map<String, String>> getUpperList(List<String> upperCodes) {
            List<Map<String, String>> upperList = upperCodes.stream()
                    .map(uc -> Map.of("code", uc, "name", uc)) 
                    .collect(Collectors.toList());
            return upperList;
        }

        /** 
         * 전체 지역 데이터에서 UpperCode를 가져오는 함수
         */
        private List<String> getUpperCodes(List<LowerLocationEntity> all) {
            List<String> upperCodes = all.stream()
                    .map(LowerLocationEntity::getUpperCode)
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));
            return upperCodes;
        }

        //TODO : 캐싱 추가

        /**
         * Lower 데이터를 모두 가져오는 함수
         * 
         * @return
         */
        private List<LowerLocationEntity> getAll() {
            List<LowerLocationEntity> all = lowerRepo.findAll(
                    Sort.by(Sort.Order.asc("upperCode"), Sort.Order.asc("lowerCode"))
            );
            return all;
        }
    
}
