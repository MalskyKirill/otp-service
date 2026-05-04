package ru.mephi.malskiy.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.dao.OtpCodeDao;


import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OtpExpirationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OtpExpirationScheduler.class);

    private final OtpCodeDao otpCodeDao;
    private final ScheduledExecutorService executorService;

    public OtpExpirationScheduler(OtpCodeDao otpCodeDao) {
        this.otpCodeDao = otpCodeDao;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        executorService.scheduleAtFixedRate(() -> {
            try {
                int expiredCount = otpCodeDao.marcExpiredCodes(LocalDateTime.now());

                if (expiredCount > 0) {
                    logger.info("Expired {} OTP code(s)", expiredCount);
                }
            } catch (Exception e) {
                logger.error("Failed to expire OTP codes", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void stop() {
        executorService.shutdown();
    }
}
