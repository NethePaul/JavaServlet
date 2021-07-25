import java.time.Duration;
import java.time.Instant;

import javax.servlet.http.Cookie;

public class Session {
    private String sessionKey = null;
    private int nameHash_ = -1;
    private int accid_ = -1;
    private Instant laTime = null;
    private String name_ = null;
    private static final int expirationTime = 5;// time for the session to expire in minuts after the last access

    public Session() {
        encryption.prepare();
    }

    public Cookie createSession(String name, int accid, int sessionId) {
        System.out.println("creating session");
        laTime = Instant.now();
        Cookie c = new Cookie("Session", sessionId + "|" + encryption.SHA512(accid + "" + name + laTime.toString()));
        c.setPath("\\");
        sessionKey = c.getValue();
        accid_ = accid;
        nameHash_ = database.getNameHash(name);
        name_ = name;
        System.out.println("created session");
        System.out.println("sid: " + sessionId);
        System.out.println("skey: " + sessionKey);
        return c;
    }

    public boolean isActive() {
        return laTime != null && Duration.between(laTime, Instant.now()).getSeconds() < expirationTime * 60;
    }

    /**
     * auth if the key is correct
     * 
     * @param key passed by client
     * @return {nameHash:int, account id:int} for valid key, or null for invalid key
     */
    public int[] authSession(String key) {
        System.out.println("active: " + isActive());
        System.out.println("key user  : " + key);
        System.out.println("key server: " + sessionKey);
        boolean a = isActive() && key.equals(sessionKey);
        if (a) {
            laTime = Instant.now();
            int[] b = new int[2];
            b[0] = nameHash_;
            b[1] = accid_;
            return b;
        }
        return null;
    }

    public static String getSessionKey(Cookie[] c) {
        for (int i = c.length; i-- > 0;)
            if (c[i].getName().equals("Session"))
                return c[i].getValue();
        return null;
    }

    public static int getSessionId(String key) {
        System.out.println("key: " + key);
        String n = key.split("\\|")[0];
        System.out.println("sid_str: " + n);
        return Integer.parseInt(n);
    }

    public void endSession() {
        laTime = null;
        sessionKey = null;
    }

    public String getName() {
        return name_;
    }
}
