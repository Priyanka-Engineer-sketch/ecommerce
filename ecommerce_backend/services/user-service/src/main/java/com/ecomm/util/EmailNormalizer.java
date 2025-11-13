package com.ecomm.util;

import java.util.Locale;

public final class EmailNormalizer {
    private EmailNormalizer() {}
    public static String canonical(String email) {
        if (email == null) return null;
        String e = email.trim().toLowerCase(Locale.ROOT);
        int at = e.indexOf('@');
        if (at < 0) return e;
        String local = e.substring(0, at);
        String domain = e.substring(at + 1);
        int plus = local.indexOf('+');
        if (plus >= 0) local = local.substring(0, plus);
        return local + "@" + domain;
    }
}
