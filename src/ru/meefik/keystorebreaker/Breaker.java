package ru.meefik.keystorebreaker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

public class Breaker extends Thread {

	private ByteArrayInputStream buf;
	// private char[] char_seq =
	// "0123456789abcdefjhijklmnopqrstuvwxyz".toCharArray();
	private char[] char_seq;
	private int seq_len;
	private int[] int_seq;
	private int depth;
	private char[] passphrase;
	private boolean found = false;
	private boolean alive = false;
	public final char[] firstPwd;
	public final char[] lastPwd;

	public Breaker(String ksFile, char[] seq, char[] fPwd, char[] lPwd,
			int threads) {
		try {
			File file = new File(ksFile);
			FileInputStream fis = new FileInputStream(file);
			byte[] fileBytes = new byte[(int) file.length()];
			fis.read(fileBytes);
			buf = new ByteArrayInputStream(fileBytes);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// normalize
		char[] nPwd = new char[lPwd.length];
		for (int i = 0; i < lPwd.length; i++) {
			if (i < fPwd.length)
				nPwd[i] = fPwd[i];
			else
				nPwd[i] = seq[0];
		}

		this.firstPwd = nPwd;
		this.lastPwd = lPwd;
		this.passphrase = this.firstPwd;
		this.char_seq = seq;
		this.seq_len = char_seq.length;
		setIntSeq(this.firstPwd);
		// System.out.println(getPwd());
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
		KeyStore ks = null;
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		while (alive) {
			buf.reset();
			try {
				// System.out.println(getPwd());
				if (!nextIntSeq())
					break;
				passphrase = getPwd();
				// if (Arrays.equals(passphrase,lastPwd)) alive = false;
				ks.load(buf, passphrase);
				found = true;
				alive = false;
			} catch (Throwable t) {
				continue;
			}
		}
		alive = false;
	}

}
