package pins.phase.lexan;

import java.io.*;
import java.util.Objects;

import pins.common.report.*;
import pins.data.symbol.*;

/**
 * Lexical analyzer.
 */
public class LexAn implements AutoCloseable {

	private final String srcFileName;

	private final FileReader srcFile;

	public LexAn(String srcFileName) {
		this.srcFileName = srcFileName;
		try {
			srcFile = new FileReader(new File(srcFileName));
		} catch (FileNotFoundException __) {
			throw new Report.Error("Cannot open source file '" + srcFileName + "'.");
		}
	}

	public void close() {
		try {
			srcFile.close();
		} catch (IOException __) {
			throw new Report.Error("Cannot close source file '" + srcFileName + "'.");
		}
	}

	/**
	 * Location class members - used to assemble location of current symbol.
 	 */
	private int[] columns = {1, 1};
	private int line = 1;

	/**
	 * File processing members
	 *
	 * lexeme holds currently read lexeme.
	 * buffer holds characters that have been removed in order to assemble a valid symbol.
	 * character holds currently read character.
	 */
	private String lexeme = "";
	private String buffer = "";
	private char character = ' ';

	/**
	 * isChar is a flag that signifies that future characters read are part of character constant.
	 * commentCount is a counter that counts comment tags (#{ = +1, }# = -1).
	 */
	private boolean isChar = false;
	private int commentCount = 0;

	/**
	 * Reads a file character by character and returns a lexical Symbol when one can be constructed.
	 * Each iteration continues reading from where it left off. It also ignores comments.
	 *
	 * @return Lexical Symbol, with lexeme and location in file.
	 * @throws Report.Error when encountering IO operation exception.
	 */
	public Symbol lexer() {
		try {
			// Prepare buffer and lexeme.
			initialize();

			while (srcFile.ready()) {
				character = (char)srcFile.read();
				if (!Character.isWhitespace(character)) {
					if (commentCount != 0) {
						columns[0]++;
					}
					columns[1]++;
				}

				// Check if next characters will be in comment - if so ignore, only check for
				// comment start/ends (#{, }#).
				resolveComments();
				// Check if next characters will be part of character constant.
				resolveCharacter();

				// Resolve whitespace if needed, check for errors and add character to lexeme.
				if (Character.isWhitespace(character)) {
					if (resolveWhitespaceCharacter()) {
						continue;
					}
				}
				else {
					// If not whitespace, proceed normally.
					lexeme += character;
				}

				if (lexeme.length() == 0 || commentCount != 0) {
					continue;
				}

				// If current lexeme invalid and previous lexeme is valid, store last character read to buffer,
				// assemble symbol, reset location, resolve whitespace and return.
				if (getToken(lexeme) == null && getToken(lexeme.substring(0, lexeme.length() - 1)) != null) {
					return commitSymbol(false);
				}
				else {
					if (Character.isWhitespace(character)) {
						applyWhitespace(character);
					}
				}
			}

			// Return last symbol in file, if exists. Otherwise return EOF symbol.
			return terminate();

		} catch (IOException __) {
			throw new Report.Error("Error in lexical analysis: IO Error (Method 'lexer').");
		}
	}

