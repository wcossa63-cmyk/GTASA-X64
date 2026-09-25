package com.blrp.launcher.config;

import com.blrp.launcher.NetworkService;

public class Config {
    public static final String APK_FILE_NAME = "russia.apk";

    public static final String ZIP_FILES_BASE_ADR = NetworkService.FILES_BASE_ADR + "zip/";
    public static final String FILE_INFO_URL = NetworkService.FILES_BASE_ADR + "files.php";

    public static final String URL_RE_CAPTCHA = "https://t.me/a_r_m_6_4";
    private static final String URL_DONATE = "https://t.me/a_r_m_6_4";

    public static final String FORUM_URL = "https://t.me/a_r_m_6_4";
    public static final String DONATE_URL = "https://t.me/a_r_m_6_4";
    public static final String TELEGRAM_URI = "https://t.me/a_r_m_6_4";

    public static final String NATIVE_SETTINGS_FILE_PATH = "/SAMP/settings.ini";
    public static final String SETTINGS_FILE_PATH = "/gta_sa.set";

    public static String createBillingUri(String serverId, String serverName, String sum, String nickname, String mail, String captcha) {
        return String.format(
                URL_DONATE,
                serverId,
                serverName,
                sum,
                nickname,
                mail,
                captcha
        );
    }
}