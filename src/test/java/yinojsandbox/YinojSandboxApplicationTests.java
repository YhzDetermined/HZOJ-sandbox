package yinojsandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import com.yupi.yinojsandbox.YinojSandboxApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest(classes = YinojSandboxApplication.class)
class YinojSandboxApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void getDockerImage() throws IOException {
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        try {
            // 获取当前所有镜像
            List<Image> images = dockerClient.listImagesCmd().exec();

            // 打印每个镜像的详细信息
            for (Image image : images) {
                System.out.println("镜像 ID: " + image.getId());
                if (image.getRepoTags() != null) {
                    for (String tag : image.getRepoTags()) {
                        System.out.println("标签: " + tag);
                    }
                } else {
                    System.out.println("标签: 无标签");
                }
            }
        } catch (Exception e) {
            System.out.println("获取镜像时发生错误: " + e.getMessage());
        } finally {
            dockerClient.close();
        }
    }

}
