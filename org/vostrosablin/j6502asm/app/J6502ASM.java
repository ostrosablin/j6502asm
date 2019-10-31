package org.vostrosablin.j6502asm.app;
//J6502ASM - Java 6502 (Cross) Assembler

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.vostrosablin.j6502asm.Lex6502;
import org.vostrosablin.j6502asm.Lex6502.LexerError;
import org.vostrosablin.j6502asm.Parse6502;
import org.vostrosablin.j6502asm.Parse6502.ParserError;
import org.vostrosablin.j6502asm.Prep6502;

import static java.lang.System.*;

public class J6502ASM {

	/**
	 * @param args
	 * @throws ParserError 
	 * @throws LexerError 
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		out.println("J6502ASM Cross-Assembler v0.11b");
		ArrayList<ArrayList<Lex6502.LexToken>> tokens = null;
		String filename = "";
		String outfilename = "out.bin";
		Option inpf = new Option("i", "if", true, "Input File");
		inpf.setArgs(1);
		inpf.setOptionalArg(false);
		inpf.setArgName("Input File ");
		Option outf = new Option("o", "of", true, "Output File");
		outf.setArgs(1);
		outf.setOptionalArg(false);
		outf.setArgName("Output File ");
		Option prnc = new Option("p", "printcode", false, "Print Output");
		Option cutn = new Option("n", "cutnull", false, "Cut Leading Null Bytes in Output");
		Options opts = new Options();
		opts.addOption(cutn);
		opts.addOption(prnc);
		opts.addOption(inpf);
		opts.addOption(outf);
		CommandLineParser cliparser = new PosixParser();
		CommandLine cln = null;
		try {
			cln = cliparser.parse(opts, args);
		} catch (ParseException e) {
			e.printStackTrace();
			exit(1);
		}
		if (!cln.hasOption('i'))
		{
			out.println("Input file is not specified. Aborted.");
			exit(1);
		}
		else filename = cln.getOptionValue('i');
		if (cln.hasOption('o')) outfilename = cln.getOptionValue('o');
		out.println("Assembling file " + filename + " into " + outfilename);
		long starttime,tasktime,endtime;
		out.print("Stage 1 - Preprocessing...");
		starttime = tasktime = System.nanoTime();
		Prep6502 prepr = new Prep6502();
		ArrayList<String> asmcode = null;
		try {
			asmcode = prepr.preprocess(filename);
		} catch (IOException e) {
			e.printStackTrace();
			exit(1);
		}
		prepr = null;
		endtime = System.nanoTime();
		out.println(" done in " + ((double)(endtime-starttime) / 1000000000.0));
		out.print("Stage 2 - Lexical analysis...");
		tasktime = System.nanoTime();
		Lex6502 lexer = new Lex6502(asmcode);
		try {
			tokens = lexer.tokenizeInput();
		} catch (LexerError e) {
			e.printStackTrace();
			exit(1);
		}
		lexer = null;
		endtime = System.nanoTime();
		out.println(" done in " + ((double)(endtime-tasktime) / 1000000000.0));
		out.print("Stage 3 - Parsing and assembling...");
		tasktime = System.nanoTime();
		Parse6502 parser = new Parse6502(tokens);
		ArrayList<Byte> bin = null;
		try {
			bin = parser.parseAndAssemble();
		} catch (ParserError e) {
			e.printStackTrace();
			exit(1);
		}
		parser = null;
		int n = bin.size();
		int first = 0;
		if (cln.hasOption('n'))
		{
			for (int i = 0; i < n; i++)
			{
				if (!(bin.get(i) == 0)) {first = i; break;}
				if (i == (n-1)) {first = n;}
			}
		}
		byte[] bino = new byte[n-first];
		for (int i = 0; i < (n-first); i++) {
			bino[i] = bin.get(first+i);
		}
		try {
			FileOutputStream of = new FileOutputStream(new File(outfilename));
			of.write(bino);
			of.close();
			of = null;
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			exit(1);
		} catch (IOException e)
		{
			e.printStackTrace();
			exit(1);
		}
		endtime = System.nanoTime();
		out.println(" done in " + ((double)(endtime-tasktime) / 1000000000.0));
		if (cln.hasOption('p'))
		{
			out.print("Assembler output: ");
			for (int i = first; i<bin.size(); i++)
			{
				Integer temp = new Integer((int)bin.get(i) & 0xFF);
				if (temp < 0) temp += 127;
				String tmpstr = Integer.toHexString(temp).toUpperCase();
				if (tmpstr.length() < 2) tmpstr = "0" + tmpstr;
				out.print(tmpstr);
				if (i != (bin.size() - 1)) out.print(" ");
				else out.print("\n");
				temp = null;
				tmpstr = null;
			}
		}
		endtime = System.nanoTime();
		out.println("Task done in " + ((double)(endtime-starttime) / 1000000000.0));
	}

}
