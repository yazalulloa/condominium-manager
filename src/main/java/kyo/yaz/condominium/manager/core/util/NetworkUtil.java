package kyo.yaz.condominium.manager.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class NetworkUtil {

    public static String getPublicIp() {
        try {
            String urlString = "http://checkip.amazonaws.com/";
            final var url = new URL(urlString);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
