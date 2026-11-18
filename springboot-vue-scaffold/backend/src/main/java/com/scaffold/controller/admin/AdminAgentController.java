package com.scaffold.controller.admin;

import com.scaffold.agent.AgentAdminService;
import com.scaffold.common.AuthContext;
import com.scaffold.common.Result;
import com.scaffold.dto.AgentAdminOverview;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/agent")
public class AdminAgentController {

    @Autowired
    private AgentAdminService agentAdminService;

    @GetMapping("/overview")
    public Result<AgentAdminOverview> overview(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return Result.ok(agentAdminService.overview());
    }
}
