package com.scaffold.controller.user;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.dto.AccountProfile;
import com.scaffold.dto.RechargeRequest;
import com.scaffold.entity.RechargeRecord;
import com.scaffold.service.AuthService;
import com.scaffold.service.RechargeRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserProfileController {

    @Autowired
    private AuthService authService;
    @Autowired
    private RechargeRecordService rechargeRecordService;

    @GetMapping("/profile")
    public Result<AccountProfile> profile(HttpServletRequest request) {
        Long userId = AuthContext.requireUser(request);
        return Result.ok(authService.getCurrentUser(userId, "USER"));
    }

    @PostMapping("/recharge")
    public Result<AccountProfile> recharge(HttpServletRequest request, @RequestBody RechargeRequest body) {
        Long userId = AuthContext.requireUser(request);
        return Result.ok(authService.recharge(userId, body.getAmount()));
    }

    @GetMapping("/recharge/records")
    public Result<PageResult<RechargeRecord>> rechargeRecords(HttpServletRequest request,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return Result.ok(rechargeRecordService.page(AuthContext.requireUser(request), page, size));
    }
}
