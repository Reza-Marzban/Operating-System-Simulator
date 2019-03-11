/*
f. This class is the Binary_Hex_Convert class that has two main method for converting Hex to binary and Binary to Hex.
g.---
*/
import java.util.*;
import java.math.BigInteger;

public class Binary_Hex_convert
{
	static String HexToBin(String s) {
		return new BigInteger(s, 16).toString(2);
	}
	static String BinToHex(String s) {
		return new BigInteger(s, 2).toString(16);
	}
}