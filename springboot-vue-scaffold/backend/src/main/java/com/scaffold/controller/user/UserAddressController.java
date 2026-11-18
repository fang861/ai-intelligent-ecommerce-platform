package com.scaffold.controller.user;

import com.scaffold.common.AuthContext;
import com.scaffold.common.Result;
import com.scaffold.entity.UserAddress;
import com.scaffold.service.AddressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addresses")
public class UserAddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public Result<List<UserAddress>> list(HttpServletRequest request) {
        return Result.ok(addressService.list(AuthContext.requireUser(request)));
    }

    @PostMapping
    public Result<UserAddress> create(HttpServletRequest request, @RequestBody UserAddress address) {
        return Result.ok(addressService.create(AuthContext.requireUser(request), address));
    }

    @PutMapping("/{id}")
    public Result<UserAddress> update(HttpServletRequest request, @PathVariable Long id,
                                      @RequestBody UserAddress address) {
        return Result.ok(addressService.update(AuthContext.requireUser(request), id, address));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        addressService.delete(AuthContext.requireUser(request), id);
        return Result.ok();
    }

    @PutMapping("/{id}/default")
    public Result<UserAddress> setDefault(HttpServletRequest request, @PathVariable Long id) {
        return Result.ok(addressService.setDefault(AuthContext.requireUser(request), id));
    }
}
