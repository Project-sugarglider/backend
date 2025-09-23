package com.projectsugarglider.front.dto;

import java.util.List;


public record TemperatureDataBundle (
    List<String> labels,
    List<String> data
)
{}
