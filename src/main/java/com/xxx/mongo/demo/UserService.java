package com.xxx.mongo.demo;

import com.xxx.mongo.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/**
 * @author yangyu
 * @date 2021/6/15 9:46 上午
 */

@Service
public class UserService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void insertUser(User user) {
        mongoTemplate.insert(user);
    }

}
