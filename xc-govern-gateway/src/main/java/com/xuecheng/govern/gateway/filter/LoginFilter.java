package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    AuthService authService;

    @Override
    public String filterType() {
        return "pre";   //四种类型：pre、routing、post、error
    }

    @Override
    public int filterOrder() {
        return 0;    //int值来定义过滤器的执行顺序，数值越小优先级越高
    }

    @Override
    public boolean shouldFilter() {
        return true;    // 该过滤器需要执行
    }

    @Override
    public Object run() throws ZuulException {
        // 上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 请求对象
        HttpServletRequest request = requestContext.getRequest();
        // 查询身份令牌
        String access_token = authService.getTokenFromCookie(request);
        if(access_token == null){
            // 拒绝访问
            access_denied();
            return null;
        }
        long expire = authService.getExpire(access_token);
        if(expire <= 0){
            // 拒绝访问
            access_denied();
            return null;
        }
        String jwtFromHeader = authService.getJwtFromHeader(request);
        if(jwtFromHeader == null){
            // 拒绝访问
            access_denied();
            return null;
        }

        return null;
    }

    // 拒绝访问
    private void access_denied(){
        // 上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 拒绝访问
        requestContext.setSendZuulResponse(false);
        // 设置响应内容
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String jsonString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(jsonString);
        // 设置状态码
        requestContext.setResponseStatusCode(200);

        HttpServletResponse response = requestContext.getResponse();
        response.setContentType("application/json;charset=utf-8");
    }
}
