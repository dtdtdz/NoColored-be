package com.ssafy.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class SchedulerConfig {

    @Bean
    @Primary
    public ScheduledExecutorService primaryScheduledExecutorService() {
        return Executors.newScheduledThreadPool(5);
    }
    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(1); // 예: 스레드 풀 크기를 1로 설정
    }
    @Bean
    public ScheduledExecutorService authScheduledExecutorService(){
        return Executors.newScheduledThreadPool(1);
    }
    // 스프링 컨텍스트가 종료될 때 이벤트 리스너
    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
        ScheduledExecutorService primaryScheduledExecutorService = event.getApplicationContext()
                .getBean("primaryScheduledExecutorService", ScheduledExecutorService.class);
        shutdownAndAwaitTermination(primaryScheduledExecutorService);
        ScheduledExecutorService executorService = event.getApplicationContext()
                .getBean("scheduledExecutorService",ScheduledExecutorService.class);
        shutdownAndAwaitTermination(executorService);
        ScheduledExecutorService authExecutorService = event.getApplicationContext()
                .getBean("authScheduledExecutorService", ScheduledExecutorService.class);
        shutdownAndAwaitTermination(authExecutorService);
    }
    // 스레드 풀 종료를 위한 메서드
    void shutdownAndAwaitTermination(ScheduledExecutorService pool) {
//        System.out.println("shut");
        pool.shutdown(); // 스레드 풀 종료를 시도
        try {
            // 스레드 풀이 종료될 때까지 기다림
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // 종료되지 않으면 즉시 종료를 시도

                // 스레드 풀이 즉시 종료될 때까지 기다림
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("스레드 풀이 즉시 종료되지 않았습니다.");
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            // 현재 스레드가 대기 중 인터럽트 되면 즉시 종료
            pool.shutdownNow();
            // 현재 스레드의 인터럽트 상태를 복구
            Thread.currentThread().interrupt();
        }
    }
}
