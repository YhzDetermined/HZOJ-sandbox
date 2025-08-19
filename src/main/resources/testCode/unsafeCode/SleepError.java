import java.io.IOException;

/**
 * 无限睡眠
 */
public class Main{
    public static void main(String[] args) throws IOException, InterruptedException {
        long ONE_HOUR=60*60*1000L;
        Thread.sleep(ONE_HOUR);
        System.out.println("睡完了");
    }
}
