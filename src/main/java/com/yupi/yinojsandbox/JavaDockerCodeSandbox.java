package com.yupi.yinojsandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.InvocationBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.yupi.yinojsandbox.constant.DockerConstant;
import com.yupi.yinojsandbox.model.ExecuteCodeRequest;
import com.yupi.yinojsandbox.model.ExecuteCodeResponse;
import com.yupi.yinojsandbox.model.ExecuteMessage;
import com.yupi.yinojsandbox.model.JudgeInfo;
import com.yupi.yinojsandbox.utils.ProcessUtils;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
@Component
public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate{
    private static final long TIME_OUT=3000L;
    private static Boolean FIRST_INIT=true;
    private static DockerClient dockerClient;
    static{
        dockerClient = DockerClientBuilder.getInstance().build();
        List<Image> images = dockerClient.listImagesCmd().exec();
        // 打印每个镜像的详细信息
        for (Image image : images) {
            if (image.getRepoTags() != null) {
                for (String tag : image.getRepoTags()) {
                    System.out.println(tag);
                    if(tag .equals(DockerConstant.JDK_IMAGE) ){
                        System.out.println("存在相关镜像");
                        FIRST_INIT=false;
                        break;
                    }
                }
            } else {
                System.out.println("标签: 无标签");
            }
            if(!FIRST_INIT){
                break;
            }
        }
    }
    public static void main(String[] args) {
        JavaDockerCodeSandbox javaDockerCodeSandbox = new JavaDockerCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2","1 3"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/UnsafeCode/RunFileError.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaDockerCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList,Long timeLimit,Long memoryLimit) {
        System.out.println(timeLimit);
        System.out.println(memoryLimit);
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        File file = new File(userCodeParentPath);
//        String ppPath = file.getParentFile().getAbsolutePath();
//        String curpName = file.getName();
//        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        // 拉取镜像
        if(FIRST_INIT){
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(DockerConstant.JDK_IMAGE);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像：" + item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        .awaitCompletion();
                FIRST_INIT = false;
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
        }

        System.out.println("已存在镜像");
        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(DockerConstant.JDK_IMAGE);
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory( memoryLimit * 1800 *1024L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        hostConfig.setBinds(new Bind(userCodeParentPath,new Volume("/app")));
//        hostConfig.setBinds(new Bind(ppPath,new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();
//        String containerId = "7b138178c0f4907a32d2d9351893b343b6ce3519fa3a2f5cbe3293603fcb3c1d";
        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        //        docker exec  happy_ellis java -cp /app Main 1 3
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
//        执行命令获取结果
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String[] inputArgsArray = inputArgs.split(" ");
//            String classPath = "/app/" + curpName;
//            String[] cmdArray = ArrayUtil.append(new String[] {"java","-cp",classPath,"Main"},inputArgsArray);
            String[] cmdArray = ArrayUtil.append(new String[] {"java","-cp","/app","Main"},inputArgsArray);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
           System.out.println("创建执行命令：" + execCreateCmdResponse);
            ExecuteMessage executeMessage = new ExecuteMessage();
//            final String[] message = {null};
            StringBuilder messageBuilder = new StringBuilder();
//            final String[] errorMessage = {null};
            StringBuilder errorBuilder = new StringBuilder();
            Long time = 0L;
            final boolean[] timeout = {true};
            String execId = execCreateCmdResponse.getId();
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    // 如果执行完成，则表示没超时
                    timeout[0] = false;
                    super.onComplete();
                }
                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
//                        errorMessage[0] = new String(frame.getPayload());
//                        System.out.println("输出错误结果：" + errorMessage[0]);
                        errorBuilder.append(new String(frame.getPayload()));

                    } else {
//                        message[0] = new String(frame.getPayload());
//                        System.out.println("输出结果：" + message[0]);
                        messageBuilder.append(new String(frame.getPayload()));
                    }
                    super.onNext(frame);
                }
            };
            Closeable[] closeableHolder = new Closeable[1];
            final long[] maxMemory = {0L};
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);

            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                   System.out.println("容器" + containerId + "内存占用：" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }

                @Override
                public void close() throws IOException {

                }

                @Override
                public void onStart(Closeable closeable) {
                    closeableHolder[0] = closeable;
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }
            });
//            statsCmd.exec(statisticsResultCallback);
            try {
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(timeLimit+1000L, TimeUnit.MILLISECONDS);
//                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                if (closeableHolder[0] != null) {
                    closeableHolder[0].close();
                }
                if (statisticsResultCallback != null) {
                    statisticsResultCallback.close(); // 非常重要！
                }
                statsCmd.close();
            } catch (Exception e) {
                System.out.println("程序执行异常");
                throw new RuntimeException(e);
            }
//            executeMessage.setMessage(message[0]);
            executeMessage.setMessage(messageBuilder.toString());
            executeMessage.setErrorMessage(errorBuilder.toString());
            executeMessage.setMemory(maxMemory[0]);
            executeMessage.setTime(time);
            executeMessageList.add(executeMessage);
            if(time > timeLimit || maxMemory[0] > memoryLimit*1024*1024 || StrUtil.isNotBlank(errorBuilder.toString())){
//            if(time > timeLimit || maxMemory[0] > memoryLimit*1024*1024){
                System.out.println("该测试用例未通过！");
                break;
            }

        }
        return executeMessageList;
    }
}
