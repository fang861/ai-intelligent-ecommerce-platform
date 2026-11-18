package com.scaffold.controller.merchant;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.dto.AccountProfile;
import com.scaffold.dto.QaAnswerRequest;
import com.scaffold.entity.Merchant;
import com.scaffold.entity.Product;
import com.scaffold.entity.ProductQa;
import com.scaffold.entity.ProductReview;
import com.scaffold.entity.ShopOrder;
import com.scaffold.service.AuthService;
import com.scaffold.service.MerchantService;
import com.scaffold.service.OrderService;
import com.scaffold.service.ProductQaService;
import com.scaffold.service.ProductService;
import com.scaffold.service.ReviewService;
import com.scaffold.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ProductQaService productQaService;
    @Autowired
    private StatsService statsService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private AuthService authService;

    @GetMapping("/profile")
    public Result<AccountProfile> profile(HttpServletRequest request) {
        Long id = AuthContext.requireMerchant(request);
        return Result.ok(authService.getCurrentUser(id, "MERCHANT"));
    }

    @PutMapping("/profile")
    public Result<Merchant> updateProfile(HttpServletRequest request, @RequestBody Merchant body) {
        Long id = AuthContext.requireMerchant(request);
        return Result.ok(merchantService.updateProfile(id, body));
    }

    @GetMapping("/products")
    public Result<PageResult<Product>> products(HttpServletRequest request,
                                                @RequestParam(defaultValue = "") String keyword,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(productService.page(keyword, null, merchantId, status, page, size));
    }

    @GetMapping("/products/{id}")
    public Result<Product> productDetail(HttpServletRequest request, @PathVariable Long id) {
        Long merchantId = AuthContext.requireMerchant(request);
        Product product = productService.getById(id);
        if (product.getMerchantId() == null || !product.getMerchantId().equals(merchantId)) {
            throw new com.scaffold.common.BusinessException(403, "无权查看该商品");
        }
        return Result.ok(product);
    }

    @PostMapping("/products")
    public Result<Product> createProduct(HttpServletRequest request, @RequestBody Product product) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(productService.create(product, merchantId));
    }

    @PutMapping("/products/{id}")
    public Result<Product> updateProduct(HttpServletRequest request, @PathVariable Long id,
                                         @RequestBody Product product) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(productService.update(id, product, merchantId, false));
    }

    @PostMapping("/products/{id}/on-shelf")
    public Result<Product> onShelf(HttpServletRequest request, @PathVariable Long id) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(productService.onShelf(id, merchantId, false));
    }

    @PostMapping("/products/{id}/off-shelf")
    public Result<Product> offShelf(HttpServletRequest request, @PathVariable Long id) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(productService.offShelf(id, merchantId, false));
    }

    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(HttpServletRequest request, @PathVariable Long id) {
        Long merchantId = AuthContext.requireMerchant(request);
        productService.delete(id, merchantId, false);
        return Result.ok();
    }

    @GetMapping("/orders")
    public Result<PageResult<ShopOrder>> orders(HttpServletRequest request,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) Integer afterSaleStatus,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(orderService.pageForMerchant(merchantId, status, afterSaleStatus, page, size));
    }

    @GetMapping("/orders/{id}")
    public Result<ShopOrder> orderDetail(HttpServletRequest request, @PathVariable Long id) {
        Long merchantId = AuthContext.requireMerchant(request);
        ShopOrder order = orderService.getDetail(id);
        if (!order.getMerchantId().equals(merchantId)) {
            return Result.fail(403, "无权查看该订单");
        }
        return Result.ok(order);
    }

    @PostMapping("/orders/{id}/ship")
    public Result<ShopOrder> ship(HttpServletRequest request, @PathVariable Long id,
                                  @RequestBody(required = false) com.scaffold.dto.ShipOrderRequest body) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(orderService.ship(id, merchantId, false, body));
    }

    @PostMapping("/orders/{id}/after-sale")
    public Result<ShopOrder> handleAfterSale(HttpServletRequest request, @PathVariable Long id,
                                             @RequestBody com.scaffold.dto.AfterSaleHandleRequest body) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(orderService.handleAfterSale(merchantId, id, body, false));
    }

    @GetMapping("/reviews")
    public Result<PageResult<ProductReview>> reviews(HttpServletRequest request,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(reviewService.page(null, merchantId, null, page, size));
    }

    @GetMapping("/qa")
    public Result<PageResult<ProductQa>> qa(HttpServletRequest request,
                                            @RequestParam(required = false) Boolean unanswered,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(productQaService.page(null, merchantId, unanswered, page, size));
    }

    @PostMapping("/qa/{id}/answer")
    public Result<ProductQa> answer(HttpServletRequest request, @PathVariable Long id,
                                    @RequestBody QaAnswerRequest body) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(productQaService.answer(id, merchantId, body.getAnswer(), merchantId, false));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(HttpServletRequest request) {
        Long merchantId = AuthContext.requireMerchant(request);
        return Result.ok(statsService.merchantStats(merchantId));
    }
}
