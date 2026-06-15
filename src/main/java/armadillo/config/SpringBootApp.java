package armadillo.config;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Spring Boot 应用配置类
 * 用于启动嵌入式HTTP服务器，提供REST API接口
 */
@SpringBootApplication(
        scanBasePackages = "armadillo",
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
public class SpringBootApp {

    private static final Logger logger = Logger.getLogger(SpringBootApp.class);

    /**
     * 启动Spring Boot HTTP服务器
     * 在独立线程中运行，不阻塞主程序
     */
    public static void start() {
        new Thread(() -> {
            try {
                logger.info("Spring Boot HTTP Server starting on port 8080...");
                SpringApplication.run(SpringBootApp.class);
                logger.info("Spring Boot HTTP Server started successfully on port 8080");
            } catch (Exception e) {
                logger.error("Spring Boot HTTP Server start failed", e);
            }
        }, "SpringBoot-HTTP").start();
    }
}
