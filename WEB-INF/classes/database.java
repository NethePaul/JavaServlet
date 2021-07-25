import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class database {
    private final static String dir = "C:/Users/Admin/Downloads/apache-tomcat-9.0.50/webapps/hello/WEB-INF/accounts/";
    public List<String> data = null;

    public database() {
        encryption.prepare();
    }

    public void load(Path f) throws Exception {
        unload();
        data = Files.readAllLines(f, StandardCharsets.UTF_8);
    }

    public void save(Path f) throws Exception {
        if (data != null)
            Files.write(f, data, StandardCharsets.UTF_8);
    }

    public void unload() {
        if (data != null)
            data.clear();// no need to wait for garbage collector
        data = null;
    }

    /**
     * find the account in the database hashes the name
     * 
     * @return account id, or -1 when no account found, or -2 when the database
     *         cannot be loaded
     */
    public int findAccountId(String name) {
        int nameHash = getNameHash(name);
        String nameSHA = encryption.SHA512(name);
        System.out.println(name);
        System.out.println(nameSHA);

        String filepath = dir + "accounts_" + nameHash + "/database.data";
        if (Files.exists(Paths.get(filepath))) {
            try {
                load(Paths.get(filepath));

                for (int accid = 0; accid < data.size(); accid += 2) {
                    System.out.println(accid + ":  " + data.get(accid));
                    System.out.println(" vs " + nameSHA);
                    if (data.get(accid).equals(nameSHA)) {
                        System.out.println("correct");
                        unload();
                        return accid;
                    }
                    System.out.println("wrong");
                }
                unload();
                return -1;
            } catch (Exception e) {
                // System.out.println(e.getStackTrace().toString());
                e.printStackTrace();
                return -2;
            }
        }
        return -1;
    }

    /**
     * creates a new account hashes the name
     * 
     * @return account id or -1 when account already used
     * @throws Exception when it cannot create the account files
     */
    public int createAccount(String name, String password) throws Exception {
        try {
            if (findAccountId(name) >= 0) {
                System.out.println("did not create account");
                return -1;
            }
        } catch (Exception e) {
        }
        String nameSHA = encryption.SHA512(name);
        System.out.println(name);
        System.out.println(nameSHA);
        int nameHash = getNameHash(name);
        String filepath = dir + "accounts_" + nameHash + "\\database.data";
        if (!Files.exists(Paths.get(filepath))) {
            try {
                Files.createDirectories(Paths.get(dir + "accounts_" + nameHash));
            } catch (IOException e) {
            }
            Files.createFile(Paths.get(filepath));
        }

        load(Paths.get(filepath));
        int accid = -1;
        for (int l = data.size(); l-- > 0;)
            if (data.get(l).equals(""))
                accid = l;
        if (accid == -1)
            accid = data.size() / 2;
        String accFilePath = dir + "accounts_" + nameHash + "\\acc_" + accid + ".data";
        data.add(nameSHA);
        data.add(accFilePath);
        Files.createFile(Paths.get(accFilePath));
        save(Paths.get(filepath));
        load(Paths.get(accFilePath));
        data.add(encryption.SHA512(password + nameHash + accid));
        save(Paths.get(accFilePath));
        unload();
        System.out.println("created account");
        return accid;

    }

    public static int getNameHash(String name) {
        return (name.hashCode() % 127);
    }

    public void loadAccount(int nameHash, int accid) throws Exception {
        String filepath = dir + "accounts_" + nameHash + "\\database.data";
        load(Paths.get(filepath));
        load(Paths.get(data.get(accid + 1)));
    }

    public void delAccount(int nameHash, int accid) throws Exception {
        String filepath = dir + "accounts_" + nameHash + "\\database.data";
        load(Paths.get(filepath));
        data.set(accid, "");
        String b = data.get(accid + 1);
        data.set(accid + 1, "");
        save(Paths.get(filepath));
        Files.delete(Paths.get(b));
        unload();
    }

    public boolean authUser(int nameHash, int accid, String password) throws Exception {
        loadAccount(nameHash, accid);
        return data.get(0).equals(encryption.SHA512(password + nameHash + accid));
    }
}
