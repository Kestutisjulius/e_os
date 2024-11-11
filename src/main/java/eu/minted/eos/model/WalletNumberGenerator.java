package eu.minted.eos.model;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.concurrent.atomic.AtomicLong;

public class WalletNumberGenerator {

    private static final String PREFIX = "EOS-M";
    private static final int COUNTER_LENGTH = 10;
    private static final AtomicLong counter = new AtomicLong(1);

    public static String generateWalletNumber() {
        String timestamp = String.valueOf(Instant.now().getLong(ChronoField.INSTANT_SECONDS));
        String counterString = String.format("%0" + COUNTER_LENGTH + "d", counter.getAndIncrement());

        return PREFIX + timestamp + counterString;
    }
}
