package ru.mephi.malskiy.notification;

import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SmsNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);

    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsNotificationService() {
        Properties config = loadConfig();

        this.host = config.getProperty("smpp.host");
        this.port = Integer.parseInt(config.getProperty("smpp.port"));
        this.systemId = config.getProperty("smpp.system_id");
        this.password = config.getProperty("smpp.password");
        this.systemType = config.getProperty("smpp.system_type");
        this.sourceAddress = config.getProperty("smpp.source_addr");
    }

    @Override
    public void sendCode(String destination, String code) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("SMS destination is required");
        }

        SMPPSession session = new SMPPSession();

        try {
            BindParameter bindParameter = new BindParameter(
                BindType.BIND_TX,
                systemId,
                password,
                systemType,
                TypeOfNumber.UNKNOWN,
                NumberingPlanIndicator.UNKNOWN,
                sourceAddress
            );

            session.connectAndBind(host, port, bindParameter);

            session.submitShortMessage(
                systemType,
                TypeOfNumber.UNKNOWN,
                NumberingPlanIndicator.UNKNOWN,
                sourceAddress,
                TypeOfNumber.UNKNOWN,
                NumberingPlanIndicator.UNKNOWN,
                destination,
                new ESMClass(),
                (byte) 0,
                (byte) 1,
                null,
                null,
                new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                (byte) 0,
                new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                (byte) 0,
                ("Your code: " + code).getBytes(StandardCharsets.UTF_8)
            );

            logger.info("SMS with OTP sent successfully to {}", destination);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS", e);
        } finally {
            try {
                session.unbindAndClose();
            } catch (Exception e) {
                logger.warn("Failed to close SMPP session cleanly", e);
            }
        }
    }

    private Properties loadConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sms.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("sms.properties not found");
            }

            Properties props = new Properties();
            props.load(inputStream);
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sms configuration", e);
        }
    }
}
