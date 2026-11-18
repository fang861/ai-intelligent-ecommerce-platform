package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.User;
import com.scaffold.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<PageResult<User>> page(HttpServletRequest request,
                                         @RequestParam(defaultValue = "") String keyword,
                                         @RequestParam(required = false) String role,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(userService.page(keyword, role, page, size));
    }

    @GetMapping("/{id}")
    public Result<User> detail(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        return Result.ok(userService.getById(id));
    }

    @PostMapping
    public Result<User> create(HttpServletRequest request, @RequestBody User user) {
        AuthContext.requireAdmin(request);
        return Result.ok(userService.create(user));
    }

    @PutMapping("/{id}")
    public Result<User> update(HttpServletRequest request,
                               @PathVariable Long id,
                               @RequestBody User user) {
        AuthContext.requireAdmin(request);
        return Result.ok(userService.update(id, user));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        userService.delete(id);
        return Result.ok();
    }
}
