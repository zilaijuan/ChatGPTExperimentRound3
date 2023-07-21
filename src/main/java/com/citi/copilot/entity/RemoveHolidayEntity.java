package com.citi.copilot.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RemoveHolidayEntity {
    private String countryCode;
    private LocalDate holidayDate;
}
