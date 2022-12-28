package dev.wiji.instancemanager.misc;

import java.util.TreeMap;

public class AUtil {

	private static final TreeMap<Integer, String> map = new TreeMap();

	public AUtil() {
	}

	public static String toRoman(int number) {
		int l = (Integer) map.floorKey(number);
		return number == l ? (String) map.get(number) : (String) map.get(l) + toRoman(number - l);
	}

	static {
		map.put(1000, "M");
		map.put(900, "CM");
		map.put(500, "D");
		map.put(400, "CD");
		map.put(100, "C");
		map.put(90, "XC");
		map.put(50, "L");
		map.put(40, "XL");
		map.put(10, "X");
		map.put(9, "IX");
		map.put(5, "V");
		map.put(4, "IV");
		map.put(1, "I");
	}
}
