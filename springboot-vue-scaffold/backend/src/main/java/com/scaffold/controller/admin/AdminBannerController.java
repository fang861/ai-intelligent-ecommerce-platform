package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.HomeBanner;
import com.scaffold.service.HomeBannerService;
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
@RequestMapping("/admin/banners")
public class AdminBannerController {

    @Autowired
    private HomeBannerService homeBannerService;

    @GetMapping
    public Result<PageResult<HomeBanner>> page(HttpServletRequest request,
                                               @RequestParam(defaultValue = "") String keyword,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(homeBannerService.page(keyword, page, size));
    }

    @PostMapping
    public Result<HomeBanner> create(HttpServletRequest request, @RequestBody HomeBanner banner) {
        AuthContext.requireAdmin(request);
        return Result.ok(homeBannerService.create(banner));
    }

    @PutMapping("/{id}")
    public Result<HomeBanner> update(HttpServletRequest request,
                                     @PathVariable Long id,
                                     @RequestBody HomeBanner banner) {
        AuthContext.requireAdmin(request);
        return Result.ok(homeBannerService.update(id, banner));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        homeBannerService.delete(id);
        return Result.ok();
    }
}
