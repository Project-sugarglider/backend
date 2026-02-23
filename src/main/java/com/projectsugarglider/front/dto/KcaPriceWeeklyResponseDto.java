package com.projectsugarglider.front.dto;

import java.util.List;

public record KcaPriceWeeklyResponseDto(
    String goodInspectDay,
    List<KcaPriceResponseDto> items
) {}