package com.xxx.mongo.demo;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.xxx.mongo.demo.entity.Order;
import com.xxx.mongo.demo.entity.User;
import com.xxx.mongo.demo.utils.RandUserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author yangyu
 * @date 2021/6/15 9:47 上午
 */

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserService userService;

    @Test
    void insertUser() throws InterruptedException {

        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        int count = 8000;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            executorService.execute(() -> {
                insert();
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.err.println("插入10000000条数据耗时：" + (System.currentTimeMillis() - start) + " ms");


    }

    private void insert() {
        List<User> list = Lists.newArrayList();
        for (int j = 0; j < 10000; j++) {
            User user = new User();
            user.setName(RandUserInfo.randFamilyName());
            user.setAge(RandUserInfo.randAge());
            user.setCreateTime(new Date());

            Order order = new Order();
            order.setOrderId(new Snowflake(1, 1).nextIdStr());
            order.setStatus(RandomUtil.randomNumbers(1));
            order.setGoodsName("华为手机" + RandomUtil.randomNumbers(1));
            order.setCreateTime(new Date());
            user.setOrder(order);
            list.add(user);
        }
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);
        bulkOperations.insert(list);
        bulkOperations.execute();
    }

    @Test
    void queryById() {
        User user = mongoTemplate.findById("60c81db3c281260bbb11909f", User.class);
        System.err.println(user);
    }

    @Test
    void queryByIdError() throws InterruptedException {
        long start = System.currentTimeMillis();
        Query query = Query.query(Criteria.where("id1").is("60c81db3c281260bbb11909f"));
        User user = mongoTemplate.findOne(query, User.class, "user");
        System.err.println(user);
        System.err.println(System.currentTimeMillis() - start);
    }

    @Test
    void queryUser() {
        Query query = new Query();
        query.addCriteria(Criteria.where("order.status").is(1));
        List<User> users = mongoTemplate.find(query, User.class);
        System.err.println(users);
    }

    @Test
    void queryIndex() {
        List<IndexInfo> indexInfo = mongoTemplate.indexOps(User.class).getIndexInfo();
        indexInfo.forEach(System.out::println);
    }

    /**
     * 深度分页优化，先返回ID，走索引覆盖
     * 1、age是二级索引，先根据age查询id集合
     * 2、在根据ID集合查询出想要的数据
     */
    @Test
    void queryPage() {
        long start = System.currentTimeMillis();
        List<User> users = mongoTemplate.find(new Query().addCriteria(Criteria.where("age").is(23)).skip(1000000).limit(200), User.class);
        System.err.println("cost:" + (System.currentTimeMillis() - start) + " " + users);

        long start1 = System.currentTimeMillis();
        Query newQuery = new Query().addCriteria(Criteria.where("age").is(23)).skip(1000000).limit(200);
        newQuery.fields().include("_id");
        List<User> userList = mongoTemplate.find(newQuery, User.class);
        List<String> idList = userList.stream().map(User::getId).collect(Collectors.toList());
        Query query = new Query().addCriteria(Criteria.where("_id").in(idList));
        mongoTemplate.find(query, User.class);
        System.err.println("cost:" + (System.currentTimeMillis() - start1) + " " + users);


    }

    @Test
    void queryCountNOWhere() {
        long start = System.currentTimeMillis();
        long count = mongoTemplate.count(new Query(), User.class);
        System.err.println(String.format("数量：%s, 耗时：%s", count, (System.currentTimeMillis() - start) + " ms"));
    }

    @Test
    void queryCount() {
        long start = System.currentTimeMillis();
        Query query = new Query().addCriteria(Criteria.where("age").is(23));
        long count = mongoTemplate.count(new Query(), User.class);
        System.err.println(String.format("数量：%s, 耗时：%s", count, (System.currentTimeMillis() - start) + " ms"));
    }
}
