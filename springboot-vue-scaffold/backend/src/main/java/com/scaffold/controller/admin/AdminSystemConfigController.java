package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.Result;
import com.scaffold.service.SysConfigService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/system-config")
public class AdminSystemConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    @GetMapping
    public Result<Map<String, String>> get(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return Result.ok(sysConfigService.getPublicConfig());
    }

    @PutMapping
    public Result<Map<String, String>> update(HttpServletRequest request, @RequestBody Map<String, String> body) {
        AuthContext.requireAdmin(request);
        String systemName = body == null ? null : body.get("systemName");
        String systemLogo = body == null ? null : body.get("systemLogo");
        return Result.ok(sysConfigService.updatePublicConfig(systemName, systemLogo));
    }
}
