package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.Result;
import com.scaffold.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping
    public Result<Map<String, Object>> stats(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return Result.ok(statsService.adminStats());
    }
}
