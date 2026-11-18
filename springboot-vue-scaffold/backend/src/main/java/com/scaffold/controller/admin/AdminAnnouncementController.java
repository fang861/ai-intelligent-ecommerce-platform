package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.Announcement;
import com.scaffold.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/announcements")
public class AdminAnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public Result<PageResult<Announcement>> page(HttpServletRequest request,
                                                 @RequestParam(defaultValue = "") String keyword,
                                                 @RequestParam(required = false) Integer status,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(announcementService.page(keyword, status, page, size));
    }

    @PostMapping
    public Result<Announcement> create(HttpServletRequest request, @RequestBody Announcement announcement) {
        AuthContext.requireAdmin(request);
        return Result.ok(announcementService.create(announcement));
    }

    @PutMapping("/{id}")
    public Result<Announcement> update(HttpServletRequest request, @PathVariable Long id,
                                       @RequestBody Announcement announcement) {
        AuthContext.requireAdmin(request);
        return Result.ok(announcementService.update(id, announcement));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        announcementService.delete(id);
        return Result.ok();
    }
}
