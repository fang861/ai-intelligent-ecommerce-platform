package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.dto.QaAnswerRequest;
import com.scaffold.entity.ProductFavorite;
import com.scaffold.entity.ProductQa;
import com.scaffold.entity.ProductReview;
import com.scaffold.service.FavoriteService;
import com.scaffold.service.ProductQaService;
import com.scaffold.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminReviewQaFavoriteController {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ProductQaService productQaService;
    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/reviews")
    public Result<PageResult<ProductReview>> reviews(HttpServletRequest request,
                                                     @RequestParam(required = false) Long productId,
                                                     @RequestParam(required = false) Long userId,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(reviewService.page(productId, null, userId, page, size));
    }

    @DeleteMapping("/reviews/{id}")
    public Result<Void> deleteReview(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        reviewService.delete(id);
        return Result.ok();
    }

    @GetMapping("/qa")
    public Result<PageResult<ProductQa>> qa(HttpServletRequest request,
                                            @RequestParam(required = false) Long productId,
                                            @RequestParam(required = false) Boolean unanswered,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(productQaService.page(productId, null, unanswered, page, size));
    }

    @PostMapping("/qa/{id}/answer")
    public Result<ProductQa> answerQa(HttpServletRequest request, @PathVariable Long id,
                                      @RequestBody QaAnswerRequest body) {
        Long adminId = AuthContext.requireLogin(request);
        AuthContext.requireAdmin(request);
        return Result.ok(productQaService.answer(id, adminId, body.getAnswer(), null, true));
    }

    @DeleteMapping("/qa/{id}")
    public Result<Void> deleteQa(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        productQaService.delete(id);
        return Result.ok();
    }

    @GetMapping("/favorites")
    public Result<PageResult<ProductFavorite>> favorites(HttpServletRequest request,
                                                         @RequestParam(required = false) Long userId,
                                                         @RequestParam(required = false) Long productId,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(favoriteService.page(userId, productId, page, size));
    }

    @DeleteMapping("/favorites/{id}")
    public Result<Void> deleteFavorite(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        favoriteService.deleteById(id);
        return Result.ok();
    }
}
