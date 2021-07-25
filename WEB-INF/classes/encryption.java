import java.security.MessageDigest;
import java.math.BigInteger;

public class encryption {
    public static MessageDigest sha256 = null;
    public static MessageDigest sha512 = null;

    public static void main(String[] args) {
        prepare();
        for (int i = args.length; i-- > 0;)
            System.out.println(SHA512(args[i]));
        for (int i = args.length; i-- > 0;)
            System.out.println(SHA256(args[i]));
    }

    public encryption() {
        prepare();
    }

    /**
     * preapres the algorythms for use
     * 
     * @return whether it was successfull
     */
    public static boolean prepare() {
        try {
            if (sha256 == null)
                sha256 = MessageDigest.getInstance("SHA-256");
            if (sha512 == null)
                sha512 = MessageDigest.getInstance("SHA-512");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * runs sha512, prepare() has to be called at least once before this function is
     * called
     * 
     * @param in
     * @return a String representation of the result with a radix of 64 of
     *         alphanumeric, '+' and '-' charachters
     */
    public static String SHA512(String in) {
        return bigintToRadix64(new BigInteger(sha512.digest(in.getBytes())));
    }

    /**
     * runs sha512, prepare() has to be called at least once before this function is
     * called
     * 
     * @param in
     * @return a String representation of the result with a radix of 64 of
     *         alphanumeric, '+' and '-' charachters
     */
    public static String SHA256(String in) {
        return bigintToRadix64(new BigInteger(sha256.digest(in.getBytes())));
    }

    /**
     * only allows a max radix of 36 this does the same as BigInteger.toString(x)
     * but with a radix of 64 while BigInteger.toString(x) has a max radix of 36
     * uses 0-9 for first 10 digits uses a-b for next 26 digits : 36 total uses A-B
     * for next 26 digits : 62 total uses + for 63rd digit uses - for 64th digit
     * 
     * @param in the number
     * @return a String representation of the number with radix 64
     */
    public static String bigintToRadix64(BigInteger in) {
        StringBuilder buffer = new StringBuilder();

        while (in.bitLength() > 0) {
            byte[] c = in.toByteArray();
            byte t = (byte) (c.length > 0 ? c[c.length - 1] & 0b111111 : 0);
            if (t < 10)
                t += ((byte) '0');
            else if (t < 36)
                t += ((byte) 'a') - 10;
            else if (t < 62)
                t += ((byte) 'A') - 36;
            else if (t == 62)
                t = ((byte) '+');
            else if (t == 63)
                t = ((byte) '-');
            buffer.insert(0, (char) t);
            in = in.shiftRight(6);
        }
        return buffer.toString();
    }
}
