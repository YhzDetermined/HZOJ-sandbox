package com.yupi.yinojsandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;

import java.util.List;

public class DockerDemo {
//    获取默认的docker client
public static void main(String[] args) throws InterruptedException {
    DockerClient dockerClient = DockerClientBuilder.getInstance().build();
    String image = "nginx:latest";
//    创建容器
    CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
    CreateContainerResponse createContainerResponse = containerCmd.withCmd("echo","Hello Docker!").exec();
    System.out.println(createContainerResponse.toString());
    String containerId = createContainerResponse.getId();
//    查看容器状态
    ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
    List<Container> containerList = listContainersCmd.withShowAll(true).exec();
    for(Container container : containerList) {
        System.out.println(container.toString());
    }
//启动
    dockerClient.startContainerCmd(containerId).exec();
//    查看日志
    LogContainerResultCallback logContainerResultCallback = new LogContainerResultCallback() {
        @Override
        public void onNext(Frame item) {
            System.out.println(item.getStreamType());
            System.out.println("日志：" + new String(item.getPayload()));
            super.onNext(item);
        }
    };
//    阻塞等待日志输出
    dockerClient.logContainerCmd(containerId)
            .withStdErr(true)
            .withStdOut(true)
            .exec(logContainerResultCallback)
            .awaitCompletion();
    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
    dockerClient.removeImageCmd(image).exec();
}
}
