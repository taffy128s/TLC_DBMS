package com.github.taffy128s.tlcdbms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	public static int state; //States are defined in State class.
	public static ArrayList<Table> tables = new ArrayList<>(); //All of the tables in this database.
	
	public static void main(String[] args) throws IOException {
		state = State.START;
		String line;
		Scanner sc = new Scanner(System.in);
		//Printing ">> " means that it's OK to give input now.
		System.out.print(">> ");
		//EOF ends this program.
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			//Handle empty line.
			if (line.equals("")) {
				System.out.print(">> ");
				continue;
			}
			//Insert spaces next to ',' or '(' or ')'.
			line = stringInsertSpaces(line);
			//Split the line using spaces.
			String[] splited = line.split("\\s+");
			//Go back to initial state if last one was an error.
			if (state == State.ERROR) state = State.START;
			//Start to parse the words.
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
					case State.CREATE_PARSED: {
					    if (temp.equals("table")) {
					        System.out.println("Table received.");
					        state = State.CREATE_TABLE_PARSED;
					    } else handleError("Error near '" + origin + "'.");
					    break;
					}
					case State.CREATE_TABLE_PARSED: {
					    // Feel tired.....
					}
				}
				if (state == State.ERROR) break;
			}
			System.out.print(">> ");
		}
		if (state == State.CREATE_TABLE_RIGHT_PARSED) {
		    System.out.println("\nEOF received. Create table successfully.");
		}
		sc.close();
	}
	
	/**
	 * Set the state to ERROR, clear temporary table, and print the error message.
	 * 
	 * @param errMsg
	 */
	public static void handleError(String errMsg) {
		state = State.ERROR;
		// TODO: clear temporary table.
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
