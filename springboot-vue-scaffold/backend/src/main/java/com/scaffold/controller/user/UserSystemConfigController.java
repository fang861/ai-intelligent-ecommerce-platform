package com.scaffold.controller.user;

import com.scaffold.common.Result;
import com.scaffold.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user/system-config")
public class UserSystemConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    @GetMapping
    public Result<Map<String, String>> get() {
        return Result.ok(sysConfigService.getPublicConfig());
    }
}
