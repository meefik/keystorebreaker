package ru.meefik.keystorebreaker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Manager {

    private char[] char_seq;
    private int seq_len;

    public Manager(String ksFile, char[] seq, char[] firstPwd, char[] lastPwd,
            int threads) {
        this.char_seq = seq;
        this.seq_len = char_seq.length;

        try {
            String passphrase = "";
            long t;
            long sum;
            boolean found = false;
            boolean alive = true;
            long amount = getNumber(lastPwd) - getNumber(firstPwd);
            long timeLeft;
            long speed;

            System.out.println("Keystore: " + ksFile);
            System.out.println("Threads: " + threads);
            System.out.println("Sequence: " + String.valueOf(char_seq));
            System.out.println("First password: " + String.valueOf(firstPwd));
            System.out.println("Last password: " + String.valueOf(lastPwd));
            System.out.println("Combinations: " + amount);
            System.out.println("Distribution by threads: ");

            long s = getNumber(firstPwd);
            Breaker[] br = new Breaker[threads];
            for (int i = 0; i < threads; i++) {
                long n = amount / threads;
                if (i == 0) {
                    n += amount % threads;
                }
                char[] fp = getPasswd(s, lastPwd.length);
                s += n;
                char[] lp = getPasswd(s, lastPwd.length);

                System.out.println("#" + i + ": " + String.valueOf(fp) + " - "
                        + String.valueOf(lp));

                br[i] = new Breaker(ksFile, char_seq, fp, lp, threads);
                br[i].start();
                br[i].setPriority(Thread.NORM_PRIORITY + 1);
            }

            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("GMT+0"));

            long fp;
            long lp;
            long cp;

            System.out.println("Processing: ");

            t = System.currentTimeMillis();
            while (alive) {
                Thread.sleep(1000);
                sum = 0;
                alive = false;
                for (int i = 0; i < threads; i++) {
                    fp = getNumber(br[i].firstPwd);
                    lp = getNumber(br[i].lastPwd);
                    cp = getNumber(br[i].getPasswd());

                    sum += cp - fp;
                    if (br[i].isFound()) {
                        passphrase = String.valueOf(br[i].getPasswd());
                        found = true;
                    }
                    if (cp > lp || found) {
                        br[i].inactive();
                    }
                    alive = br[i].isActive() || alive;

                    System.out.println("#"
                            + i
                            + ": "
                            + String.valueOf(br[i].getPasswd())
                            + " / "
                            + String.valueOf(br[i].lastPwd)
                            + "   "
                            + String.valueOf((cp - fp) * 100 / (lp - fp))
                            + "% "
                            + "[ "
                            + String.valueOf((cp - fp) * 1000
                                    / (System.currentTimeMillis() - t))
                            + " pwd/s ]");
                }

                speed = (sum * 1000 / (System.currentTimeMillis() - t));
                timeLeft = (amount - sum) / speed; // sec

                System.out.print("Total: " + (sum * 100 / amount) + "% [ "
                        + speed + " pwd/s ]  ");
                System.out.println("Time left: " + timeLeft / (24 * 60 * 60)
                        + "." + df.format(new Date(timeLeft * 1000)));
            }

            if (found) {
                System.out.println("Passphrase found: " + passphrase);
            } else {
                System.out.println("Passphrase not found :(");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long getNumber(char[] x) {
        long res = 0;
        for (int i = 0; i < x.length; i++) {
            // System.out.println(String.valueOf(x[i])+" "+getPos(x[i])+"*"+Math.pow(seq_len,i));
            res += getPos(x[i]) * Math.pow(seq_len, i);
        }
        return res;
    }

    private int getPos(char c) {
        for (int i = 0; i < seq_len; i++) {
            if (char_seq[i] == c) {
                return i;
            }
        }
        return -1;
    }

    private char[] getPasswd(long x, int l) {
        int d;
        String s = "";
        while (x >= seq_len) {
            d = (int) (x % seq_len);
            x = x / seq_len;
            s += char_seq[d];
        }
        d = (int) x;
        s += char_seq[d];
        for (int i = s.length(); i < l; i++) {
            s += char_seq[0];
        }
        return s.toCharArray();
    }

}
