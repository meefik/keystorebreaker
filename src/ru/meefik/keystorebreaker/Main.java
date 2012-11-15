package ru.meefik.keystorebreaker;

import java.io.File;

public class Main {

	public static void main(String[] args) {
		if (args.length != 5) {
			System.out
					.println("Usage: java -jar KeystoreBreaker.jar <keystore file> <sequence> <first passwd> <last passwd> <number of threads>");
			return;
		}
		String ksFile = "test.jks";
		char[] char_seq = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
		char[] firstPwd = "1234".toCharArray();
		char[] lastPwd = "zzzz".toCharArray();
		int threads = 1;

		try {
			ksFile = args[0];
			char_seq = args[1].toCharArray();
			firstPwd = args[2].toCharArray();
			lastPwd = args[3].toCharArray();
			threads = Integer.parseInt(args[4]);
		} catch (Throwable t) {
			System.out.println("Error parse input parameters!");
			return;
		}

		File f = new File(ksFile);
		if (!f.exists()) {
			System.out.println("Keystore file not found!");
			return;
		}

		new Manager(ksFile, char_seq, firstPwd, lastPwd, threads);
	}

}
