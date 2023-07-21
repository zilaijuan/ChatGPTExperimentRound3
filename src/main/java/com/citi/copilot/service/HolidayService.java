package com.citi.copilot.service;

import com.citi.copilot.entity.CheckedResult;
import com.citi.copilot.entity.HolidayEntity;

import com.citi.copilot.entity.RemoveHolidayEntity;
import com.citi.copilot.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HolidayService {

    public Result add(List<HolidayEntity> list) {
        return writeToFile(list, false);

    }

    private Result writeToFile(List<HolidayEntity> list, boolean updateWhenExist) {
        Result r = new Result();
        //open data.csv file with write mode
        List<HolidayEntity> data = loadData();
        String result = duplicateCheck(list, data);
        if (!Strings.isBlank(result)) {
            log.warn("duplicate data found, {}", result);
            r.setMessage("duplicate data found, " + result);
            r.setStatus(false);
            return r;
        }
        if (data.addAll(list)) {
            try {
                extracted(data);
                r.setStatus(true);
                r.setMessage("success");
                return r;
            } catch (IOException e) {
                log.warn("write to file error", e);
            }
        }
        r.setStatus(false);
        r.setMessage("write to file error");
        return r;

    }

    private static void extracted(List<HolidayEntity> data) throws IOException {
        Writer file = new FileWriter("data.csv");
        CSVPrinter printer = CSVFormat.EXCEL.print(file);
        for (HolidayEntity entity : data) {
            printer.printRecord(entity.getCountryCode(), entity.getCountryDesc(), entity.getHolidayDate(), entity.getHolidayName());
        }
        printer.flush();
        printer.close();
        file.close();
    }

    private String duplicateCheck(List<HolidayEntity> list, List<HolidayEntity> data) {
        String result = null;
        for (HolidayEntity entity : list) {
            for (HolidayEntity entity1 : data) {
                if (entity.getCountryCode().equals(entity1.getCountryCode()) && entity.getHolidayDate().equals(entity1.getHolidayDate())) {
                    result = entity.getCountryCode() + " " + entity.getHolidayDate();
                    break;
                }
            }
        }
        return result;
    }

    private List<HolidayEntity> loadData() {
        List<HolidayEntity> data = new ArrayList<>();
        try {
            Reader file = new FileReader("data.csv");
            Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(file);
            for (CSVRecord record : records) {
                // convert record to HolidayEntity
                HolidayEntity entity = new HolidayEntity();
                entity.setCountryCode(record.get(0));
                entity.setCountryDesc(record.get(1));
                entity.setHolidayDate(LocalDate.parse(record.get(2)));
                entity.setHolidayName(record.get(3));
                data.add(entity);
            }
        } catch (IOException e) {
            log.warn("load data error", e);
        }
        return data;
    }

    public Result update(List<HolidayEntity> list) {
        Result r = new Result();
        // load data
        List<HolidayEntity> data = loadData();

        list.forEach(entity -> {
            for (HolidayEntity entity1 : data) {
                if (entity.getCountryCode().equals(entity1.getCountryCode()) && entity.getHolidayDate().equals(entity1.getHolidayDate())) {
                    entity1.setCountryDesc(entity.getCountryDesc());
                    entity1.setHolidayName(entity.getHolidayName());
                    break;
                }
            }
        });
        try {
            extracted(data);
            r.setMessage("success");
            r.setStatus(true);
            return r;
        } catch (IOException e) {
            log.error("write to file error", e);
        }
        r.setStatus(false);
        r.setMessage("write to file error");
        return r;
    }

    public Result remove(List<RemoveHolidayEntity> list) {
        // load data
        List<HolidayEntity> data = loadData();
        Result r = new Result();
        list.forEach(entity -> {
            for (HolidayEntity entity1 : data) {
                if (entity.getCountryCode().equals(entity1.getCountryCode()) && entity.getHolidayDate().equals(entity1.getHolidayDate())) {
                    data.remove(entity1);
                    break;
                }
            }
        });
        try {
            extracted(data);
            r.setMessage("success");
            r.setStatus(true);
            return r;
        } catch (IOException e) {
            log.error("write to file error", e);
        }
        r.setStatus(false);
        r.setMessage("write to file error");
        return r;
    }

    public Result nextYear(String countryCode) {

        Result r = new Result();

        List<HolidayEntity> data = loadData();

        LocalDate now = LocalDate.now();
        // next year
        LocalDate nextYear = now.plusYears(1);
        // first day of next year
        LocalDate first = nextYear.withDayOfYear(1);
        // last day of next year
        LocalDate last = nextYear.withDayOfYear(nextYear.lengthOfYear());

        List<HolidayEntity> result = data.stream()
                .filter(entity -> entity.getCountryCode().equals(countryCode))
                .filter(entity ->
                        // filter holiday date between first day of next year and last day of next year
                        entity.getHolidayDate().compareTo(first) >= 0 && entity.getHolidayDate().compareTo(last) <= 0
                ).sorted(Comparator.comparing(HolidayEntity::getHolidayDate)).collect(Collectors.toList());

        r.setStatus(true);
        r.setMessage("success");
        r.setData(result);
        return r;
    }

    public Result next(String countryCode) {
        // load data
        List<HolidayEntity> data = loadData();
        Result r = new Result();
        LocalDate now = LocalDate.now();
        HolidayEntity next = data.stream()
                .filter(entity -> entity.getCountryCode().equals(countryCode))
                .filter(entity -> entity.getHolidayDate().isAfter(now))
                .sorted(Comparator.comparing(HolidayEntity::getHolidayDate))
                .findFirst().orElse(null);
        if (next != null) {
            r.setStatus(true);
            r.setMessage("success");
            r.setData(next);
        } else {
            r.setStatus(false);
            r.setMessage("no next holiday");
        }
        return r;
    }

    public Result checkHoliday(String date) {
        // load data
        List<HolidayEntity> data = loadData();
        Result<CheckedResult> r = new Result();
        LocalDate localDate = LocalDate.parse(date);
        List<String> allCountry = data.stream().map(HolidayEntity::getCountryCode).distinct().collect(Collectors.toList());

        List<String> isHoliday = data.stream()
                .filter(entity -> entity.getHolidayDate().equals(localDate))
                .map(HolidayEntity::getCountryCode)
                .distinct()
                .collect(Collectors.toList());
        allCountry.removeAll(isHoliday);

        CheckedResult checkedResult = new CheckedResult();
        checkedResult.setIsHoliday(isHoliday);
        checkedResult.setNotHoliday(allCountry);
        r.setStatus(true);
        r.setData(checkedResult);

        return r;
    }
}
