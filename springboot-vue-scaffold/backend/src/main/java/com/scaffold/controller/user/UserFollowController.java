package com.scaffold.controller.user;

import com.scaffold.common.AuthContext;
import com.scaffold.common.Result;
import com.scaffold.entity.ShopFollow;
import com.scaffold.service.ShopFollowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/follows")
public class UserFollowController {

    @Autowired
    private ShopFollowService shopFollowService;

    @GetMapping
    public Result<List<ShopFollow>> list(HttpServletRequest request) {
        return Result.ok(shopFollowService.listByUser(AuthContext.requireUser(request)));
    }

    @PostMapping
    public Result<ShopFollow> follow(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long merchantId = body.get("merchantId") == null ? null : Long.valueOf(body.get("merchantId").toString());
        return Result.ok(shopFollowService.follow(AuthContext.requireUser(request), merchantId));
    }

    @DeleteMapping("/{merchantId}")
    public Result<Void> unfollow(HttpServletRequest request, @PathVariable Long merchantId) {
        shopFollowService.unfollow(AuthContext.requireUser(request), merchantId);
        return Result.ok();
    }

    @GetMapping("/check/{merchantId}")
    public Result<Boolean> check(HttpServletRequest request, @PathVariable Long merchantId) {
        return Result.ok(shopFollowService.isFollowed(AuthContext.requireUser(request), merchantId));
    }
}
