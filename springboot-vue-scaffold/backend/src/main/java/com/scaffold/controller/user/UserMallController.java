package com.scaffold.controller.user;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.dto.ShopVO;
import com.scaffold.entity.Category;
import com.scaffold.entity.Product;
import com.scaffold.entity.ProductQa;
import com.scaffold.entity.ProductReview;
import com.scaffold.service.BrowseHistoryService;
import com.scaffold.service.CategoryService;
import com.scaffold.service.ProductQaService;
import com.scaffold.service.ProductService;
import com.scaffold.service.RecommendService;
import com.scaffold.service.ReviewService;
import com.scaffold.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserMallController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;
    @Autowired
    private RecommendService recommendService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ProductQaService productQaService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private BrowseHistoryService browseHistoryService;

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(categoryService.listEnabled());
    }

    @GetMapping("/shops")
    public Result<PageResult<ShopVO>> shops(@RequestParam(defaultValue = "") String keyword,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "12") int size) {
        return Result.ok(shopService.page(keyword, page, size));
    }

    @GetMapping("/shops/{id}")
    public Result<ShopVO> shopDetail(@PathVariable Long id) {
        return Result.ok(shopService.getPublicShop(id));
    }

    @GetMapping("/products")
    public Result<PageResult<Product>> products(@RequestParam(defaultValue = "") String keyword,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) Long merchantId,
                                                @RequestParam(defaultValue = "default") String sort,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "12") int size) {
        return Result.ok(productService.page(keyword, categoryId, merchantId, 1, sort, page, size));
    }

    @GetMapping("/products/hot")
    public Result<List<Product>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(productService.listHot(limit));
    }

    @GetMapping("/products/recommend")
    public Result<List<Product>> recommend(HttpServletRequest request,
                                           @RequestParam(required = false) Long productId,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) String mode,
                                           @RequestParam(defaultValue = "10") int limit) {
        Long userId = AuthContext.getUserId(request);
        return Result.ok(recommendService.recommend(userId, productId, categoryId, mode, limit));
    }

    @GetMapping("/products/{id}")
    public Result<Product> productDetail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = AuthContext.getUserId(request);
        Product product = productService.getOnShelf(id);
        if (userId != null) {
            try {
                browseHistoryService.record(userId, id);
            } catch (Exception ignored) {
                // 足迹失败不影响详情
            }
        }
        return Result.ok(product);
    }

    @GetMapping("/products/{id}/reviews")
    public Result<PageResult<ProductReview>> productReviews(@PathVariable Long id,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(reviewService.page(id, null, null, page, size));
    }

    @GetMapping("/products/{id}/qa")
    public Result<PageResult<ProductQa>> productQa(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return Result.ok(productQaService.page(id, null, null, page, size));
    }

    @PostMapping("/reviews")
    public Result<ProductReview> createReview(HttpServletRequest request, @RequestBody ProductReview review) {
        return Result.ok(reviewService.create(AuthContext.requireUser(request), review));
    }

    @PostMapping("/qa")
    public Result<ProductQa> askQa(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = AuthContext.requireUser(request);
        Long productId = body.get("productId") == null ? null : Long.valueOf(body.get("productId").toString());
        String question = body.get("question") == null ? null : body.get("question").toString();
        return Result.ok(productQaService.ask(userId, productId, question));
    }
}
