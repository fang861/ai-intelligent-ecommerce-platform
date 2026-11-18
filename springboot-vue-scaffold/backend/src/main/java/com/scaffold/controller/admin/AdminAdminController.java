package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.Admin;
import com.scaffold.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/admins")
public class AdminAdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    public Result<PageResult<Admin>> page(HttpServletRequest request,
                                          @RequestParam(defaultValue = "") String keyword,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(adminService.page(keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<Admin> detail(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        return Result.ok(adminService.getById(id));
    }

    @PostMapping
    public Result<Admin> create(HttpServletRequest request, @RequestBody Admin admin) {
        AuthContext.requireAdmin(request);
        return Result.ok(adminService.create(admin));
    }

    @PutMapping("/{id}")
    public Result<Admin> update(HttpServletRequest request,
                                @PathVariable Long id,
                                @RequestBody Admin admin) {
        AuthContext.requireAdmin(request);
        return Result.ok(adminService.update(id, admin));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        adminService.delete(id);
        return Result.ok();
    }
}
