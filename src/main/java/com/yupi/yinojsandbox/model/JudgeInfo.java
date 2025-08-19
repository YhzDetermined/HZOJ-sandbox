package com.yupi.yinojsandbox.model;

import lombok.Data;

/**
 * 判题信息
 */
@Data
public class JudgeInfo {
    /**
     * 程序执行信息
     */
    private String message;
    /**
     * 消耗内存（KB）
     */
    private Long memory;
    /**
     * 消耗时间
     */
    private Long time;
    /**
     * 编译报错信息
     */
    private String compileMsg;
}
