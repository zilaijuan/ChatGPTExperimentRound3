package com.citi.copilot.controller;

import com.citi.copilot.entity.HolidayEntity;
import com.citi.copilot.entity.RemoveHolidayEntity;
import com.citi.copilot.entity.Result;
import com.citi.copilot.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HolidayController {


    @Autowired
    private HolidayService holidayService;
    @PostMapping("/add")
    public Result add(@RequestBody List<HolidayEntity> list) {
        return holidayService.add(list);
    }

    @PostMapping("/update")
    public Result update(@RequestBody List<HolidayEntity> list) {
        return holidayService.update(list);
    }

    @PostMapping("/remove")
    public Result remove(@RequestBody List<RemoveHolidayEntity> list) {
        return holidayService.remove(list);
    }

    @GetMapping("/nextYear")
    public Result nextYear(String countryCode) {
        return holidayService.nextYear(countryCode);
    }

    @GetMapping("/next")
    public Result next(String countryCode) {
        return holidayService.next(countryCode);
    }

    @GetMapping("/checkHoliday")
    public Result checkHoliday(String date) {
        return holidayService.checkHoliday(date);
    }
}
