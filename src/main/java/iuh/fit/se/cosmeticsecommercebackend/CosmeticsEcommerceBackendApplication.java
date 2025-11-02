package iuh.fit.se.cosmeticsecommercebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class CosmeticsEcommerceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CosmeticsEcommerceBackendApplication.class, args);
    }
    @EventListener(ApplicationReadyEvent.class)
    public void printUrl() {
        System.out.println("Ứng dụng đã khởi động: http://localhost:8085/");
    }
}

