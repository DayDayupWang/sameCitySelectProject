package com.example.sameCity.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sameCity.common.R;
import com.example.sameCity.entity.User;
import com.example.sameCity.service.UserService;
import com.example.sameCity.utils.MailUtils;
import com.example.sameCity.utils.ValidateCodeUtils;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;



    // 发送邮箱验证码
    //public Boolean sendMsg(@RequestBody User user, HttpSession session) throws MessagingException {
    //    // 1.获取前端传来的用户邮箱
    //    String email = user.getEmail();
    //    // 2.如果邮箱不为空才进行下一步操作
    //    if (!email.isEmpty()) {
    //        // 2.1 随机生成六位数验证码
    //        String code = MailUtils.getCode();
    //        // 2.2 发送验证码邮件
    //        MailUtils.sendMail(email, code);
    //        // 2.3 将验证码存入到Redis中，key是email，value是验证码，有效期设置为5分钟
    //        redisTemplate.opsForValue().set(email, code, 5, TimeUnit.MINUTES);
    //        return true;
    //    }
    //    return false;
    //}

    // 发送手机验证码
    /**
     * 发送手机短信验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")  //用@RequestBody接收来自JSON的数据
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //1获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            //2生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("验证码在此 ：code={}", code);

            //3发送短信 签名，模板code，发送的手机号，生成的验证码
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);
            //4.1需要将生成的验证码保存到session
            //session.setAttribute(phone,code);

            //将生成的验证码缓存到Redis中
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            //log.info("session里phone对应：", session.getAttribute(phone).toString());
            return R.success("手机验证码短信发送成功");
        }
        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     *
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从Session中获取保存的验证码
        //Object codeInSession = session.getAttribute(phone);
        //从redis中获取保存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if (codeInSession != null && codeInSession.equals(code)) {
            //如果能够比对成功，说明登录成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);
            if (user == null) {
                //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());

            //如果用户登录成功，删除Redis中缓存的验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("登录失败");
    }


    // 移动端用户退出登录
    @PostMapping("/loginout")
    public R<String> logout(HttpSession session) {
        if (userService.logout(session)) {
            return R.success("退出成功");
        }
        return R.error("退出失败");
    }
}
