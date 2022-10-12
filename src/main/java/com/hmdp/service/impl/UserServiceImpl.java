package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {

        //验证手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //不符合返回格式错误信息
            return Result.fail("手机号格式不正确");
        }

        //生成验证码
        String code = RandomUtil.randomString(6);

        //保存到session中
        session.setAttribute("code", code);

        //发送验证码
        log.debug("手机验证码：code{}" + code);

        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //验证提交的手机号和验证码
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误！");
        }
        String code = loginForm.getCode();
        //获取session的信息
        Object sessionCode = session.getAttribute("code");
        if (code == null || !sessionCode.toString().equals(code)) {
            return Result.fail("验证码错误！");
        }
        //根据手机号查询用户
        User user = query().eq("phone", phone).one();
        if (user == null) {
            //用户不存在，创建新用户并且保存到数据库当中
            user = createUserWithPhone(phone);
        }
        //保存用户信息到session
        session.setAttribute("user", user);
        return null;
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;
    }

}
