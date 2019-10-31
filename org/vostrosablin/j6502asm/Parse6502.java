package org.vostrosablin.j6502asm;
//J6502ASM - 6502 Parser & Assembler (Parse6502)

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

public class Parse6502 {
	public class ParserError extends Exception {
		private static final long serialVersionUID = -6662674447576552758L;
		ParserError(String message) {
			super(message);
		}
	}
	public class Label { //Label
		public String name;
		public int pc;
		public Label(String name, int pc)
		{
			this.name = name;
			this.pc = pc;
		}
	}
	public class LabelRQ { //Label resolve request
		public String name;
		public int pc, lineno, tc;
		public boolean abs;
		public LabelRQ(String name, int pc, boolean abs, int lineno, int tc)
		{
			this.name = name;
			this.pc = pc;
			this.abs = abs;
			this.lineno = lineno;
			this.tc = tc;
		}
	}
	private static String instr [] = {"ADC","AND","ASL","BCC","BCS","BEQ","BIT","BMI","BNE","BPL","BRK","BVC","BVS","CLC","CLD","CLI","CLV","CMP","CPX","CPY","DEC","DEX","DEY","EOR","INC","INX","INY","JMP","JSR","LDA","LDX","LDY","LSR","NOP","ORA","PHA","PHP","PLA","PLP","ROL","ROR","RTI","RTS","SBC","SEC","SED","SEI","STA","STX","STY","TAX","TAY","TSX","TXA","TXS","TYA"};
	private static String modes [] = {"IMMEDIATE","ZERO-PAGE","ZERO-PAGE X","ZERO-PAGE Y","IMPLIED","ABSOLUTE","ABSOLUTE X","ABSOLUTE Y","INDIRECT X","INDIRECT Y","ACCUMULATOR","RELATIVE","INDIRECT"};
	private ArrayList<ArrayList<Lex6502.LexToken>> tokens; //Token stream
	private ArrayList<Byte> bin = new ArrayList<Byte>(0); //Compiled code array
	private static int[][] opcodes = new int[][]{ //Array of opcodes

		//IMM   ZP  ZPX  ZPY  IMP  ABS ABSX ABSY INDX INDY  ACC  REL  IND

		{0x69,0x65,0x75,  -1,  -1,0x6D,0x7D,0x79,0x61,0x71,  -1,  -1,  -1 }, //ADC
		{0x29,0x25,0x35,  -1,  -1,0x2D,0x3D,0x39,0x21,0x31,  -1,  -1,  -1 }, //AND
		{  -1,0x06,0x16,  -1,  -1,0x0E,0x1E,  -1,  -1,  -1,0x0A,  -1,  -1 }, //ASL
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,0x90,  -1 }, //BCC
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,0xB0,  -1 }, //BCS
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,0xF0,  -1 }, //BEQ
		{  -1,0x24,  -1,  -1,  -1,0x2C,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //BIT
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,0x30,  -1 }, //BMI
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,0xD0,  -1 }, //BNE
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,0x10,  -1 }, //BPL
		{  -1,  -1,  -1,  -1,0x00,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //BRK
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,0x50,  -1 }, //BVC
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,0x70,  -1 }, //BVS
		{  -1,  -1,  -1,  -1,0x18,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //CLC
		{  -1,  -1,  -1,  -1,0xD8,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //CLD
		{  -1,  -1,  -1,  -1,0x58,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //CLI
		{  -1,  -1,  -1,  -1,0xB8,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //CLV
		{0xC9,0xC5,0xD5,  -1,  -1,0xCD,0xDD,0xD9,0xC1,0xD1,  -1,  -1,  -1 }, //CMP
		{0xE0,0xE4,  -1,  -1,  -1,0xEC,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //CPX
		{0xC0,0xC4,  -1,  -1,  -1,0xCC,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //CPY
		{  -1,0xC6,0xD6,  -1,  -1,0xCE,0xDE,  -1,  -1,  -1,  -1,  -1,  -1 }, //DEC
		{  -1,  -1,  -1,  -1,0xCA,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //DEX
		{  -1,  -1,  -1,  -1,0x88,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //DEY
		{0x49,0x45,0x55,  -1,  -1,0x4D,0x5D,0x59,0x41,0x51,  -1,  -1,  -1 }, //EOR
		{  -1,0xE6,0xF6,  -1,  -1,0xEE,0xFE,  -1,  -1,  -1,  -1,  -1,  -1 }, //INC
		{  -1,  -1,  -1,  -1,0xE8,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //INX
		{  -1,  -1,  -1,  -1,0xC8,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //INY
		{  -1,  -1,  -1,  -1,  -1,0x4C,  -1,  -1,  -1,  -1,  -1,  -1,0x6C }, //JMP
		{  -1,  -1,  -1,  -1,  -1,0x20,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //JSR
		{0xA9,0xA5,0xB5,  -1,  -1,0xAD,0xBD,0xB9,0xA1,0xB1,  -1,  -1,  -1 }, //LDA
		{0xA2,0xA6,  -1,0xB6,  -1,0xAE,  -1,0xBE,  -1,  -1,  -1,  -1,  -1 }, //LDX
		{0xA0,0xA4,0xB4,  -1,  -1,0xAC,0xBC,  -1,  -1,  -1,  -1,  -1,  -1 }, //LDY
		{  -1,0x46,0x56,  -1,  -1,0x4E,0x5E,  -1,  -1,  -1,0x4A,  -1,  -1 }, //LSR
		{  -1,  -1,  -1,  -1,0xEA,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //NOP
		{0x09,0x05,0x15,  -1,  -1,0x0D,0x1D,0x19,0x01,0x11,  -1,  -1,  -1 }, //ORA
		{  -1,  -1,  -1,  -1,0x48,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //PHA
		{  -1,  -1,  -1,  -1,0x08,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //PHP
		{  -1,  -1,  -1,  -1,0x68,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //PLA
		{  -1,  -1,  -1,  -1,0x28,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //PLP
		{  -1,0x26,0x36,  -1,  -1,0x2E,0x3E,  -1,  -1,  -1,0x2A,  -1,  -1 }, //ROL
		{  -1,0x66,0x76,  -1,  -1,0x6E,0x7E,  -1,  -1,  -1,0x6A,  -1,  -1 }, //ROR
		{  -1,  -1,  -1,  -1,0x40,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //RTI
		{  -1,  -1,  -1,  -1,0x60,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //RTS
		{0xE9,0xE5,0xF5,  -1,  -1,0xED,0xFD,0xF9,0xE1,0xF1,  -1,  -1,  -1 }, //SBC
		{  -1,  -1,  -1,  -1,0x38,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //SEC
		{  -1,  -1,  -1,  -1,0xF8,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //SED
		{  -1,  -1,  -1,  -1,0x78,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //SEI
		{  -1,0x85,0x95,  -1,  -1,0x8D,0x9D,0x99,0x81,0x91,  -1,  -1,  -1 }, //STA
		{  -1,0x86,  -1,0x96,  -1,0x8E,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //STX
		{  -1,0x84,0x94,  -1,  -1,0x8C,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //STY
		{  -1,  -1,  -1,  -1,0xAA,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //TAX
		{  -1,  -1,  -1,  -1,0xA8,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //TAY
		{  -1,  -1,  -1,  -1,0xBA,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //TSX
		{  -1,  -1,  -1,  -1,0x8A,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //TXA
		{  -1,  -1,  -1,  -1,0x9A,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }, //TXS
		{  -1,  -1,  -1,  -1,0x98,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 }};//TYA

	//IMM   ZP  ZPX  ZPY  IMP  ABS ABSX ABSY INDX INDY  ACC  REL  IND		

	private int pc, tc, lineno = 0; //Program counter, token counter, parsing line
	private ArrayList<Label> labels = new ArrayList<Label>(0); //Encountered labels
	private ArrayList<LabelRQ> labelrqs = new ArrayList<LabelRQ>(0); //Deferred label resolve requests
	public Parse6502 (ArrayList<ArrayList<Lex6502.LexToken>> tokens) //We accept token stream from lexer
	{
		this.tokens = tokens;
	}
	private boolean acceptToken(ArrayList<Lex6502.LexToken> tokens, String type)
	{
		if ((!(tc < tokens.size())) && type.equals("EOL")) return true;
		if (!(tc < tokens.size())) return false;
		if (tokens.get(tc).type.equals(type))
		{	
			tc += 1;
			return true;
		}
		return false;
	}
	private boolean expectToken(ArrayList<Lex6502.LexToken> tokens, String type) throws ParserError
	{
		if (acceptToken(tokens,type)) return true;
		throw new ParserError("Expected " + type + ", but \"" + tokens.get(tc).token + " (" + tokens.get(tc).type + ")\" found at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
	}
	private boolean acceptToken(ArrayList<Lex6502.LexToken> tokens, String type, String token)
	{
		if ((!(tc < tokens.size())) && type.equals("EOL")) return true;
		if (!(tc < tokens.size())) return false;
		if ((tokens.get(tc).type.equals(type)) && (tokens.get(tc).token.equals(token)))
		{	
			tc += 1;
			return true;
		}
		return false;
	}
	private boolean expectToken(ArrayList<Lex6502.LexToken> tokens, String type, String token) throws ParserError
	{
		if (acceptToken(tokens,type,token)) return true;
		throw new ParserError("Expected \"" + token + " (" + type + ")\", but \"" + tokens.get(tc).token + " (" + tokens.get(tc).type + ")\" found at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
	}
	private boolean unexpectedToken(ArrayList<Lex6502.LexToken> tokens) throws ParserError
	{
		throw new ParserError("Unexpected token \"" + tokens.get(tc).type + " (" + tokens.get(tc).token + ")\" found at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
	}
	public static void ensureSize(ArrayList<Byte> arr, int size) {
		arr.ensureCapacity(size);
		while (arr.size() < size) {
			arr.add((byte)0);
		}
	}
	public static int getOperatorIndex(String operator)
	{
		int i = 0;
		while (true)
		{
			if (instr[i].equals(operator)) return i;
			i++;
		}
	}
	public static boolean isRelative(int opindex)
	{
		if (opcodes[opindex][11] != -1) return true;
		else return false;
	}
	public ArrayList<Byte> parseAndAssemble() throws ParserError //Main method here
	{
		pc = 0;
		for (int i = 0; i<tokens.size(); i++)
		{
			lineno = i;
			parseString(tokens.get(i));
		}
		resolveLRQ();
		return bin;
	}
	private void parseString(ArrayList<Lex6502.LexToken> s) throws ParserError //Parse single line from source
	{
		tc = 0;
		while (tc < s.size())
		{
			if ((acceptToken(s,"LABEL")))
			{
				if (tc != 1) throw new ParserError("Label can be defined only in beginning of line");
				expectToken(s,"COLON");
				labelDef(s);
				acceptToken(s,"EOL");
			}
			else if ((acceptToken(s,"DIRECTIVE")))
			{
				tc -= 1;
				directive(s);
				expectToken(s,"EOL");
			}
			else if (acceptToken(s,"OPERATOR"))
			{
				operator(s);
				expectToken(s,"EOL");
			}
			else unexpectedToken(s);
		}
	}
	private void labelDef(ArrayList<Lex6502.LexToken> s) throws ParserError //Parse label definition
	{
		String lnm = s.get(tc-2).token;
		for (int i = 0; i<labels.size(); i++)
		{
			if (labels.get(i).name.equals(lnm)) throw new ParserError("Duplicate label \"" + lnm + "\" encountered at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
		}
		labels.add(new Label(lnm,pc));
	}
	private int resolveLabel(String lnm, boolean abs) throws ParserError //Try to evaluate label
	{
		int offset = 0;
		boolean lbl = false;
		for (int i = 0; i<labels.size(); i++)
		{
			if (labels.get(i).name.equals(lnm))
			{
				lbl = true; 
				if (abs) {offset = labels.get(i).pc; break;}
				else
				{
					if (labels.get(i).pc < (pc+2))
					{
						offset = (pc+2) - labels.get(i).pc;
						if (offset>128) throw new ParserError("Label offset is out of range at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
						//else offset = offset | 128; break;
						else offset = -offset;
					}
					else
					{
						offset = labels.get(i).pc - (pc+2);
						if (offset>127) throw new ParserError("Label offset is out of range at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
						else break;
					}
				}
			}
		}
		if (!lbl) throw new ParserError("No such label");
		return offset;
	}
	private void resolveLRQ() throws ParserError //Evaluate leftover labels
	{
		for (int j = 0; j<labelrqs.size(); j++)
		{
			boolean lbl = false;
			for (int i = 0; i<labels.size(); i++)
			{
				if (labels.get(i).name.equals(labelrqs.get(j).name))
				{
					lbl = true;
					if (labelrqs.get(j).abs)
					{
						Byte operand = new Byte((byte)(labels.get(i).pc & 0xFF));
						Byte operand2 = new Byte((byte)((labels.get(i).pc >> 8) & 0xFF));
						bin.set(labelrqs.get(j).pc,operand);
						bin.set(labelrqs.get(j).pc+1,operand2);
					}
					else
					{
						int offset;
						if (labels.get(i).pc < (labelrqs.get(j).pc+1))
						{
							offset = (labelrqs.get(j).pc+1) - labels.get(i).pc;
							if (offset>128) throw new ParserError("Label offset is out of range at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
							else offset = offset | 128;
						}
						else
						{
							offset = labels.get(i).pc - (labelrqs.get(j).pc+1);
							if (offset>127) throw new ParserError("Label offset is out of range at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
						}
						Byte operand = new Byte((byte)offset);
						bin.set(labelrqs.get(j).pc,operand);
					}
					break;
				}
			}
			if (!lbl) throw new ParserError("Couldn't resolve label \"" + labelrqs.get(j).name + "\" at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
		}
	}
	private void operator(ArrayList<Lex6502.LexToken> s) throws ParserError //Parse operator construction
	{
		int opindex = getOperatorIndex(s.get(tc-1).token); //Get index of operator
		int mode = -1; //Mode index
		if (acceptToken(s,"IMMEDIATE")) //Mode 0
		{
			expectToken(s,"NUM");
			mode = 0;
			if (opcodes[opindex][mode] != -1)
			{
				if (s.get(tc-1).value < 256)
				{
					Byte opcode = new Byte((byte)opcodes[opindex][mode]);
					Byte operand = new Byte((byte)s.get(tc-1).value);
					bin.ensureCapacity(pc+2);
					bin.add(pc,opcode);
					bin.add(pc+1,operand);
					pc+=2;
				}
				else throw new ParserError("Expected byte, but found word at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
			}
			else throw new ParserError("Opcode " + instr[opindex] + " is not defined for mode " + modes[mode] + " at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
		}
		else if (acceptToken(s,"LABEL","A")) //Mode 10
		{
			mode = 10;
			if (opcodes[opindex][mode] != -1)
			{
				Byte opcode = new Byte((byte)opcodes[opindex][mode]);
				bin.ensureCapacity(pc+1);
				bin.add(pc,opcode);
				pc++;
			}
			else throw new ParserError("Opcode " + instr[opindex] + " is not defined for mode " + modes[mode] + " at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
		}
		else if (acceptToken(s,"LABEL")) //Modes 5, 11 (Labeled)
		{
			int offset = 0;
			if (opindex == 27) //Mode 5 for JMP, else 11
			{
				mode = 5;
				try
				{
					offset = resolveLabel(s.get(tc-1).token,true);
				}
				catch (ParserError e)
				{
					if (e.getMessage().equals("No such label"))
					{
						labelrqs.add(new LabelRQ(s.get(tc-1).token,pc+1,true,lineno,tc));
						offset = 0;
					}
					else throw e;
				}
				if (opcodes[opindex][mode] != -1)
				{
					{
						Byte opcode = new Byte((byte)opcodes[opindex][mode]);
						Byte operand = new Byte((byte)(offset & 0xFF));
						Byte operand2 = new Byte((byte)((offset >> 8) & 0xFF));
						bin.ensureCapacity(pc+3);
						bin.add(pc,opcode);
						bin.add(pc+1,operand);
						bin.add(pc+2,operand2);
						pc+=3;
					}
				}
			}
			else //Mode 11
			{
				mode = 11;
				try
				{
					offset = resolveLabel(s.get(tc-1).token,false);
				}
				catch (ParserError e)
				{
					if (e.getMessage().equals("No such label"))
					{
						labelrqs.add(new LabelRQ(s.get(tc-1).token,pc+1,false,lineno,tc));
						offset = 0;
					}
					else throw e;
				}
				if (opcodes[opindex][mode] != -1)
				{
					{
						Byte opcode = new Byte((byte)opcodes[opindex][mode]);
						Byte operand = new Byte((byte)offset);
						bin.ensureCapacity(pc+2);
						bin.add(pc,opcode);
						bin.add(pc+1,operand);
						pc+=2;
					}
				}
			}
		}
		else if (acceptToken(s,"NUM")) //Modes 1, 2, 3, 5, 6, 7, 11
		{
			if ((s.get(tc-1).value < 256)) //Modes 1, 2, 3, 11
			{
				if (acceptToken(s,"EOL")) //Modes 1, 11
				{
					if (isRelative(opindex)) mode = 11;
					else mode = 1;
					if (opcodes[opindex][mode] != -1)
					{
						{
							Byte opcode = new Byte((byte)opcodes[opindex][mode]);
							Byte operand = new Byte((byte)s.get(tc-1).value);
							bin.ensureCapacity(pc+2);
							bin.add(pc,opcode);
							bin.add(pc+1,operand);
							pc+=2;
						}
					}
					else throw new ParserError("Opcode " + instr[opindex] + " is not defined for mode " + modes[mode] + " at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
				}
				else if (acceptToken(s,"COMMA")) //Modes 2, 3
				{
					if (acceptToken(s,"LABEL","X")) mode = 2;
					else if (acceptToken(s,"LABEL","Y")) mode = 3;
					else unexpectedToken(s);
					if (opcodes[opindex][mode] != -1)
					{
						{
							Byte opcode = new Byte((byte)opcodes[opindex][mode]);
							Byte operand = new Byte((byte)s.get(tc-3).value);
							bin.ensureCapacity(pc+2);
							bin.add(pc,opcode);
							bin.add(pc+1,operand);
							pc+=2;
						}
					}
					else throw new ParserError("Opcode " + instr[opindex] + " is not defined for mode " + modes[mode] + " at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
				}
				else unexpectedToken(s);
			}
			else //Modes 5, 6, 7
			{
				if (acceptToken(s,"EOL")) //Mode 5
				{
					mode = 5;
					if (opcodes[opindex][mode] != -1)
					{
						{
							Byte opcode = new Byte((byte)opcodes[opindex][mode]);
							Byte operand = new Byte((byte)(s.get(tc-1).value & 0xFF));
							Byte operand2 = new Byte((byte)((s.get(tc-1).value >> 8) & 0xFF));
							bin.ensureCapacity(pc+3);
							bin.add(pc,opcode);
							bin.add(pc+1,operand);
							bin.add(pc+2,operand2);
							pc+=3;
						}
					}
					else throw new ParserError("Opcode " + instr[opindex] + " is not defined for mode " + modes[mode] + " at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
				}
				else if (acceptToken(s,"COMMA")) //Modes 6, 7
				{
					if (acceptToken(s,"LABEL","X")) mode = 6;
					else if (acceptToken(s,"LABEL","Y")) mode = 7;
					else unexpectedToken(s);
					if (opcodes[opindex][mode] != -1)
					{
						{
							Byte opcode = new Byte((byte)opcodes[opindex][mode]);
							Byte operand = new Byte((byte)(s.get(tc-3).value & 0xFF));
							Byte operand2 = new Byte((byte)((s.get(tc-3).value >> 8) & 0xFF));
							bin.ensureCapacity(pc+3);
							bin.add(pc,opcode);
							bin.add(pc+1,operand);
							bin.add(pc+2,operand2);
							pc+=3;
						}
					}
					else throw new ParserError("Opcode " + instr[opindex] + " is not defined for mode " + modes[mode] + " at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
				}
			}
		}
		else if (acceptToken(s,"LPAREN")) //Modes 8, 9, 12
		{
			if (acceptToken(s,"NUM"))
			{
				if ((s.get(tc-1).value < 256)) //Modes 8, 9
				{
					if (acceptToken(s,"COMMA")) //Mode 8
					{
						mode = 8;
						expectToken(s,"LABEL","X");
						expectToken(s,"RPAREN");
						if (opcodes[opindex][mode] != -1)
						{
							{
								Byte opcode = new Byte((byte)opcodes[opindex][mode]);
								Byte operand = new Byte((byte)(s.get(tc-4).value & 0xFF));
								bin.ensureCapacity(pc+1);
								bin.add(pc,opcode);
								bin.add(pc+1,operand);
								pc+=2;
							}
						}
					}
					else if (acceptToken(s,"RPAREN")) //Mode 9
					{
						mode = 9;
						expectToken(s,"COMMA");
						expectToken(s,"LABEL","Y");
						if (opcodes[opindex][mode] != -1)
						{
							{
								Byte opcode = new Byte((byte)opcodes[opindex][mode]);
								Byte operand = new Byte((byte)(s.get(tc-4).value & 0xFF));
								bin.ensureCapacity(pc+1);
								bin.add(pc,opcode);
								bin.add(pc+1,operand);
								pc+=2;
							}
						}
					}
					else unexpectedToken(s);
				}
				else //Mode 12
				{
					mode = 12;
					expectToken(s,"RPAREN");
					if (opcodes[opindex][mode] != -1)
					{
						{
							Byte opcode = new Byte((byte)opcodes[opindex][mode]);
							Byte operand = new Byte((byte)(s.get(tc-2).value & 0xFF));
							Byte operand2 = new Byte((byte)((s.get(tc-2).value >> 8) & 0xFF));
							bin.ensureCapacity(pc+3);
							bin.add(pc,opcode);
							bin.add(pc+1,operand);
							bin.add(pc+2,operand2);
							pc+=3;
						}
					}
				}
			}
			else if (acceptToken(s,"LABEL")) //Mode 12 (Labeled)
			{
				int offset = 0;
				mode = 12;
				expectToken(s,"RPAREN");
				try
				{
					offset = resolveLabel(s.get(tc-2).token,true);
				}
				catch (ParserError e)
				{
					if (e.getMessage().equals("No such label"))
					{
						labelrqs.add(new LabelRQ(s.get(tc-1).token,pc+1,true,lineno,tc));
						offset = 0;
					}
					else throw e;
				}
				if (opcodes[opindex][mode] != -1)
				{
					{
						Byte opcode = new Byte((byte)opcodes[opindex][mode]);
						Byte operand = new Byte((byte)(offset & 0xFF));
						Byte operand2 = new Byte((byte)((offset >> 8) & 0xFF));
						bin.ensureCapacity(pc+3);
						bin.add(pc,opcode);
						bin.add(pc+1,operand);
						bin.add(pc+2,operand2);
						pc+=3;
					}
				}
			}
			else unexpectedToken(s);
		}
		else if (acceptToken(s,"EOL")) //Mode 4
		{
			mode = 4;
			if (opcodes[opindex][mode] != -1)
			{
				Byte opcode = new Byte((byte)opcodes[opindex][mode]);
				bin.ensureCapacity(pc+1);
				bin.add(pc,opcode);
				pc++;
			}
			else throw new ParserError("Opcode " + instr[opindex] + " is not defined for mode " + modes[mode] + " at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
		}
		else unexpectedToken(s);
	}
	private void directive(ArrayList<Lex6502.LexToken> s) throws ParserError //Parse directive
	{
		if (acceptToken(s,"DIRECTIVE",".DB"))
		{
			do
			{
				expectToken(s,"NUM");
				if (s.get(tc-1).value < 256)
				{
					Byte db = new Byte((byte)s.get(tc-1).value);
					bin.ensureCapacity(pc+1);
					bin.add(pc,db);
					pc++;
				}
				else
				{
					Byte db = new Byte((byte)(s.get(tc-1).value & 0xFF));
					bin.ensureCapacity(pc+1);
					bin.add(pc,db);
					pc++;
					db = new Byte((byte)((s.get(tc-1).value >> 8) & 0xFF));
					bin.ensureCapacity(pc+1);
					bin.add(pc,db);
					pc++;
				}
			} while (acceptToken(s,"COMMA"));
		}
		else if (acceptToken(s,"DIRECTIVE",".WORDS")||acceptToken(s,"DIRECTIVE",".WORD"))
		{
			do
			{
				expectToken(s,"NUM");
				Byte db = new Byte((byte)(s.get(tc-1).value & 0xFF));
				bin.add(pc,db);
				pc++;
				db = new Byte((byte)((s.get(tc-1).value >> 8) & 0xFF));
				bin.ensureCapacity(pc+1);
				bin.add(pc,db);
				pc++;
			} while (acceptToken(s,"COMMA"));
		}
		else if (acceptToken(s,"DIRECTIVE",".BYTES")||acceptToken(s,"DIRECTIVE",".BYTE"))
		{
			do
			{
				expectToken(s,"NUM");
				if (s.get(tc-1).value > 255) throw new ParserError("Expected byte, but found word at line " + (lineno+1) + ", token " + (tc+1) + ". Aborted.");
				Byte db = new Byte((byte)s.get(tc-1).value);
				bin.ensureCapacity(pc+1);
				bin.add(pc,db);
				pc++;
			} while (acceptToken(s,"COMMA"));
		}
		else if (acceptToken(s,"DIRECTIVE",".ORG"))
		{
			expectToken(s,"NUM");
			pc = s.get(tc-1).value;
			ensureSize(bin,pc);
		}
		else unexpectedToken(s);
	}
}
