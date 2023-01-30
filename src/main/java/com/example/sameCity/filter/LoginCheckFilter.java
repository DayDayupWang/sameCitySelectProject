package com.example.sameCity.filter;


import com.alibaba.fastjson.JSON;
import com.example.sameCity.common.BaseContext;
import com.example.sameCity.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;//因为父类没有getUrl方法
        HttpServletResponse response = (HttpServletResponse) servletResponse;//因为父类没有getUrl方法
        //1.获取本次请求的url
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}", requestURI);
        //定义不需要处理的路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",//静态的可以放行，动态的会拦截
                "/front/**",//静态的可以放行，动态的会拦截
                "/common/**",
                "/user/sendMsg",
                "/user/login",
                "/doc.html",
                "webjars/**",
                "/swagger-resources",
                "/v2/api-docs"

        };
        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3.如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        //4-1.判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("employee"));
            //展示对应的线程
//            Long id = Thread.currentThread().getId();
//            log.info("线程id为：{}",id);
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setThreadLocal(empId);
            filterChain.doFilter(request, response);
            return;
        }

        //4-2.判断移动端登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("user"));
            //展示对应的线程
//            Long id = Thread.currentThread().getId();
//            log.info("线程id为：{}",id);
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setThreadLocal(userId);
            filterChain.doFilter(request, response);
            return;
        }
        log.info("用户未登录");
        //5.如果未登录则返回未登录结果，通过输出流方式向客户端响应数据
        //根据前端写后端未登录的话把对应提示传过去
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
            return;
    }


    /**
     * 路径匹配，检查请求是否需要放行
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
