package com.corecompass.finance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication
@EnableDiscoveryClient
@org.springframework.cloud.openfeign.EnableFeignClients
public class FinanceServiceApplication {
    public static void main(String[] args) { SpringApplication.run(FinanceServiceApplication.class, args); }
}
