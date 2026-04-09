package com.uab.sante.utils;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class NumberGenerator {

    public synchronized String generateNumeroFeuille() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Utiliser timestamp + random pour garantir l'unicité
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return String.format("F%s%d%d", date, timestamp, random);
    }

    public synchronized String generateNumeroOrdonnance() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return String.format("ORD%s%d%d", date, timestamp, random);
    }

    public synchronized String generateNumeroBulletin() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return String.format("BLT%s%d%d", date, timestamp, random);
    }
}
