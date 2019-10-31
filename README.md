J6502ASM - Portable Cross-Assembler for MOS Technology 6502 CPU.
By Vitaly Ostrosablin 2013-2014 (tmp6154@gmail.com)
Version 0.11b

=====
Usage
=====

You need Java Runtime Environment 1.6 to use J6502ASM. To run it from shell, execute following command:

$ java -jar J6502ASM.jar -if <input.asm>

This will translate input file into out.bin.

Command-line parameters:

-if <input file>
Opens specified file for assembling. This is required parameter.

-of <output file>
Outputs to specified binary file (if omitted, it will output into out.bin).

--printcode
Outputs assembled code to screen.

--cutnull
Strip leading null bytes from output (for use with .ORG directive).

===========
Feature set
===========

J6502 supports all documented 6502 opcodes. By default, origin address is zero. Following features are supported:

------
Labels
------

You can define label in beginning of line of code. Labels use following syntax:

<labelname>:<code>

Label names can be used only with jump or branch instructions.

--------
Comments
--------

You can write comments by using ";" character. Everything following it will be stripped by preprocessor.

----------------------
Different number bases
----------------------

J6502ASM supports binary, octal, decimal and hexadecimal numbers. Each base has separate syntax:

%???????? - Binary number
0??? - Octal number
??? - Decimal number
$?? - Hexadecimal number

----------
Directives
----------

.INCLUDE
Preprocessor directive which includes contents of another file.

Example:
.INCLUDE "include.asm"

.BYTE
.BYTES
Directive to declare one (or more comma-separated) bytes.

Example:
.BYTE $FF,255

.WORD
.WORDS
Directive to declare one (or more comma-separated) 16-bit words.

Example:
.WORDS $BEEF

.DB
Directive to declare one or more hardcoded values. This directive writes everything in range 0-255 as bytes, and everything above as 16-bit words.

Example:
.DB 65535, $FF, $BEEF, %11111111

.ORG
Change origin value.

Example:
.ORG $1000
