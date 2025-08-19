package com.yupi.yinojsandbox;


import com.yupi.yinojsandbox.model.ExecuteCodeRequest;
import com.yupi.yinojsandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {
    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
