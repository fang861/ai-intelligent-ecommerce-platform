package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.ShopOrder;
import com.scaffold.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Result<PageResult<ShopOrder>> page(HttpServletRequest request,
                                              @RequestParam(required = false) Long userId,
                                              @RequestParam(required = false) Long merchantId,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "") String keyword,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(orderService.pageForAdmin(userId, merchantId, status, keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<ShopOrder> detail(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        return Result.ok(orderService.getDetail(id));
    }
}
