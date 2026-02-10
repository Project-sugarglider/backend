package com.projectsugarglider.front.dto;

import java.util.List;
import java.util.Map;

public record RegionInitResponseDto(
        List<Map<String, String>> upperList,
        Map<String, List<Map<String, String>>> lowersByUpper,
        String selectedUpper,
        String selectedLower
) {}
