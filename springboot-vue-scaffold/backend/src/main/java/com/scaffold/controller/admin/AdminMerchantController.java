package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.dto.MerchantAuditRequest;
import com.scaffold.entity.Merchant;
import com.scaffold.service.MerchantService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/merchants")
public class AdminMerchantController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping
    public Result<PageResult<Merchant>> page(HttpServletRequest request,
                                             @RequestParam(defaultValue = "") String keyword,
                                             @RequestParam(required = false) Integer auditStatus,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(merchantService.page(keyword, auditStatus, page, size));
    }

    @GetMapping("/{id}")
    public Result<Merchant> detail(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        return Result.ok(merchantService.getById(id));
    }

    @PostMapping("/{id}/audit")
    public Result<Merchant> audit(HttpServletRequest request, @PathVariable Long id,
                                  @RequestBody MerchantAuditRequest body) {
        AuthContext.requireAdmin(request);
        return Result.ok(merchantService.audit(id, body));
    }
}
