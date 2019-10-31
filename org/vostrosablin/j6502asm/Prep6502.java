package org.vostrosablin.j6502asm;
//J6502ASM - 6502 Assembler Preprocessor (Prep6502)

//    This file is part of J6502ASM.
//
//    J6502ASM is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    J6502ASM is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with J6502ASM.  If not, see <http://www.gnu.org/licenses/>.

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class Prep6502 {
	private static String readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}
	private static ArrayList<String> toStringList (String input) //Slice string into list of lines
	{
		ArrayList<String> list = new ArrayList<String>(0);
		String buffer;
		buffer = "";
		for (int i = 0;i<input.length();i++)
		{
			if (input.charAt(i) == '\n')
			{
				buffer += " \n";
				list.add(buffer);
				buffer = "";
			}
			else buffer += input.charAt(i);
		}
		if (buffer != "")
		{
			if (buffer.charAt(buffer.length()-1) != '\n') buffer += " \n";
			list.add(buffer);
		}
		return list;
	}
	private static ArrayList<String> stringListToUpper (ArrayList<String> input) //Convert all strings to uppercase
	{
		ArrayList<String> list = input;
		for (int i = 0;i<input.size();i++)
		{
			list.set(i,input.get(i).toUpperCase(Locale.ENGLISH));
		}
		return list;
	}
	private static ArrayList<String> stripComments (ArrayList<String> input) //Strip all comments from input strings
	{
		for (int i = 0;i < input.size();i++)
		{
			String result = input.get(i).split(";")[0];
			if (result != input.get(i)) input.set(i, result + '\n');
		}
		return input;
	}
	private static String trimLeft (String s)
	{
		int start = -1;
		for (int i = 0;i < s.length();i++)
		{
			if (" \t".contains(""+s.charAt(i))) start = i;
			else break;
		}
		return s.substring(start+1);
	}
	private ArrayList<String> depResolve (ArrayList<String> input) throws IOException //Insert all included files
	{
		for (int i = input.size()-1;i >= 0;i--)
		{
			if (trimLeft(input.get(i)).startsWith(".INCLUDE "))
			{
				String temp = input.get(i).split("\\.INCLUDE ",2)[1].trim();
				boolean chg = false;
				if (temp.startsWith("\"")) {chg = true; temp = temp.substring(1);}
				if (temp.endsWith("\"")) {chg = true; temp = temp.substring(0, temp.length()-1);};
				if (chg) {temp = temp.trim();}
				input.remove(i);
				input.addAll(i,preprocess(temp)); //We use recursive inclusion. Should be fine for normal operation and will crash fast enough in case of circular dependency.
			}
		}
		return input;
	}
	public ArrayList<String> preprocess(String filename) throws IOException
	{
		ArrayList<String> asmcode = stringListToUpper(depResolve(stripComments(toStringList(readFile(filename))))); //Read file into string, split into a list of strings, strip comments, resolve includes, convert to uppercase.
		return asmcode;
	}
}
