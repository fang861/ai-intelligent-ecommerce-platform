package com.scaffold.controller.user;

import com.scaffold.common.Result;
import com.scaffold.entity.Announcement;
import com.scaffold.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/announcements")
public class UserAnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public Result<List<Announcement>> list() {
        return Result.ok(announcementService.listEnabled());
    }

    @GetMapping("/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        Announcement a = announcementService.getById(id);
        if (a.getStatus() == null || a.getStatus() != 1) {
            return Result.fail(404, "公告不存在");
        }
        return Result.ok(a);
    }
}
