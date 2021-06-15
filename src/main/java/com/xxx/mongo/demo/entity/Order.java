package com.xxx.mongo.demo.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Order implements Serializable {

    private String orderId;

    private String status;

    private String goodsName;

    private Date createTime;

}
