package com.scaffold.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Long addressId;
    @JsonAlias("cartIds")
    private List<Long> cartItemIds;
}
