package com.corecompass.habits;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication @EnableDiscoveryClient
public class HabitsServiceApplication {
    public static void main(String[] args) { SpringApplication.run(HabitsServiceApplication.class, args); }
}
