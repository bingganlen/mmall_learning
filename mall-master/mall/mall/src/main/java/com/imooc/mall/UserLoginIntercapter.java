package com.imooc.mall;

import com.imooc.mall.consts.MallConst;
import com.imooc.mall.exception.UserLoginException;
import com.imooc.mall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**\
 * 判断用户登录状态 拦截器
 */
@Slf4j
public class UserLoginIntercapter implements HandlerInterceptor {

    /**\
     * true 表示继续流程   false 表示中断
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle...");
        User user = (User) request.getSession().getAttribute(MallConst.CURRENT_USER);
        if (user == null) {
            log.info("user=null");
            throw new UserLoginException();
            //虽然只能返回boolean类型  直接抛异常就会被拦截了
            //RuntimeExceptionHandler  userLoginHandle


//			response.getWriter().print("error");
//			return false;
//			return ResponseVo.error(ResponseEnum.NEED_LOGIN);
        }
        return true;
    }

}
