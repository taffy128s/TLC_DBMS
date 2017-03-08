package com.github.taffy128s.tlcdbms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	public static int state;
	public static ArrayList<Table> tables = new ArrayList<>();
	
	public static void main(String[] args) throws IOException {
		state = 0;
		String line;
		Scanner sc = new Scanner(System.in);
		System.out.print(">> ");
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			if (line.equals("")) {
				System.out.print(">> ");
				continue;
			}
			line = stringInsertSpaces(line);
			String[] splited = line.split("\\s+");
			if (state == State.ERROR) state = State.START;
			for (String origin : splited) {
				String temp = origin.toLowerCase();
				switch (state) {
					case State.START: {
						if (temp.equals("create")) {
							System.out.println("Create received.");
							state = State.CREATE_PARSED;
						} else if (temp.equals("insert")) {
							System.out.println("Insert received.");
							// TODO: insert parsing...
						} else handleError("Error near '" + origin + "'.");
						break;
					}
				}
				if (state == State.ERROR) break;
			}
			System.out.print(">> ");
		}
		sc.close();
	}
	
	public static void handleError(String errMsg) {
		state = State.ERROR;
		tables.clear();
		System.out.println(errMsg);
	}

	/**
	 * Insert spaces into a String.
	 * 
	 * @param origin
	 * @return string with spaces near "(),;"
	 */
	public static String stringInsertSpaces(String origin) {
		String temp = "";
		for (char c : origin.toCharArray()) {
			if (c == '(' || c == ')' || c == ',' || c == ';') {
				temp += " ";
				temp += c;
				temp += " ";
			} else temp += c;
		}
		return temp;
	}
	
}