	/**
	 * Matches string with symbol, keyword or constant.
	 *
	 * @param token String to be matched with regex.
	 * @return Item from pins.data.symbol.Token enum based on input token.
	 */
	private Token getToken(String token) {
		switch (token) {
			case "(":
				return Token.LPARENTHESIS;
			case ")":
				return Token.RPARENTHESIS;
			case "[":
				return Token.LBRACKET;
			case "]":
				return Token.RBRACKET;
			case "{":
				return Token.LBRACE;
			case "}":
				return Token.RBRACE;
			case ",":
				return Token.COMMA;
			case ":":
				return Token.COLON;
			case ";":
				return Token.SEMIC;
			case "&":
				return Token.AND;
			case "|":
				return Token.OR;
			case "!":
				return Token.NOT;
			case "==":
				return Token.EQU;
			case "!=":
				return Token.NEQ;
			case "<":
				return Token.LTH;
			case ">":
				return Token.GTH;
			case "<=":
				return Token.LEQ;
			case ">=":
				return Token.GEQ;
			case "*":
				return Token.MUL;
			case "/":
				return Token.DIV;
			case "%":
				return Token.MOD;
			case "+":
				return Token.ADD;
			case "-":
				return Token.SUB;
			case "^":
				return Token.PTR;
			case "=":
				return Token.ASSIGN;
			case "char":
				return Token.CHAR;
			case "del":
				return Token.DEL;
			case "do":
				return Token.DO;
			case "else":
				return Token.ELSE;
			case "end":
				return Token.END;
			case "fun":
				return Token.FUN;
			case "if":
				return Token.IF;
			case "int":
				return Token.INT;
			case "new":
				return Token.NEW;
			case "then":
				return Token.THEN;
			case "typ":
				return Token.TYP;
			case "var":
				return Token.VAR;
			case "void":
				return Token.VOID;
			case "where":
				return Token.WHERE;
			case "while":
				return Token.WHILE;
			case "none":
				return Token.CONSTVOID;
			case "nil":
				return Token.CONSTPTR;
			// Special cases to match CHARCONST, ' and \ must be escaped by \.
			case "'\\\\'":
			case "'\\''":
				return Token.CONSTCHAR;
			default:
				if (matchId(token)) {
					return Token.IDENTIFIER;
				}
				else if (matchConstInt(token)) {
					return Token.CONSTINT;
				}
				else if (matchConstChar(token)) {
					return Token.CONSTCHAR;
				}
		}
		return null;
	}

	/**
	 * Matches string with IDENTIFIER regex.
	 *
	 * @param token String to be matched with regex.
	 * @return True if match was found, false if not.
	 */
	private boolean matchId(String token) {
		return token.matches("[A-Za-z_][A-Za-z0-9_]*");
	}

	/**
	 * Matches string with CONSTINT regex.
	 *
	 * @param token String to be matched with regex.
	 * @return True if match was found, false if not.
	 */
	private boolean matchConstInt(String token) {
		return token.matches("[0-9]+");
	}

	/**
	 * Matches string with CONSTCHAR regex, including ASCII from code 32 to 126, excluding ' and \.
	 *
	 * @param token String to be matched with regex.
	 * @return True if match was found, false if not.
	 */
	private boolean matchConstChar(String token) {
		return token.matches("['][\\x20-\\x26\\x28-\\x5B\\x5D-\\x7E][']");
	}

	/**
	 * Initializes the lexeme and buffer class members to be used
	 * in assembly of new symbol.
	 */
	private void initialize() {
		// Resolve all whitespace left over from previous symbol.
		for (int i = 0; i < buffer.length(); i++) {
			if (Character.isWhitespace(buffer.charAt(i))) {
				applyWhitespace(buffer.charAt(i));
			}
		}

		// Trim contents of buffer and set as current lexeme to continue.
		if (buffer.trim().length() > 0) {
			lexeme = buffer.trim();
		}
		buffer = "";

		// Adjust starting point by # of characters remaining in lexeme from previous symbol.
		columns[0] -= lexeme.length();
	}

	/**
	 * Checks if following characters read are part of character constant. It first checks if ' was found,
	 * and if it was escaped. It switches the isChar flag when an ordinary ' was found.
	 */
	private void resolveCharacter() {
		if (character == '\'' && commentCount == 0) {
			if (lexeme.length() != 0) {
				if (!lexeme.substring(lexeme.length() - 1).contains("\\")
						|| lexeme.substring(lexeme.length() - 2).contains("\\\\")) {
					isChar = !isChar;
				}
			}
			else {
				isChar = !isChar;
			}
		}
	}

	/**
	 * Checks if comment start (#{) or comment end (}#) tag was encountered, if so adjusts comment counter by +-1.
	 */
	private void resolveComments() {
		if (!isChar && lexeme.endsWith("#{")) {
			lexeme = "";
			commentCount++;
		}

		if (!isChar && lexeme.endsWith("}#")) {
			lexeme = "";
			commentCount--;
			columns[0] = columns[1];
		}
	}

