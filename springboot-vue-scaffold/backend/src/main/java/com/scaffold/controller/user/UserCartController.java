package com.scaffold.controller.user;

import com.scaffold.common.AuthContext;
import com.scaffold.common.Result;
import com.scaffold.entity.CartItem;
import com.scaffold.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/cart")
public class UserCartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public Result<List<CartItem>> list(HttpServletRequest request) {
        Long userId = AuthContext.requireUser(request);
        return Result.ok(cartService.list(userId));
    }

    @PostMapping
    public Result<CartItem> add(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = AuthContext.requireUser(request);
        Long productId = body.get("productId") == null ? null : Long.valueOf(body.get("productId").toString());
        Integer quantity = body.get("quantity") == null ? 1 : Integer.valueOf(body.get("quantity").toString());
        return Result.ok(cartService.add(userId, productId, quantity));
    }

    @PutMapping("/{id}")
    public Result<CartItem> update(HttpServletRequest request, @PathVariable Long id,
                                   @RequestBody Map<String, Object> body) {
        Long userId = AuthContext.requireUser(request);
        Integer quantity = body.get("quantity") == null ? null : Integer.valueOf(body.get("quantity").toString());
        return Result.ok(cartService.updateQuantity(userId, id, quantity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = AuthContext.requireUser(request);
        cartService.delete(userId, id);
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> clear(HttpServletRequest request) {
        Long userId = AuthContext.requireUser(request);
        cartService.clear(userId);
        return Result.ok();
    }
}
