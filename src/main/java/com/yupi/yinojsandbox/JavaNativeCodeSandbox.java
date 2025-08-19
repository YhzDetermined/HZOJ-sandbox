package com.yupi.yinojsandbox;

import com.yupi.yinojsandbox.model.ExecuteCodeRequest;
import com.yupi.yinojsandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate{
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
//        TODO：NativeCodeSandBox无法统计占用内存
        return super.executeCode(executeCodeRequest);
    }
}
