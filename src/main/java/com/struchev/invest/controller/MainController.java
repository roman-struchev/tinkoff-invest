package com.struchev.invest.controller;

import com.struchev.invest.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ReportService reportService;

    @GetMapping(path = {"/", "/strategies"})
    public ModelAndView strategies() {
        var report = reportService.buildReportStrategiesInfo();
        return new ModelAndView("report_strategies", Map.of("reportStrategiesInfo", report));
    }

    @GetMapping(path = {"/orders"})
    public ModelAndView orders() {
        var orders = reportService.getOrdersSortByIdDesc();
        return new ModelAndView("report_orders", Map.of("orders", orders));
    }

    @GetMapping(path = {"/instrument_by_instrument"})
    public ModelAndView instrumentByInstrument() {
        var report = reportService.buildReportInstrumentByInstrument();
        return new ModelAndView("report_instrument_by_instrument", Map.of("reportInstrumentByInstrument", report));
    }

    @GetMapping(path = {"/instrument_by_fiat"})
    public ModelAndView instrumentByFiat() {
        var report = reportService.buildReportInstrumentByFiat();
        return new ModelAndView("report_instrument_by_fiat", Map.of("reportInstrumentByFiat", report));
    }
}
