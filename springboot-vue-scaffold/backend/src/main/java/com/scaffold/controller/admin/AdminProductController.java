package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.Product;
import com.scaffold.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<PageResult<Product>> page(HttpServletRequest request,
                                            @RequestParam(defaultValue = "") String keyword,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Long merchantId,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(productService.page(keyword, categoryId, merchantId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<Product> detail(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        return Result.ok(productService.getById(id));
    }

    @PostMapping
    public Result<Product> create(HttpServletRequest request, @RequestBody Product product) {
        AuthContext.requireAdmin(request);
        Long merchantId = product.getMerchantId();
        if (merchantId == null) {
            return Result.fail("商家ID不能为空");
        }
        return Result.ok(productService.create(product, merchantId));
    }

    @PutMapping("/{id}")
    public Result<Product> update(HttpServletRequest request, @PathVariable Long id,
                                  @RequestBody Product product) {
        AuthContext.requireAdmin(request);
        return Result.ok(productService.update(id, product, null, true));
    }

    @PostMapping("/{id}/on-shelf")
    public Result<Product> onShelf(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        return Result.ok(productService.onShelf(id, null, true));
    }

    @PostMapping("/{id}/off-shelf")
    public Result<Product> offShelf(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        return Result.ok(productService.offShelf(id, null, true));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        productService.delete(id, null, true);
        return Result.ok();
    }
}
