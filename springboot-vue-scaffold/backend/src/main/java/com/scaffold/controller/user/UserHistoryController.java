package com.scaffold.controller.user;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.BrowseHistory;
import com.scaffold.service.BrowseHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user/history")
public class UserHistoryController {

    @Autowired
    private BrowseHistoryService browseHistoryService;

    @GetMapping
    public Result<PageResult<BrowseHistory>> page(HttpServletRequest request,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return Result.ok(browseHistoryService.page(AuthContext.requireUser(request), page, size));
    }

    @PostMapping
    public Result<BrowseHistory> record(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long productId = body.get("productId") == null ? null : Long.valueOf(body.get("productId").toString());
        return Result.ok(browseHistoryService.record(AuthContext.requireUser(request), productId));
    }

    @DeleteMapping("/{productId}")
    public Result<Void> remove(HttpServletRequest request, @PathVariable Long productId) {
        browseHistoryService.remove(AuthContext.requireUser(request), productId);
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> clear(HttpServletRequest request) {
        browseHistoryService.clear(AuthContext.requireUser(request));
        return Result.ok();
    }
}
