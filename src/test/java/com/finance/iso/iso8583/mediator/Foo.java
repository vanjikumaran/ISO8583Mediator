package com.finance.iso.iso8583.mediator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Foo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pattern pattern = Pattern.compile("/:/gi");
		Matcher matcher = pattern.matcher("aaaaa:CCCCCCC:ddDDDDD");
		if (matcher.matches()) {
			System.out.println("adfadsfdsf");
			String matchedValue = matcher.group(0);
			System.out.println(matchedValue);
		}

	}

}
