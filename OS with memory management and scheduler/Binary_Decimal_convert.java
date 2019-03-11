/*
f. This class is the Binary_Decimal_convert class that has two main method for converting Decimal to binary and Binary to Decimal.
g.---
*/
import java.util.*;
public class Binary_Decimal_convert
{

public static int[] DecToBin(int number){
String binary=Integer.toString(number,2);
String binary2="";
if(number>=0){
	int t=(16-binary.length());
	String a=String.format("%0" + t + "d", 0);
	binary=a+binary;
	int[] numbers = Arrays.stream(binary.split("")).mapToInt(Integer::parseInt).toArray();
	return numbers;		
}else{
	binary=binary.substring(1);
	int t=(16-binary.length());
	String a=String.format("%0" + t + "d", 0);
	binary=a+binary;
	for(int k=0;k<binary.length();k++)
	{
		if(binary.charAt(k)=='0'){binary2+="1";}else{binary2+="0";}
	}
	binary=binary2;
	String s1=binary;
	String s2="1";
	int first = s1.length() - 1;
	int second = s2.length() - 1;
	StringBuilder sb = new StringBuilder();
	int carry = 0;
	while (first >= 0 || second >= 0) {
		int sum = carry;
		if (first >= 0) {
			sum += s1.charAt(first) - '0';
			first--;
		}
		if (second >= 0) {
			sum += s2.charAt(second) - '0';
			second--;
		}
		carry = sum >> 1;
		sum = sum & 1;
		sb.append(sum == 0 ? '0' : '1');
	}
	if (carry > 0)
		sb.append('1');

	sb.reverse();
	binary=String.valueOf(sb);
	int[] numbers = Arrays.stream(binary.split("")).mapToInt(Integer::parseInt).toArray();
	return numbers;	
}

}
public static int BinToDec(int[] numbers){
String binary =Arrays.toString(numbers).replaceAll("[^0-9]", "");
int i = Integer.parseInt(binary, 2);
String kStr = generateKString(binary);
int k =  Integer.parseInt(kStr, 2);
if (i >= k) i -= 2 * k;
return i;
}
public static String generateKString(String binary){
// k is the smallest positive number the actual binary scheme cannot represent
// for 1101 it is 1000
String kStr = "1";
for(int i = 1; i < binary.length(); i++)
	kStr += "0";
return kStr;
}
}
