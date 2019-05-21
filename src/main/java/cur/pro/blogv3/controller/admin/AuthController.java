package cur.pro.blogv3.controller.admin;

import cur.pro.blogv3.constant.WebConst;
import cur.pro.blogv3.controller.BaseController;
import cur.pro.blogv3.dto.LogActions;
import cur.pro.blogv3.exception.TipException;
import cur.pro.blogv3.modal.Bo.RestResponseBo;
import cur.pro.blogv3.modal.Vo.UserVo;
import cur.pro.blogv3.service.ILogServcie;
import cur.pro.blogv3.service.IUserService;
import cur.pro.blogv3.utils.Commons;
import cur.pro.blogv3.utils.TaleUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
@RequestMapping("/admin")
@Transactional(rollbackFor = TipException.class)
@Slf4j
public class AuthController extends BaseController {

    @Resource
    private IUserService usersService;

    @Resource
    private ILogServcie logService;

    @GetMapping(value = "/login")
    public String login() {
        return "admin/login";
    }

    /**
     * 管理后台登录
     * @param username
     * @param password
     * @param remeber_me
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "login")
    @ResponseBody
    public RestResponseBo doLogin(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam(required = false) String remeber_me,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        Integer error_count = cache.get("login_error_count");
        try {
            UserVo user = usersService.login(username, password);
            request.getSession().setAttribute(WebConst.LOGIN_SESSION_KEY, user);
            if (StringUtils.isNotBlank(remeber_me)) {
                TaleUtils.setCookie(response, user.getUid());
            }
            logService.insertLog(LogActions.LOGIN.getAction(), null, request.getRemoteAddr(), user.getUid());
        } catch (Exception e) {
            error_count = null == error_count ? 1 : error_count + 1;
            if (error_count > 3) {
                return RestResponseBo.fail("您输入密码已经错误超过3次，请10分钟后尝试");
            }
            cache.set("login_error_count", error_count, 10 * 60);
            String msg = "登录失败";
            if (e instanceof TipException) {
                msg = e.getMessage();
            } else {
                log.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

    /**
     * 注销
     * @param session
     * @param response
     */
    @RequestMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        session.removeAttribute(WebConst.LOGIN_SESSION_KEY);
        Cookie cookie = new Cookie(WebConst.USER_IN_COOKIE, "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        try {
            //response.sendRedirect(Commons.site_url());
            response.sendRedirect(Commons.site_login());
        } catch (IOException e) {
            e.printStackTrace();
            log.error("注销失败", e);
        }
    }
}
