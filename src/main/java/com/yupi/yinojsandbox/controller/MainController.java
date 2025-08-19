package com.yupi.yinojsandbox.controller;

import com.yupi.yinojsandbox.JavaDockerCodeSandbox;
import com.yupi.yinojsandbox.JavaNativeCodeSandbox;
import com.yupi.yinojsandbox.model.ExecuteCodeRequest;
import com.yupi.yinojsandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class MainController {
    private static final String AUTH_REQUEST_HEADER="auth";
    private static final String AUTH_REQUEST_SECRET="secretKey";
//    @Resource
//    private JavaNativeCodeSandbox javaNativeCodeSandbox;
    @Resource
    private JavaDockerCodeSandbox javaDockerCodeSandbox;

    @GetMapping("/health")
    public String healthCheck(){
        return "ok";
    }


    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request, HttpServletResponse response){
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if(!authHeader.equals(AUTH_REQUEST_SECRET)){
            response.setStatus(403);
            return null;
        }
        if(executeCodeRequest == null){
            throw new RuntimeException("请求参数为空");
        }
//        return javaNativeCodeSandbox.executeCode(executeCodeRequest);
        return javaDockerCodeSandbox.executeCode(executeCodeRequest);
    }
}
