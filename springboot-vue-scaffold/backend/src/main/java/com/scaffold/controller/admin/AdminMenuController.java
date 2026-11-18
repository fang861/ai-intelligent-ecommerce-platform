package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.SysMenu;
import com.scaffold.service.SysMenuService;
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

import java.util.List;

@RestController
@RequestMapping("/admin/menus")
public class AdminMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @GetMapping
    public Result<PageResult<SysMenu>> page(HttpServletRequest request,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "50") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(sysMenuService.page(page, size));
    }

    @GetMapping({"/tree", "/all"})
    public Result<List<SysMenu>> tree(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return Result.ok(sysMenuService.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysMenu> detail(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        return Result.ok(sysMenuService.getById(id));
    }

    @PostMapping
    public Result<SysMenu> create(HttpServletRequest request, @RequestBody SysMenu menu) {
        AuthContext.requireAdmin(request);
        return Result.ok(sysMenuService.create(menu));
    }

    @PutMapping("/{id}")
    public Result<SysMenu> update(HttpServletRequest request,
                                   @PathVariable Long id,
                                   @RequestBody SysMenu menu) {
        AuthContext.requireAdmin(request);
        return Result.ok(sysMenuService.update(id, menu));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        sysMenuService.delete(id);
        return Result.ok();
    }
}