	/**
	 * Checks what whitespace character was encountered and increases line and column counters accordingly.
	 *
	 * @param chr The whitespace character to resolve.
	 */
	private void applyWhitespace(char chr) {
		switch (chr) {
			case '\n' -> {
				line++;
				columns[0] = 1;
				columns[1] = 1;
			}
			case '\t' -> {
				// Move cursor to multiple of 8.
				columns[0] += (8 - columns[0] % 8) + 1;
				columns[1] += (8 - columns[1] % 8) + 1;
			}
			case '\r' -> {
				columns[0] = 1;
				columns[1] = 1;
			}
			case ' ' -> {
				columns[0]++;
				columns[1]++;
			}
		}
	}

	/**
	 * Resolves whitespace if needed, checks for errors in current lexeme, adds character to lexeme if needed.
	 *
	 * @return True if whitespace was resolved, otherwise false
	 */
	private boolean resolveWhitespaceCharacter() {
		if (commentCount != 0) {
			lexeme += character;
		}

		if (lexeme.length() == 0 || commentCount != 0) {
			applyWhitespace(character);
			return true;
		}

		// If not in character constant, check if lexeme invalid and throw error.
		// This is needed so, for example "#char ax" throws error at "#char".
		if (getToken(lexeme) == null && commentCount == 0 && !isChar) {
			throw new Report.Error(new Location(line, columns[0], line, columns[1]),
					"Unrecognized token: " + lexeme);
		}

		// If character constant or non-empty lexeme, add whitespace
		// This is needed during symbol checking, as a symbol is committed
		// when current symbol isn't valid, but previous symbol was.
		if (!lexeme.equals("") || isChar || commentCount != 0) {
			lexeme += character;
		}
		return false;
	}

	/**
	 * Assembles Symbol from current lexeme.
	 * 
	 * @return Symbol created from lexeme, token and location.
	 */
	private Symbol assembleSymbol(boolean isLast) {
		Token tk;
		String lx;
		Location lt;
		if (!isLast) {
			tk = getToken(lexeme.substring(0, lexeme.length() - 1));
			lx = lexeme.substring(0, lexeme.length() - 1);
			lt = new Location(line, columns[0], line, columns[0] + lexeme.length() - 2);
		}
		else {
			tk = getToken(lexeme);
			lx = lexeme;
			lt = new Location(line, columns[0], line, columns[0] + lexeme.length() - 1);
		}


		return new Symbol(tk, lx, lt);
	}

	/**
	 * Assembles symbol, sets buffer, lexeme and sets starting column to ending column.
	 * 
	 * @return Symbol created from lexeme, token and location.
	 */
	private Symbol commitSymbol(boolean isLast) {
		Symbol sym = assembleSymbol(isLast);

		if (!isLast) {
			// Store invalid characters to buffer, used in next iteration.
			buffer += lexeme.substring(lexeme.length() - 1);
			lexeme = "";
		}
		else {
			lexeme = buffer = "";
		}

		columns[0] = columns[1];

		return sym;
	}

	/**
	 * Ends method lexer, by returning last symbol in file, reports a warning if comments aren't closed and throws an
	 * error if invalid lexeme at end of file.
	 *
	 * @return Symbol (either last symbol in file or EOF symbol)
	 */
	private Symbol terminate() {
		// Needed to check if comment is closed at end of file.
		resolveComments();

		// Check if any character left in lexeme when file read - this is needed if EOF comes directly after
		// end of lexeme, which would cause the lexeme to not be committed.
		lexeme = lexeme.trim() + buffer.trim();
		if (!lexeme.equals("") && commentCount == 0 && getToken(lexeme) != null) {
			return commitSymbol(true);
		}
		// Check if not in comment and invalid lexeme found - throw error.
		else if(!lexeme.equals("") && commentCount == 0 && getToken(lexeme) == null) {
			throw new Report.Error(new Location(line, columns[0], line, columns[1]),
					"Unrecognized token: " + lexeme);
		}

		if (commentCount != 0) {
			Report.warning("Open comment at end of file.");
		}

		// If entire file read, return EOF symbol. The EOF symbol does not have a location.
		return new Symbol(Token.EOF, "EOF", new Location(null));
	}

}
