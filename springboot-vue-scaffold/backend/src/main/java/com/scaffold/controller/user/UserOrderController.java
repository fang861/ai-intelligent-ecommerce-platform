package com.scaffold.controller.user;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.dto.CreateOrderRequest;
import com.scaffold.entity.ShopOrder;
import com.scaffold.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/orders")
public class UserOrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result<List<ShopOrder>> create(HttpServletRequest request, @RequestBody CreateOrderRequest body) {
        return Result.ok(orderService.create(AuthContext.requireUser(request), body));
    }

    @GetMapping
    public Result<PageResult<ShopOrder>> page(HttpServletRequest request,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.ok(orderService.pageForUser(AuthContext.requireUser(request), status, page, size));
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> stats(HttpServletRequest request) {
        return Result.ok(orderService.statsForUser(AuthContext.requireUser(request)));
    }

    @GetMapping("/{id}")
    public Result<ShopOrder> detail(HttpServletRequest request, @PathVariable Long id) {
        return Result.ok(orderService.getOwnedByUser(AuthContext.requireUser(request), id));
    }

    @PostMapping("/{id}/pay")
    public Result<ShopOrder> pay(HttpServletRequest request, @PathVariable Long id) {
        return Result.ok(orderService.pay(AuthContext.requireUser(request), id));
    }

    @PostMapping("/{id}/cancel")
    public Result<ShopOrder> cancel(HttpServletRequest request, @PathVariable Long id) {
        return Result.ok(orderService.cancel(AuthContext.requireUser(request), id));
    }

    @PostMapping("/{id}/after-sale")
    public Result<ShopOrder> applyAfterSale(HttpServletRequest request, @PathVariable Long id,
                                            @RequestBody com.scaffold.dto.AfterSaleApplyRequest body) {
        return Result.ok(orderService.applyAfterSale(AuthContext.requireUser(request), id, body));
    }

    @PostMapping("/{id}/confirm")
    public Result<ShopOrder> confirm(HttpServletRequest request, @PathVariable Long id) {
        return Result.ok(orderService.confirm(AuthContext.requireUser(request), id));
    }
}
