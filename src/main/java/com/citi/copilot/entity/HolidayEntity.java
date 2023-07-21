package com.citi.copilot.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayEntity {
    private String countryCode;
    private String countryDesc;
    private LocalDate holidayDate;
    private String holidayName;
}
