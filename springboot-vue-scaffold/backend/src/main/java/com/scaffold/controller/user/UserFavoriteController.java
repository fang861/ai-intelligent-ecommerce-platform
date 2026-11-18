package com.scaffold.controller.user;

import com.scaffold.common.AuthContext;
import com.scaffold.common.Result;
import com.scaffold.entity.ProductFavorite;
import com.scaffold.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/favorites")
public class UserFavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public Result<List<ProductFavorite>> list(HttpServletRequest request) {
        return Result.ok(favoriteService.listByUser(AuthContext.requireUser(request)));
    }

    @PostMapping
    public Result<ProductFavorite> add(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long productId = body.get("productId") == null ? null : Long.valueOf(body.get("productId").toString());
        return Result.ok(favoriteService.add(AuthContext.requireUser(request), productId));
    }

    @DeleteMapping("/{productId}")
    public Result<Void> remove(HttpServletRequest request, @PathVariable Long productId) {
        favoriteService.remove(AuthContext.requireUser(request), productId);
        return Result.ok();
    }

    @GetMapping("/check/{productId}")
    public Result<Boolean> check(HttpServletRequest request, @PathVariable Long productId) {
        return Result.ok(favoriteService.isFavorite(AuthContext.requireUser(request), productId));
    }
}
