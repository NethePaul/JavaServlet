import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(urlPatterns = { "/*" })
public class Servlet extends HttpServlet {
    database user = new database();
    Session[] session = new Session[100];// max 100 sessions at once
    final static String dir = "C:/Users/Admin/Downloads/apache-tomcat-9.0.50/webapps/hello/WEB-INF/";

    public Servlet() {
        for (int i = session.length; i-- > 0;)
            session[i] = new Session();
    }

    public static void main(String[] args) {

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        PrintWriter out = response.getWriter();
        System.out.println("----------REQUEST----------\n" + request.getRequestURI());
        if (request.getRequestURI().endsWith("/global.js")) {
            response.setContentType("text/javascript");
            System.out.println("sending global.js");
            out.println(loadFile("global.js"));
        } else {
            try {
                SessionInfo sinfo = authRequest(request);
                if (sinfo == null) {
                    throw new Exception("session not found");
                } else {
                    response.setContentType("text/html");
                    out.print(loadFile("index.html").replace("<!--REPLACE-->",
                            loadFile("user.html").replace("<!--NAME-->", session[sinfo.sid].getName())));
                    return;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            response.setContentType("text/html");
            out.print(loadFile("index.html").replace("<!--REPLACE-->", loadFile("login.html")));

        }
        out.close();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text");
        String firstline = request.getReader().readLine();
        if (firstline.equals("login"))
            login(request, response);
        else if (firstline.equals("logout"))
            logout(request, response);
        else if (firstline.equals("register"))
            register(request, response);
        else if (firstline.equals("delete account"))
            delacc(request, response);
    }

    public void delacc(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("----------DELETE ACCOUNT----------");
        String password = request.getReader().readLine();
        SessionInfo sinfo = authRequest(request);
        try {
            if (!user.authAccount(sinfo.nameHash, sinfo.accid, password)) {
                PrintWriter out = response.getWriter();
                ReplaceJs(out, "msg", loadFile("wrong_password.html"));
                out.close();
                user.unload();
                return;
            }
            user.unload();
            user.delAccount(sinfo.nameHash, sinfo.accid);
            PrintWriter out = response.getWriter();
            ReplaceJs(out, "body", loadFile("login.html"));
            out.close();
        } catch (Exception e) {
            PrintWriter out = response.getWriter();
            ReplaceJs(out, "msg", loadFile("internal_error.html"));
            out.close();
        }
    }

    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("----------LOGIN----------");
        String name = request.getReader().readLine();
        // name = request.getReader().readLine();
        String password = request.getReader().readLine();
        try {
            PrintWriter out = response.getWriter();
            int nameHash = database.getNameHash(name);
            int accid = user.findAccountId(name);
            if (accid < 0) {
                ReplaceJs(out, "msg", loadFile("wrong_credentials.html"));
                out.close();
                return;
            }
            if (!user.authAccount(nameHash, accid, password)) {
                ReplaceJs(out, "msg", loadFile("wrong_credentials.html"));
                out.close();
                user.unload();
                return;
            }
            user.unload();

            int sid = findFreeSession();
            if (sid == -1) {
                ReplaceJs(out, "msg", loadFile("server_full.html"));
                out.close();
                return;
            }
            Cookie c = session[sid].createSession(name, accid, sid);
            ReplaceJs(out, "body", loadFile("user.html").replace("<!--NAME-->", session[sid].getName()));
            response.addCookie(c);
            out.close();
        } catch (Exception e) {
            PrintWriter out = response.getWriter();
            ReplaceJs(out, "msg", loadFile("internal_error.html"));
            out.close();
            System.out.println("cannot login Account");
            System.out.println(e.getMessage());
        }

    }

    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("----------LOGOUT----------");
        // just delete all cookies
        Cookie[] c = request.getCookies();
        for (Cookie co : c) {
            co.setMaxAge(0);
            response.addCookie(co);
        }
        PrintWriter out = response.getWriter();
        ReplaceJs(out, "body", loadFile("login.html"));
        out.close();
    }

