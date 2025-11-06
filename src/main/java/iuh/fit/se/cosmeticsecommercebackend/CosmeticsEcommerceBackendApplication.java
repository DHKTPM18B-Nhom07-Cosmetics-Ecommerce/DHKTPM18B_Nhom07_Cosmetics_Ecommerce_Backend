package iuh.fit.se.cosmeticsecommercebackend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class CosmeticsEcommerceBackendApplication {

    private final Environment env;

    public CosmeticsEcommerceBackendApplication(Environment env) {
        this.env = env;
    }

    // Static block: load .env.<profile>
    static {
        // Lấy profile hiện tại hoặc mặc định "dev"
        String activeProfile = System.getProperty("spring.profiles.active",
                System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "development"));

        String envFile = ".env." + activeProfile;
        System.out.println("Loading environment file: " + envFile);

        // Load .env.<profile>, nếu không có thì fallback sang .env
        Dotenv dotenv = Dotenv.configure()
                .filename(envFile)
                .directory(".")
                .ignoreIfMissing()
                .load();

        if (dotenv.entries().isEmpty()) {
            dotenv = Dotenv.configure()
                    .filename(".env")
                    .directory(".")
                    .ignoreIfMissing()
                    .load();
        }

        // Các biến cần thiết (bổ sung thêm nếu muốn)
        String[] keys = {
                "DB_HOST", "DB_PORT", "DB_NAME", "DB_USER", "DB_PASSWORD",
                "JWT_SECRET", "JWT_EXP_MINUTES"
        };

        for (String key : keys) {
            String value = dotenv.get(key, System.getProperty(key, ""));
            if (!value.isEmpty()) {
                System.setProperty(key, value);
            }
        }

        System.out.println("Environment loaded for profile: " + activeProfile);
    }

    public static void main(String[] args) {
        SpringApplication.run(CosmeticsEcommerceBackendApplication.class, args);
    }

    // In ra URL, Swagger, profile sau khi app khởi động
    @EventListener(WebServerInitializedEvent.class)
    public void onWebServerReady(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();

        String host = env.getProperty("server.address", "localhost");
        if ("0.0.0.0".equals(host) || "::".equals(host)) host = "localhost";

        String contextPath = normalize(env.getProperty("server.servlet.context-path", ""));
        String profile = env.getProperty("spring.profiles.active", "development");

        // Swagger paths
        String swaggerUiPath = env.getProperty("springdoc.swagger-ui.path", "/swagger-ui/index.html");
        String apiDocsPath = env.getProperty("springdoc.api-docs.path", "/v3/api-docs");

        String base = "http://" + host + ":" + port + contextPath;

        System.out.println("\n================= Cosmetics Ecommerce Backend =================");
        System.out.println("🌍 Profile:        " + profile);
        System.out.println("🚀 Running at:     " + base + "/");
        System.out.println("📘 Swagger UI:     " + join(base, swaggerUiPath));
        System.out.println("🧾 OpenAPI JSON:   " + join(base, apiDocsPath));
        System.out.println("==============================================================\n");
    }

    // ===== Helper methods =====
    private static String normalize(String ctx) {
        if (ctx == null || ctx.isBlank()) return "";
        if (!ctx.startsWith("/")) ctx = "/" + ctx;
        if (ctx.endsWith("/")) ctx = ctx.substring(0, ctx.length() - 1);
        return ctx;
    }

    private static String join(String base, String path) {
        if (path == null || path.isBlank()) return base;
        if (!path.startsWith("/")) path = "/" + path;
        return base + path;
    }
}
