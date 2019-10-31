package org.vostrosablin.j6502asm;
//J6502ASM - 6502 Assembler Lexical Analyser (Lex6502)

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

import java.util.ArrayList;

public class Lex6502 {
	private static String instr [] = {"ADC","AND","ASL","BCC","BCS","BEQ","BIT","BMI","BNE","BPL","BRK","BVC","BVS","CLC","CLD","CLI","CLV","CMP","CPX","CPY","DEC","DEX","DEY","EOR","INC","INX","INY","JMP","JSR","LDA","LDX","LDY","LSR","NOP","ORA","PHA","PHP","PLA","PLP","ROL","ROR","RTI","RTS","SBC","SEC","SED","SEI","STA","STX","STY","TAX","TAY","TSX","TXA","TXS","TYA"};
	public class LexToken { //Tokenization unit.
		public String token, type;
		public int value, lineno;
		public LexToken(String token, String type, int value)
		{
			this.token = token;
			this.type = type;
			this.value = value;
		}
	}
	public static class LexerError extends Exception {
		private static final long serialVersionUID = -5829011050395675884L;
		LexerError(String message) {
			super(message);
		}
	}
	private ArrayList<String> list = new ArrayList<String>(0);
	public Lex6502 (ArrayList<String> input)
	{
		list = input;
	}
	private static int isInstr(String s)
	{
		int ins = -1;
		for (int i = 0; i<instr.length; i++) {
			if (s.equalsIgnoreCase(instr[i])) {
				ins = i;
				break; // No need to look further.
			} 
		}
		return ins;	
	}
	public ArrayList<ArrayList<Lex6502.LexToken>> tokenizeInput() throws LexerError
	{
		ArrayList<ArrayList<Lex6502.LexToken>> tokens = new ArrayList<ArrayList<Lex6502.LexToken>>(0);
		for (int i = 0;i<list.size();i++)
		{
			tokens.add(tokenizeString(list.get(i),i));
		}
		return tokens;
	}
	private ArrayList<Lex6502.LexToken> tokenizeString(String input, int lineno) throws LexerError //Finite State Automaton Lexer
	{
		ArrayList<Lex6502.LexToken> strtokens = new ArrayList<Lex6502.LexToken>(0);
		Lex6502.LexToken tknbfr = new Lex6502.LexToken("","",0);

		//DFA State Variables
		boolean scan = false; //Scanning multichar token now
		int numscan = 0; //Scanning number token
		int idscan = 0; //Scanning identifier token

		String tokenbfr = ""; //Token buffer
		for (int i = 0;i<input.length();i++)
		{
			if (!scan)
			{
				if (input.charAt(i) == '#') //Single-char literal tokens
				{
					tknbfr = new Lex6502.LexToken("#","IMMEDIATE",0);
					strtokens.add(tknbfr);
				}
				else if (input.charAt(i) == ',')
				{
					tknbfr = new Lex6502.LexToken(",","COMMA",0);
					strtokens.add(tknbfr);
				}
				else if (input.charAt(i) == ':')
				{
					tknbfr = new Lex6502.LexToken(":","COLON",0);
					strtokens.add(tknbfr);
				}
				else if (input.charAt(i) == '(')
				{
					tknbfr = new Lex6502.LexToken("(","LPAREN",0);
					strtokens.add(tknbfr);
				}
				else if (input.charAt(i) == ')')
				{
					tknbfr = new Lex6502.LexToken(")","RPAREN",0);
					strtokens.add(tknbfr);
				}
				else if ("0123456789%$".contains("" + input.charAt(i))) //Start of a generic number literal
				{
					tknbfr = new Lex6502.LexToken("","GENERICNUM",0);
					scan = true;
					numscan = 1; //Go to number scanning state (after 1st char)
					tokenbfr += input.charAt(i);
				}
				else if (".0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".contains("" + input.charAt(i))) //Start of a generic identifier
				{
					tknbfr = new Lex6502.LexToken("","IDENTIFIER",0);
					scan = true;
					idscan = 1; //Go to identifier scanning state (after 1st char)
					tokenbfr += input.charAt(i);
				}
				else if (" \t\n".contains("" + input.charAt(i))) {} //Whitespace and newline are legal characters, but we ignore 'em
				else //Otherwise, we met a character, that is not supported by J6502ASM, so we can't continue.
				{
					throw new LexerError("Unexpected character \"" + input.charAt(i) + "\" encountered at line " + (lineno+1) + ", scan position " + i + ". Aborted.");
				}
			}
			else if (scan)
			{
				if (numscan == 1)
				{
					if ("0123456789ABCDEF".contains("" + input.charAt(i)))
					{
						tokenbfr += input.charAt(i);
					}
					else
					{
						i -= 1;
						tknbfr.token = tokenbfr;
						try
						{
							if (tokenbfr.length() > 1)
							{
								if (tokenbfr.charAt(0) == '%')  
								{
									tknbfr.value = Integer.parseInt(tokenbfr.substring(1), 2);
								}
								else if (tokenbfr.charAt(0) == '$')  
								{
									tknbfr.value = Integer.parseInt(tokenbfr.substring(1), 16);
								}
								else if (tokenbfr.charAt(0) == '0')  
								{
									tknbfr.value = Integer.parseInt(tokenbfr.substring(1), 8);
								}
								else
								{
									tknbfr.value = Integer.parseInt(tokenbfr);
								}
							}
							else if (tokenbfr.length() == 1)
							{
								tknbfr.value = Integer.parseInt(tokenbfr);
							}
							if ((tknbfr.value < 0) && (tknbfr.value > 65535))
							{
								throw new NumberFormatException("Numeric value must be either 1 or 2 bytes long. Aborted.");
							}
						}
						catch (NumberFormatException e)
						{
							throw new LexerError("Invalid number literal at line " + (lineno+1) + ", scan position " + i + ". Aborted.");
						}
						tknbfr.type = "NUM";
						strtokens.add(tknbfr);
						tokenbfr = "";
						numscan = 0;
						scan = false;
					}
				}
				else if (idscan == 1)
				{
					if ("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_".contains("" + input.charAt(i)))
					{
						tokenbfr += input.charAt(i);
					}
					else
					{
						i -= 1;
						tknbfr.token = tokenbfr;
						int ins = -1;
						if (tokenbfr.charAt(0) == '.')
						{
							tknbfr.type = "DIRECTIVE";
						}
						else if ((ins = isInstr(tokenbfr)) != -1)
						{
							tknbfr.type = "OPERATOR";
							tknbfr.value = ins;
						}
						else
						{
							tknbfr.type = "LABEL";
						}
						strtokens.add(tknbfr);
						tokenbfr = "";
						idscan = 0;
						scan = false;
					}
				}
			}
		}
		return strtokens;
	}
}
