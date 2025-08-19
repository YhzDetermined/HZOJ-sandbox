package com.yupi.yinojsandbox.utils;

import com.yupi.yinojsandbox.model.ExecuteCodeResponse;
import com.yupi.yinojsandbox.model.ExecuteMessage;
import com.yupi.yinojsandbox.model.JudgeInfo;

public class MakeCompileErrorResponse {
    public static ExecuteCodeResponse makeCompileErrorResponse(ExecuteMessage executeMessage) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setStatus(0);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setCompileMsg(executeMessage.getErrorMessage());
        response.setJudgeInfo(judgeInfo);
        response.setMessage("编译错误");
        return response;
    }
}
