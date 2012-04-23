package com.felees.hbnpojogen.randomlib.data.dataGeneration;

import java.util.Locale;
import java.util.Random;

import com.felees.hbnpojogen.randomlib.data.CountryCodes;


/** 
 * A class providing methods for generating random data for complex data types.
 * 
 * @author robertam
 */
public class ComplexDataTypes {

	/**
	 * Random Number Generator.
	 */
	public final static Random generator = new Random();
	
	/**
	 * Generates a random ID for Client or Provider.
	 * @return A random ID for Client or Provider
	 */	
	public static Long generateID() {		
		long id = generator.nextLong();
		
		while (id <= 0) {
			id = generator.nextLong();
		}
		
		return id;
	}
	
	/**
	 * Generates a random IP.
	 * @return an Ip Address
	 */
	public static String generateRandomIp() {
		String ip = generator.nextInt(254) + "." + generator.nextInt(254) + "." + generator.nextInt(254) + "." + generator.nextInt(254);
		return ip;
	}
	
	/**
	 * Generates a random URL.
	 * @return a random url
	 */
	public static String generateRandomUrl() {
		StringBuffer sb = new StringBuffer();
		sb.append("http://www.");
		sb.append(BasicDataGenerator.generateRandomStringChar(10).toLowerCase(Locale.getDefault()));
		sb.append(".com");
		return sb.toString();	
	}
	
	/**
	 * Returns a random country code in the form of 3 digits.
	 * 
	 * @return	3-digit country code
	 */
	public static Integer generateNumericCountryCode() {
		int choice = generator.nextInt(CountryCodes.getCountryCodesNumLength());
		return Integer.parseInt(CountryCodes.getCountryCodesNum(choice));
	}
	
	/**
	 * Returns a random country code in the form of 2 letters. 
	 * 
	 * @return	2-letter country code
	 */
	public static String generateCountryCode() {
		int choice = generator.nextInt(CountryCodes.getCountryCodesAcLength());
		return CountryCodes.getCountryCodesAc(choice);
	}
	
}
