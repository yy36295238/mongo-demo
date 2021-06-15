package com.xxx.mongo.demo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Document(collection = "user")
@Data
public class User implements Serializable {

    @Id
    private String id;

    private String name;

    private Integer age;

    private Date createTime;

    private Order order;

}