    public void register(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("----------REGISTER----------");
        String name = request.getReader().readLine();
        // name = request.getReader().readLine();
        String password = request.getReader().readLine();
        if (name == null || password == null) {
            PrintWriter out = response.getWriter();
            ReplaceJs(out, "msg", loadFile("credentials_required.html"));
            out.close();
            System.out.println("cannot create Account");
            return;
        }
        System.out.println(name);
        System.out.println(password);
        try {
            if (!validateName(name)) {
                PrintWriter out = response.getWriter();
                ReplaceJs(out, "msg", loadFile("msg_invalid_name.html"));
                out.close();
                System.out.println("invalid name");
                return;
            }
            int accid = user.createAccount(name, password);
            if (accid < 0) {
                PrintWriter out = response.getWriter();
                ReplaceJs(out, "msg", loadFile("name_already_used.html"));
                out.close();
                System.out.println("account already exists");
                return;
            }
            System.out.println("created Account " + name + " : " + password);
            Cookie c = null;
            int sid = findFreeSession();
            if (sid < 0) {
                PrintWriter out = response.getWriter();
                ReplaceJs(out, "msg", loadFile("server_full.html"));
                out.close();
                System.out.println("server full");
                return;
            }
            c = session[sid].createSession(name, accid, sid);
            if (c != null)
                response.addCookie(c);
            PrintWriter out = response.getWriter();
            ReplaceJs(out, "body", loadFile("user.html").replace("<!--NAME-->", session[sid].getName()));
            out.close();
        } catch (Exception e) {
            PrintWriter out = response.getWriter();
            ReplaceJs(out, "msg", loadFile("internal_error.html"));
            out.close();
            System.out.println("cannot create Account");
            System.out.println(e.getMessage());
        }
    }

    private int findFreeSession() {
        for (int sid = 100; sid-- > 0;)
            if (!session[sid].isActive())
                return sid;
        return -1;
    }

    public static String loadFile(String path) {
        System.out.println("loading file: " + path);
        try {
            return new String(Files.readAllBytes(Paths.get(dir + path)));
        } catch (Exception e) {
            System.out.println("could not laod file: " + path + " - " + e.getMessage());
            return "";
        }
    }

    public boolean validateName(String name) {
        return name != null && name.split("[^a-zA-Z0-9]").length == 1;// check for non alphanumeric letters
    }

    /**
     * 
     */
    public SessionInfo authRequest(HttpServletRequest request) {
        try {
            String key = Session.getSessionKey(request.getCookies());
            if (key == null) {
                System.out.println("no session cookie");
                return null;
            }
            int id = Session.getSessionId(key);
            if (id < 0 || id >= session.length) {
                System.out.println("cannot extract id");
                return null;
            }
            System.out.println("sid: " + id);
            int[] data = session[id].authSession(key);
            if (data == null) {
                System.out.println("cannot authenticate session");
                return null;
            }
            return new SessionInfo(id, data[0], data[1]);
        } catch (Exception e) {
            return null;
        }
    }

    class SessionInfo {
        public SessionInfo(int sid_, int nameHash_, int accid_) {
            sid = sid_;
            nameHash = nameHash_;
            accid = accid_;
        }

        public int sid;
        public int nameHash;
        public int accid;
    }

    /**
     * prints the contents of replace.js that replaces the html element , that has
     * the Id id, with Replace all newline characters will be removed
     * 
     * @param out     the printwriter element that it is printing to
     * @param Id      the id of the html element to replace
     * @param Replace the text to replace it with
     */
    public void ReplaceJs(PrintWriter out, String Id, String Replace) {
        out.print(loadFile("replace.js").replace("ID", Id).replace("REPLACE", Replace.replace("'", "\\'"))
                .replace("\n", "").replace("\r", "")
                .replace("\\n", "\\\\n")/* this bug was frustating to trach down */);
    }
}