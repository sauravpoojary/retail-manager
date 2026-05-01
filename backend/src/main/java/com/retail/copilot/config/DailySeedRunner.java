package com.retail.copilot.config;

import com.retail.copilot.service.DailySeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runs once on every application startup.
 * Delegates to DailySeedService to insert today's (and yesterday's) random
 * demo data if it doesn't already exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailySeedRunner implements ApplicationRunner {

    private final DailySeedService dailySeedService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Running daily seed check...");
        try {
            dailySeedService.seedTodayIfMissing();
        } catch (Exception e) {
            // Never crash startup due to seed failure
            log.error("Daily seed failed (non-fatal): {}", e.getMessage(), e);
        }
    }
}
