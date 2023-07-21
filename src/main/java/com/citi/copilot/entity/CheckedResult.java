package com.citi.copilot.entity;

import lombok.Data;

import java.util.List;

@Data
public class CheckedResult {
    private List<String> isHoliday;
    private List<String> notHoliday;
}
