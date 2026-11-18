package com.scaffold.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("recharge_record")
public class RechargeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Double amount;
    private Double balanceAfter;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private String createdAt;
}
