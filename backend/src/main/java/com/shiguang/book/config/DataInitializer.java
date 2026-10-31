package com.shiguang.book.config;

import com.shiguang.book.entity.Book;
import com.shiguang.book.entity.User;
import com.shiguang.book.mapper.BookMapper;
import com.shiguang.book.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 首次启动时写入演示账号和演示书籍。
 * 之所以用代码而不是 SQL 插入用户：BCrypt 密文必须是运行时算出来的，
 * 直接写死在 sql 里一旦算错，全组都登不上。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public void run(ApplicationArguments args) {
        if (userMapper.selectCount(null) > 0) {
            log.info("已存在用户数据，跳过演示数据初始化");
            return;
        }

        User yang = user("13800000001", "杨昀博", "计算机学院");
        User li = user("13800000002", "李卓轩", "软件学院");
        User chen = user("13800000003", "陈意", "计算机学院");
        List.of(yang, li, chen).forEach(userMapper::insert);

        book("高等数学（第七版）上册", "教材", 8, "18.00", "大一用书，笔记不多，无缺页", yang.getId(), 1);
        book("数据结构（C语言版）", "考研", 9, "25.00", "严蔚敏版，考研 408 必备，几乎全新", yang.getId(), 1);
        book("计算机网络（第 8 版）", "教材", 7, "20.00", "谢希仁，画了几处重点", li.getId(), 1);
        book("操作系统概念（第 9 版）", "考研", 8, "30.00", "恐龙书中文版，盒装", li.getId(), 1);
        book("编译原理（龙书）", "考研", 6, "15.00", "有点旧，但不影响看", li.getId(), 0);
        book("线性代数辅导讲义", "考研", 9, "12.00", "李永乐，全新未拆", chen.getId(), 1);
        book("Java 编程思想（第 4 版）", "课外", 7, "35.00", "厚，适合当砖头", chen.getId(), 1);
        book("概率论与数理统计", "教材", 8, "16.00", "浙大版，附习题册", chen.getId(), 1);

        log.info("演示数据初始化完成：3 个账号（13800000001/2/3，密码均为 123456），8 本书");
    }

    private User user(String phone, String nickname, String dept) {
        User u = new User();
        u.setPhone(phone);
        u.setPassword(encoder.encode("123456"));
        u.setNickname(nickname);
        u.setDept(dept);
        u.setAvatar("/files/default-avatar.png");
        u.setStatus(1);
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }

    private void book(String title, String category, int condition, String price,
                      String desc, Long sellerId, int status) {
        Book b = new Book();
        b.setTitle(title);
        b.setCategory(category);
        b.setConditionLevel(condition);
        b.setPrice(new BigDecimal(price));
        b.setDescription(desc);
        b.setCover(null);
        b.setSellerId(sellerId);
        b.setStatus(status);
        b.setDeleted(0);
        b.setCreatedAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        bookMapper.insert(b);
    }
}
