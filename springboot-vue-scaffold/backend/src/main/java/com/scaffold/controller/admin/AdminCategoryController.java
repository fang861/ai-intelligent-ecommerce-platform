package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.Category;
import com.scaffold.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Result<PageResult<Category>> page(HttpServletRequest request,
                                             @RequestParam(defaultValue = "") String keyword,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(categoryService.page(keyword, status, page, size));
    }

    @PostMapping
    public Result<Category> create(HttpServletRequest request, @RequestBody Category category) {
        AuthContext.requireAdmin(request);
        return Result.ok(categoryService.create(category));
    }

    @PutMapping("/{id}")
    public Result<Category> update(HttpServletRequest request, @PathVariable Long id,
                                   @RequestBody Category category) {
        AuthContext.requireAdmin(request);
        return Result.ok(categoryService.update(id, category));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        categoryService.delete(id);
        return Result.ok();
    }
}
