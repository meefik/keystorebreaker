package ru.meefik.keystorebreaker;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Breaker extends Thread {

    private static final int DIGEST_LENGTH = 20;

    private final char[] char_seq;
    private final int seq_len;
    private int[] int_seq;
    private int depth;
    private char[] passphrase;
    private boolean found = false;
    private boolean alive = false;
    public final char[] firstPwd;
    public final char[] lastPwd;
    private final byte[] keystore;
    private final byte[] signature;
    private final byte[] salt;
    private final MessageDigest md;

    public Breaker(String ksFile, char[] seq, char[] fPwd, char[] lPwd, int threads) {

        final File file = new File(ksFile);
        this.keystore = new byte[(int) file.length() - DIGEST_LENGTH];
        this.signature = new byte[DIGEST_LENGTH];

        DataInputStream stream = null;
        try {
            stream = new DataInputStream(new FileInputStream(file));
            this.md = MessageDigest.getInstance("SHA");
            this.salt = "Mighty Aphrodite".getBytes("UTF8");
            stream.readFully(keystore);
            stream.readFully(signature);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to initialize salt: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize keystore data and signature: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to initialize MessageDigest: " + e.getMessage(), e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }

        // normalize
        char[] nPwd = new char[lPwd.length];
        for (int i = 0; i < lPwd.length; i++) {
            if (i < fPwd.length) nPwd[i] = fPwd[i];
            else nPwd[i] = seq[0];
        }

        this.firstPwd = nPwd;
        this.lastPwd = lPwd;
        this.passphrase = this.firstPwd;
        this.char_seq = seq;
        this.seq_len = char_seq.length;
        setIntSeq(this.firstPwd);
    }

    public char[] getPasswd() {
        return passphrase;
    }

    public boolean isFound() {
        return found;
    }

    public boolean isActive() {
        return alive;
    }

    public void inactive() {
        alive = false;
    }

    private void setIntSeq(char[] pwd) {
        depth = pwd.length;
        int_seq = new int[depth];
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < seq_len; j++) {
                if (char_seq[j] == pwd[i]) {
                    int_seq[i] = j;
                }
            }
        }
    }

    private boolean nextIntSeq() {
        for (int i = 0; i < depth; i++) {
            if (int_seq[i] < seq_len - 1) {
                int_seq[i]++;
                return true;
            } else {
                int_seq[i] = 0;
            }
        }
        return false;
    }

    private char[] getPwd() {
        char[] pwd = new char[depth];
        for (int i = 0; i < depth; i++) {
            pwd[i] = char_seq[int_seq[i]];
        }
        return pwd;
    }

    @Override
    public void run() {
        alive = true;
        while (alive) {
            if (!nextIntSeq()) {
                break;
            }
            passphrase = getPwd();

            final MessageDigest digest = getPreKeyedHash(passphrase);
            digest.update(this.keystore);
            final byte[] result = digest.digest();
            if (!Arrays.equals(signature, result)) {
                continue;
            }

            found = true;
            alive = false;
        }
        alive = false;
    }

    /**
     * Generate a new SHA message digest, based on the password
     */
    private MessageDigest getPreKeyedHash(char[] password) {
        this.md.reset();
        int i, j;

        byte[] passwdBytes = new byte[password.length * 2];
        for (i = 0, j = 0; i < password.length; i++) {
            passwdBytes[j++] = (byte) (password[i] >> 8);
            passwdBytes[j++] = (byte) password[i];
        }
        md.update(passwdBytes);
        md.update(salt);
        return md;
    }

}
