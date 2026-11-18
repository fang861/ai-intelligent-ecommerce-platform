package com.scaffold.controller.user;

import com.scaffold.common.Result;
import com.scaffold.entity.HomeBanner;
import com.scaffold.service.HomeBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/banners")
public class UserBannerController {

    @Autowired
    private HomeBannerService homeBannerService;

    @GetMapping
    public Result<List<HomeBanner>> list() {
        return Result.ok(homeBannerService.listEnabled());
    }
}
