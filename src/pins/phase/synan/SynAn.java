package pins.phase.synan;

// Imports used for logging.
import pins.common.report.Location;
import pins.common.report.Report;

// Imports used for accepting data from LexAn.
import pins.data.symbol.Symbol;
import pins.data.symbol.Token;
import pins.phase.lexan.*;

// Abstract syntax tree imports.
import pins.data.ast.*;

import java.util.Vector;

/**
 * Syntax analyzer, using LL(1) parsing with recursive descent.
 */
public class SynAn implements AutoCloseable {

	/** The lexical analyser. */
	private final LexAn lexan;
	/** The current symbol (LL buffer of size 1). */
	private Symbol currSymbol;
	

	public SynAn(LexAn lexan) {		
		this.lexan = lexan;
		this.currSymbol = null;
	}

	public void close() {
		lexan.close();
	}

	/**
	 * Initializes parsing of source file, by grabbing first symbol and calling recursive parsing.
	 *
	 * @return AST of source file.
	 */
	public AST parser() {
		currSymbol = lexan.lexer();
		return parsePrg();
	}


	/**
	 * Checks if current symbol matches the expected symbol of production. If so, {@link LexAn#lexer()
	 * move to next symbol}, otherwise throw error.
	 *
	 * @param t Token to which the current symbol will be compared to.
	 * @return Last symbol that was read.
	 * @throws Report.Error when current symbol doesn't match expected token.
	 */
	private Symbol checkAndMove(Token t) {
		// Check if current symbol matches expected token.
		if (currSymbol.token != t) {
			throw new Report.Error(currSymbol.location,
					"Syntax error! Expected: " + getLexeme(t) + ", got: " + currSymbol.lexeme);
		}
		// Save current symbol.
		Symbol tmp = currSymbol;
		// Move to next symbol in source file.
		currSymbol = lexan.lexer();

		return tmp;
	}

	/**
	 * Returns lexeme representation of token.
	 *
	 * @param token Token of which to get lexeme.
	 * @return Lexeme of token.
	 */
	private String getLexeme(Token token) {
		return switch (token) {
			case LPARENTHESIS -> "(";
			case RPARENTHESIS -> ")";
			case LBRACKET -> "[";
			case RBRACKET -> "]";
			case LBRACE -> "{";
			case RBRACE -> "}";
			case COMMA -> ",";
			case COLON -> ":";
			case SEMIC -> ";";
			case AND -> "&";
			case OR -> "|";
			case NOT -> "!";
			case EQU -> "==";
			case NEQ -> "!=";
			case LTH -> "<";
			case GTH -> ">";
			case LEQ -> "<=";
			case GEQ -> ">=";
			case MUL -> "*";
			case DIV -> "/";
			case MOD -> "%";
			case ADD -> "+";
			case SUB -> "-";
			case PTR -> "^";
			case ASSIGN -> "=";
			case CHAR -> "char";
			case DEL -> "del";
			case DO -> "do";
			case ELSE -> "else";
			case END -> "end";
			case FUN -> "fun";
			case IF -> "if";
			case INT -> "int";
			case NEW -> "new";
			case THEN -> "then";
			case TYP -> "typ";
			case VAR -> "var";
			case VOID -> "void";
			case WHERE -> "where";
			case WHILE -> "while";
			case CONSTVOID -> "none";
			case CONSTPTR -> "nil";
			case CONSTCHAR -> "character constant";
			case IDENTIFIER -> "identifier";
			case CONSTINT -> "int constant";
			default -> null;
		};
	}


	/**
	 * Parse start symbol of program.
	 *
	 * @return AST of program.
	 */
	private AST parsePrg() {
		Vector<AstDecl> decls = parseDecls();
		return new ASTs<>(decls.get(0).location, decls);
	}

	/**
	 * Parse declarations of program.
	 *
	 * @return Vector of ASTs of declarations.
	 */
	private Vector<AstDecl> parseDecls() {
		Vector<AstDecl> decls = new Vector<>();

		decls.add(parseDecl());
		decls.addAll(parseDeclsRst());

		return decls;
	}

	/**
	 * Parse rest of declarations in program.
	 *
	 * @return Vector of ASTs of declarations.
	 * @throws Report.Error when current symbol not one of: TYP, VAR, FUN, EOF, RPARENTHESIS.
	 */
	private Vector<AstDecl> parseDeclsRst() {
		switch (currSymbol.token) {
			case TYP, VAR, FUN -> {
				// If token a start of declaration, parse declaration.

				return parseDecls();
			}
			case EOF, RPARENTHESIS -> {
				// If token end-of-file or right parenthesis, there are no more declarations.
				return new Vector<>();
			}
			default -> {
				throw new Report.Error(currSymbol.location, "Syntax error when parsing declarations!");
			}
		}
	}


	/**
	 * Parse declaration, which can start with keywords TYP, VAR or FUN. Otherwise throw error.
	 *
	 * @return AST of declaration.
	 * @throws Report.Error when current symbol not one of: TYP, VAR, FUN.
	 */
	private AstDecl parseDecl() {
		switch (currSymbol.token) {
			case TYP -> {
				// Parse type declaration, which is: typ identifier = type;

				Symbol s1 = checkAndMove(Token.TYP);
				Symbol s2 = checkAndMove(Token.IDENTIFIER);
				checkAndMove(Token.ASSIGN);

				AstType type = parseType();

				checkAndMove(Token.SEMIC);

				return new AstTypDecl(s1.location, s2.lexeme, type);
			}
			case VAR -> {
				// Parse variable declaration, which is: var identifier : type;

				Symbol s1 = checkAndMove(Token.VAR);
				Symbol s2 = checkAndMove(Token.IDENTIFIER);
				checkAndMove(Token.COLON);

				AstType type = parseType();
				
				checkAndMove(Token.SEMIC);

				return new AstVarDecl(s1.location, s2.lexeme, type);
			}
			case FUN -> {
				// Parse function declaration, which is: fun identifier ( funParams ) : type = expr;

				Symbol s1 = checkAndMove(Token.FUN);
				Symbol s2 = checkAndMove(Token.IDENTIFIER);
				checkAndMove(Token.LPARENTHESIS);

				ASTs<AstParDecl> pars = parseFunParams();

				checkAndMove(Token.RPARENTHESIS);
				checkAndMove(Token.COLON);

				AstType type = parseType();

				checkAndMove(Token.ASSIGN);

				AstExpr expr = parseExpr();
				
				checkAndMove(Token.SEMIC);

				return new AstFunDecl(s1.location, s2.lexeme, pars, type, expr);
			}
			default -> {
				throw new Report.Error(currSymbol.location, "Syntax error when parsing declaration!");
			}
		}
	}

	/**
	 * Parse function parameters.
	 *
	 * @return Vector of ASTs of function parameters.
	 * @throws Report.Error when current symbol not one of: IDENTIFIER, RPARENTHESIS.
	 */
	private ASTs<AstParDecl> parseFunParams() {
		Vector<AstParDecl> pars = new Vector<>();
		switch (currSymbol.token) {
			case IDENTIFIER -> {
				// Parse function parameter, which is: identifier : type funParamsRst

				Symbol s = checkAndMove(Token.IDENTIFIER);
				checkAndMove(Token.COLON);

				AstType type = parseType();

				AstParDecl par = new AstParDecl(s.location, s.lexeme, type);
				pars.add(par);
				pars.addAll(parseFunParamsRst());
				return new ASTs<>(s.location, pars);
			}
			case RPARENTHESIS -> {
				// If right parenthesis encountered, function has no parameters.
				return new ASTs<>(currSymbol.location, pars);
			}
			default -> {
				throw new Report.Error(currSymbol.location, "Syntax error when parsing function parameters!");
			}
		}
	}

	/**
	 * Parse rest of function parameters.
	 *
	 * @return Vector of ASTs of function parameters.
	 * @throws Report.Error when current symbol not one of: COMMA, RPARENTHESIS.
	 */
	private Vector<AstParDecl> parseFunParamsRst() {
		Vector<AstParDecl> pars = new Vector<>();
		switch (currSymbol.token) {
			case COMMA -> {
				// Parse the rest of function parameter, which is: , identifier : type funParamsRst

				Symbol s1 = checkAndMove(Token.COMMA);
				Symbol s2 = checkAndMove(Token.IDENTIFIER);
				checkAndMove(Token.COLON);

				AstType type = parseType();

				AstParDecl par = new AstParDecl(s1.location, s2.lexeme, type);
				pars.add(par);
				pars.addAll(parseFunParamsRst());
				return pars;
			}
			case RPARENTHESIS -> {
				// If right parenthesis encountered, there are no more parameters.
				return pars;
			}
			default -> {
				throw new Report.Error(currSymbol.location, "Syntax error when parsing function parameters!");
			}
		}
	}


	/**
	 * Parse data types, which can be void, char, int, id, [ expr ] type, ^ type or ( type ).
	 *
	 * @return AST of type.
	 * @throws Report.Error when current symbol not one of: VOID, CHAR, INT, IDENTIFIER, PTR, LBRACKET, LPARENTHESIS.
	 */
	private AstType parseType() {
		switch (currSymbol.token) {
			// Parse primitive types.
			case VOID -> {
				Symbol s = checkAndMove(Token.VOID);
				return new AstAtomType(s.location, AstAtomType.Kind.VOID);
			}
			case CHAR -> {
				Symbol s = checkAndMove(Token.CHAR);
				return new AstAtomType(s.location, AstAtomType.Kind.CHAR);
			}
			case INT -> {
				Symbol s = checkAndMove(Token.INT);
				return new AstAtomType(s.location, AstAtomType.Kind.INT);
			}
			// Parse named type.
			case IDENTIFIER -> {
				Symbol s = checkAndMove(Token.IDENTIFIER);
				return new AstTypeName(s.location, s.lexeme);
			}
			// Parse array type of form: [ expr ] type
			case LBRACKET -> {
				Symbol s = checkAndMove(Token.LBRACKET);
				AstExpr expr = parseExpr();

				checkAndMove(Token.RBRACKET);

				AstType type = parseType();
				return new AstArrType(s.location, type, expr);
			}
			// Parse pointer type of form: ^ type
			case PTR -> {
				Symbol s = checkAndMove(Token.PTR);
				AstType type = parseType();

				return new AstPtrType(s.location, type);
			}
			// Parse enclosed type of form: ( type ).
			case LPARENTHESIS -> {
				Symbol s = checkAndMove(Token.LPARENTHESIS);
				AstType type = parseType();

				checkAndMove(Token.RPARENTHESIS);

				return type;
			}
			default -> {
				throw new Report.Error(currSymbol.location, "Syntax error when parsing type!");
			}
		}
	}


	/**
	 * Parse statement. This could be an IF statement, a WHILE statement, an assignment or expression statement.
	 *
	 * @return AST of statement.
	 */
	private AstStmt parseStmt() {
		switch (currSymbol.token) {
			case IF -> {
				// Parse if statement, which is: if expr then stmts end; or if expr then stmts else stmts end;

				Symbol s = checkAndMove(Token.IF);
				AstExpr expr = parseExpr();

				checkAndMove(Token.THEN);

				ASTs<AstStmt> stmts = parseStmts();
				Location l = stmts.asts().get(0).location;
				AstStmt ifStmt = parseStmtElse(s, expr, new AstExprStmt(l, new AstStmtExpr(l, stmts)));

				checkAndMove(Token.END);

				return ifStmt;
			}
			case WHILE -> {
				// Parse while statement, which is: while expr do stmts end;

				Symbol s = checkAndMove(Token.WHILE);
				AstExpr expr = parseExpr();

				checkAndMove(Token.DO);

				ASTs<AstStmt> stmtsASTs = parseStmts();
				Location l = stmtsASTs.asts().get(0).location;
				AstExprStmt stmts = new AstExprStmt(l, new AstStmtExpr(l, stmtsASTs));

				checkAndMove(Token.END);

				return new AstWhileStmt(s.location, expr, stmts);
			}
			default -> {
				// Parse expression or assignment statement.
				AstExpr expr = parseExpr();
				return parseExprAsgn(expr);
			}
		}
	}

	/**
	 * Parse multiple statements.
	 *
	 * @return ASTs of multiple statements.
	 */
	private ASTs<AstStmt> parseStmts() {
		AstStmt stmt = parseStmt();
		Vector<AstStmt> stmts = parseStmtsRst(stmt);

		return new ASTs<>(stmt.location, stmts);
	}

	/**
	 * Parse rest of statements.
	 *
	 * @param stmt AST of first statement.
	 * @return ASTs of rest of statements.
	 * @throws Report.Error when current symbol not one of: SEMIC.
	 */
	private Vector<AstStmt> parseStmtsRst(AstStmt stmt) {
		switch (currSymbol.token) {
			case SEMIC -> {
				// Parse rest of statements, which is: ; stmts'

				checkAndMove(Token.SEMIC);
				return parseStmts_(stmt);

			}
			default -> {
				throw new Report.Error(currSymbol.location, "Syntax error when parsing statements!");
			}
		}
	}

	/**
	 * Parse assignment expression.
	 *
	 * @param lExpr AST of expression.
	 * @return AST of assignment expression.
	 */
	private AstStmt parseExprAsgn(AstExpr lExpr) {
		switch (currSymbol.token) {
			case ASSIGN -> {
				// Parse assignment expression, which is: = expr ;

				checkAndMove(Token.ASSIGN);
				AstExpr expr = parseExpr();
				return new AstAssignStmt(lExpr.location, lExpr, expr);
			}
			default -> {
				return new AstExprStmt(lExpr.location, lExpr);
			}
		}
	}

	/**
	 * Parse else statement.
	 *
	 * @param s     First symbol of if statement.
	 * @param expr  AST of expression.
	 * @param lStmt AST of statement.
	 * @return AST of if(else) statement.
	 */
	private AstStmt parseStmtElse(Symbol s, AstExpr expr, AstStmt lStmt) {
		switch (currSymbol.token) {
			case ELSE -> {
				// Parse else statement, which is: else stmts

				checkAndMove(Token.ELSE);

				ASTs<AstStmt> stmts = parseStmts();
				Location l = stmts.asts().get(0).location;
				return new AstIfStmt(s.location, expr, lStmt, new AstExprStmt(l, new AstStmtExpr(l, stmts)));
			}
			default -> {
				return new AstIfStmt(s.location, expr, lStmt);
			}
		}
	}

	/**
	 * Parse stmts' nonterminal. This is needed so we can have multiple statements follow each other. If it encounters
	 * a semicolon, a right brace, an END symbol or ELSE symbol, that signifies that statements are over. Otherwise it
	 * loops back to stmts nonterminal.
	 *
	 * @param lStmt AST of first statement.
	 * @return Vector of ASTs of rest of statements.
	 */
	private Vector<AstStmt> parseStmts_(AstStmt lStmt) {
		Vector<AstStmt> stmts = new Vector<>();
		stmts.add(lStmt);
		switch (currSymbol.token) {
			case SEMIC, RBRACE, END, ELSE -> {
				return stmts;
			}
			default -> {
				stmts.addAll(parseStmts().asts());
				return stmts;
			}
		}
	}

	/**
	 * Parse expression.
	 *
	 * @return AST of expression.
	 */
	private AstExpr parseExpr() {
		switch (currSymbol.token) {
			case NEW, DEL -> {
				// Initialize parsing of allocation expression.

				return parseExprAlloc();
			}
			case LBRACE -> {
				// Initialize parsing of compound expression.

				Symbol s = checkAndMove(Token.LBRACE);

				ASTs<AstStmt> stmts = parseStmts();

				checkAndMove(Token.RBRACE);

				return new AstStmtExpr(s.location, stmts);
			}
			default -> {
				// Initialize parsing of disjunction expression.

				return parseExprDis();
			}
		}
	}

	/**
	 * Parse allocation expression.
	 *
	 * @return AST of allocation expression.
	 * @throws Report.Error when current symbol not one of: NEW, DEL.
	 */
	private AstExpr parseExprAlloc() {
		switch (currSymbol.token) {
			case NEW -> {
				// Parse allocation expression, which is: new expr

				Symbol s = checkAndMove(Token.NEW);

				AstExpr expr = parseExpr();
				return new AstPreExpr(s.location, AstPreExpr.Oper.NEW, expr);
			}
			case DEL -> {
				// Parse allocation expression, which is: del expr

				Symbol s = checkAndMove(Token.DEL);

				AstExpr expr = parseExpr();
				return new AstPreExpr(s.location, AstPreExpr.Oper.DEL, expr);
			}
			default -> {
				throw new Report.Error(currSymbol.location, "Syntax error when parsing allocation expression!");
			}
		}
	}

	/**
	 * Parse disjunction ( | ) expression.
	 *
	 * @return AST of disjunction expression.
	 */
	private AstExpr parseExprDis() {
		AstExpr expr = parseExprCon();
		return parseExprDisRst(expr);
	}

	/**
	 * Parse rest of disjunction expression.
	 *
	 * @param lExpr AST of first expression.
	 * @return AST of rest of disjunction expression.
	 */
	private AstExpr parseExprDisRst(AstExpr lExpr) {
		switch (currSymbol.token) {
			case OR -> {
				// Parse rest of disjunction expression, which is: | exprCon exprDisRst

				Symbol s = checkAndMove(Token.OR);

				AstExpr exprCon = parseExprCon();
				AstExpr exprDis = new AstBinExpr(s.location, AstBinExpr.Oper.OR, lExpr, exprCon);
				return parseExprDisRst(exprDis);
			}
			default -> {
				return lExpr;
			}
		}
	}

	/**
	 * Parse conjunction ( & ) expression.
	 *
	 * @return AST of conjunction expression.
	 */
	private AstExpr parseExprCon() {
		AstExpr expr = parseExprRel();
		return parseExprConRst(expr);
	}

	/**
	 * Parse rest of conjunction expression.
	 *
	 * @param lExpr AST of first expression.
	 * @return AST of rest of conjunction expression.
	 */
	private AstExpr parseExprConRst(AstExpr lExpr) {
		switch (currSymbol.token) {
			case AND -> {
				// Parse rest of disjunction expression, which is: & exprRel exprConRst

				Symbol s = checkAndMove(Token.AND);

				AstExpr exprRel = parseExprRel();
				AstExpr exprCon = new AstBinExpr(s.location, AstBinExpr.Oper.AND, lExpr, exprRel);
				return parseExprConRst(exprCon);
			}
			default -> {
				return lExpr;
			}
		}
	}

	/**
	 * Start parsing relational expression. This includes parsing additive expression and rest of relational expression,
	 * which starts with a relational operator ( ==, !=, <, >, <=, >= ).
	 *
	 * @return AST of relational expression.
	 */
	private AstExpr parseExprRel() {
		AstExpr expr = parseExprAdd();
		return parseExprRelRst(expr);
	}

	/**
	 * Parse rest of relational expression.
	 *
	 * @param lExpr AST of first expression.
	 * @return AST of rest of relational expression.
	 */
	private AstExpr parseExprRelRst(AstExpr lExpr) {
		switch (currSymbol.token) {
			case EQU -> {
				// Parse rest of relational expression, which is: == exprAdd exprRelRst

				Symbol s = checkAndMove(Token.EQU);

				AstExpr exprAdd = parseExprAdd();
				AstExpr exprRel = new AstBinExpr(s.location, AstBinExpr.Oper.EQU, lExpr, exprAdd);
				return parseExprRelRst(exprRel);
			}
			case NEQ -> {
				// Parse rest of relational expression, which is: != exprAdd exprRelRst

				Symbol s = checkAndMove(Token.NEQ);

				AstExpr exprAdd = parseExprAdd();
				AstExpr exprRel = new AstBinExpr(s.location, AstBinExpr.Oper.NEQ, lExpr, exprAdd);
				return parseExprRelRst(exprRel);
			}
			case GEQ -> {
				// Parse rest of relational expression, which is: >= exprAdd exprRelRst

				Symbol s = checkAndMove(Token.GEQ);

				AstExpr exprAdd = parseExprAdd();
				AstExpr exprRel = new AstBinExpr(s.location, AstBinExpr.Oper.GEQ, lExpr, exprAdd);
				return parseExprRelRst(exprRel);
			}
			case LEQ -> {
				// Parse rest of relational expression, which is: <= exprAdd exprRelRst

				Symbol s = checkAndMove(Token.LEQ);

				AstExpr exprAdd = parseExprAdd();
				AstExpr exprRel = new AstBinExpr(s.location, AstBinExpr.Oper.LEQ, lExpr, exprAdd);
				return parseExprRelRst(exprRel);
			}
			case LTH -> {
				// Parse rest of relational expression, which is: > exprAdd exprRelRst

				Symbol s = checkAndMove(Token.LTH);

				AstExpr exprAdd = parseExprAdd();
				AstExpr exprRel = new AstBinExpr(s.location, AstBinExpr.Oper.LTH, lExpr, exprAdd);
				return parseExprRelRst(exprRel);
			}
			case GTH -> {
				// Parse rest of relational expression, which is: < exprAdd exprRelRst

				Symbol s = checkAndMove(Token.GTH);

				AstExpr exprAdd = parseExprAdd();
				AstExpr exprRel = new AstBinExpr(s.location, AstBinExpr.Oper.GTH, lExpr, exprAdd);
				return parseExprRelRst(exprRel);
			}
			default -> {
				return lExpr;
			}
		}
	}

	/**
	 * Parse additive expression. This includes parsing of multiplicative expression and rest of additive expression,
	 * which starts with an additive operator ( +, - ).
	 *
	 * @return AST of additive expression.
	 */
	private AstExpr parseExprAdd() {
		AstExpr expr = parseExprMul();
		return parseExprAddRst(expr);
	}

	/**
	 * Parse rest of additive expression.
	 *
	 * @param lExpr AST of first expression.
	 * @return AST of rest of additive expression.
	 */
	private AstExpr parseExprAddRst(AstExpr lExpr) {
		switch (currSymbol.token) {
			case ADD -> {
				// Parse rest of additive expression, which is: + exprMul exprAddRst

				Symbol s = checkAndMove(Token.ADD);

				AstExpr exprMul = parseExprMul();
				AstExpr exprAdd = new AstBinExpr(s.location, AstBinExpr.Oper.ADD, lExpr, exprMul);
				return parseExprAddRst(exprAdd);
			}
			case SUB -> {
				// Parse rest of additive expression, which is: - exprMul exprAddRst

				Symbol s = checkAndMove(Token.SUB);

				AstExpr exprMul = parseExprMul();
				AstExpr exprAdd = new AstBinExpr(s.location, AstBinExpr.Oper.SUB, lExpr, exprMul);
				return parseExprAddRst(exprAdd);
			}
			default -> {
				return lExpr;
			}
		}
	}

	/**
	 * Parse multiplicative expression. This includes parsing of prefix expression and rest of multiplicative expression,
	 * which starts with a multiplicative operator ( *, /, % ).
	 *
	 * @return AST of multiplicative expression.
	 */
	private AstExpr parseExprMul() {
		AstExpr expr = parseExprPref();
		return parseExprMulRst(expr);
	}

	/**
	 * Parse rest of multiplicative expression.
	 *
	 * @param lExpr AST of first expression.
	 * @return AST of rest of multiplicative expression.
	 */
	private AstExpr parseExprMulRst(AstExpr lExpr) {
		switch (currSymbol.token) {
			case MUL -> {
				// Parse rest of multiplicative expression, which is: * exprPref exprMulRst

				Symbol s = checkAndMove(Token.MUL);

				AstExpr exprPref = parseExprPref();
				AstExpr exprMul = new AstBinExpr(s.location, AstBinExpr.Oper.MUL, lExpr, exprPref);
				return parseExprMulRst(exprMul);
			}
			case DIV -> {
				// Parse rest of multiplicative expression, which is: / exprPref exprMulRst

				Symbol s = checkAndMove(Token.DIV);

				AstExpr exprPref = parseExprPref();
				AstExpr exprMul = new AstBinExpr(s.location, AstBinExpr.Oper.DIV, lExpr, exprPref);
				return parseExprMulRst(exprMul);
			}
			case MOD -> {
				// Parse rest of multiplicative expression, which is: % exprPref exprMulRst

				Symbol s = checkAndMove(Token.MOD);

				AstExpr exprPref = parseExprPref();
				AstExpr exprMul = new AstBinExpr(s.location, AstBinExpr.Oper.MOD, lExpr, exprPref);
				return parseExprMulRst(exprMul);
			}
			default -> {
				return lExpr;
			}
		}
	}

	/**
	 * Parse prefix expression, which is an expression that starts with a prefix ( !, +, -, ^ ). It can also parse into
	 * a postfix expression.
	 *
	 * @return AST of prefix expression.
	 */
	private AstExpr parseExprPref() {
		switch (currSymbol.token) {
			case NOT -> {
				// Parse rest of prefix expression, which is: ! exprPref

				Symbol s = checkAndMove(Token.NOT);

				AstExpr exprPref = parseExprPref();
				return new AstPreExpr(s.location, AstPreExpr.Oper.NOT, exprPref);
			}
			case ADD -> {
				// Parse rest of prefix expression, which is: + exprPref

				Symbol s = checkAndMove(Token.ADD);

				AstExpr exprPref = parseExprPref();
				return new AstPreExpr(s.location, AstPreExpr.Oper.ADD, exprPref);
			}
			case SUB -> {
				// Parse rest of prefix expression, which is: - exprPref

				Symbol s = checkAndMove(Token.SUB);

				AstExpr exprPref = parseExprPref();
				return new AstPreExpr(s.location, AstPreExpr.Oper.SUB, exprPref);
			}
			case PTR -> {
				// Parse rest of prefix expression, which is: ^ exprPref

				Symbol s = checkAndMove(Token.PTR);

				AstExpr exprPref = parseExprPref();
				return new AstPreExpr(s.location, AstPreExpr.Oper.PTR, exprPref);
			}
			default -> {
				// Initialize parsing of postfix expression.
				return parseExprPost();
			}
		}
	}

	/**
	 * Parse postfix expression, which includes parsing an atomic expression and the rest of postfix expression, which
	 * is a postfix ( [ expr ], ^ ).
	 *
	 * @return AST of postfix expression.
	 */
	private AstExpr parseExprPost() {
		AstExpr expr = parseExprAtom();
		return parseExprPostRst(expr);
	}

	/**
	 * Parse rest of postfix expression.
	 *
	 * @param lExpr AST of first expression.
	 * @return AST of rest of postfix expression.
	 */
	private AstExpr parseExprPostRst(AstExpr lExpr) {
		switch (currSymbol.token) {
			case LBRACKET -> {
				// Parse rest of postfix expression, which is: [ expr ]

				Symbol s = checkAndMove(Token.LBRACKET);

				AstExpr expr = parseExpr();

				checkAndMove(Token.RBRACKET);

				AstExpr post = new AstBinExpr(s.location, AstBinExpr.Oper.ARR, lExpr, expr);
				return parseExprPostRst(post);
			}
			case PTR -> {
				// Parse rest of postfix expression, which is: ^

				Symbol s = checkAndMove(Token.PTR);

				AstExpr post = new AstPstExpr(s.location, AstPstExpr.Oper.PTR, lExpr);

				return parseExprPostRst(post);
			}
			default -> {
				return lExpr;
			}
		}
	}

	/**
	 * Parse atomic expression, which is an expression that cannot be simplified further. This includes constants,
	 * identifiers, enclosed, typecast and where expressions.
	 *
	 * @return AST of atomic expression.
	 * @throws Report.Error when current symbol not one of: CONSTVOID, CONSTCHAR, CONSTINT, CONSTPTR, IDENTIFIER,
	 * 														LPARENTHESIS.
	 */
	private AstExpr parseExprAtom() {
		switch (currSymbol.token) {
			case CONSTVOID -> {
				Symbol s = checkAndMove(Token.CONSTVOID);
				return new AstConstExpr(s.location, AstConstExpr.Kind.VOID, s.lexeme);
			}
			case CONSTCHAR -> {
				Symbol s = checkAndMove(Token.CONSTCHAR);
				return new AstConstExpr(s.location, AstConstExpr.Kind.CHAR, s.lexeme);
			}
			case CONSTINT -> {
				Symbol s = checkAndMove(Token.CONSTINT);
				return new AstConstExpr(s.location, AstConstExpr.Kind.INT, s.lexeme);
			}
			case CONSTPTR -> {
				Symbol s = checkAndMove(Token.CONSTPTR);
				return new AstConstExpr(s.location, AstConstExpr.Kind.PTR, s.lexeme);
			}
			case IDENTIFIER -> {
				// Parse atomic expression, which is: identifier or identifier(identParams)
				Symbol s = checkAndMove(Token.IDENTIFIER);

				AstNameExpr expr = new AstNameExpr(s.location, s.lexeme);
				return parseIdentifierRst(expr);
			}
			case LPARENTHESIS -> {
				// Parse atomic expression, which is: (expr exprRst)

				checkAndMove(Token.LPARENTHESIS);

				AstExpr expr = parseExpr();
				AstExpr exprRst = parseExprRst(expr);

				checkAndMove(Token.RPARENTHESIS);

				return exprRst;
			}
			default -> {
				throw new Report.Error(currSymbol.location, "Syntax error when parsing atomic expression!");
			}
		}
	}

	/**
	 * Parse rest of identifier. This is used in case of a function call.
	 *
	 * @param lExpr AST of first expression.
	 * @return AST of rest of identifier.
	 */
	private AstExpr parseIdentifierRst(AstNameExpr lExpr) {
		Vector<AstExpr> pars = new Vector<>();
		switch (currSymbol.token) {
			case LPARENTHESIS -> {
				Symbol s = checkAndMove(Token.LPARENTHESIS);

				if (currSymbol.token != Token.RPARENTHESIS) {
					pars.addAll(parseIdentParams());
				}

				ASTs<AstExpr> exprs = new ASTs<>(s.location, pars);

				checkAndMove(Token.RPARENTHESIS);

				return new AstCallExpr(lExpr.location, lExpr.name, exprs);
			}
			default -> {
				return lExpr;
			}
		}
	}

	/**
	 * Parse parameters of rest of identifier (function call).
	 *
	 * @return Vector of ASTs of parameters of function call.
	 */
	private Vector<AstExpr> parseIdentParams() {
		Vector<AstExpr> params = new Vector<>();

		params.add(parseExpr());
		params.addAll(parseIdentParamsRst());
		return params;
	}

	/**
	 * Parse rest of identifier parameters.
	 *
	 * @return Vector of ASTs of rest of identifier parameters.
	 */
	private Vector<AstExpr> parseIdentParamsRst() {
		Vector<AstExpr> params = new Vector<>();
		switch (currSymbol.token) {
			case COMMA -> {
				checkAndMove(Token.COMMA);

				params.add(parseExpr());
				params.addAll(parseIdentParamsRst());
				return params;
			}
			default -> {
				return params;
			}
		}
	}

	/**
	 * Parse rest of expression. This could be a typecast, a where expression or nothing.
	 *
	 * @param lExpr AST of first expression.
	 * @return AST of rest of expression.
	 */
	private AstExpr parseExprRst(AstExpr lExpr) {
		switch (currSymbol.token) {
			case COLON -> {
				// Parse rest of expression, which is: : typecast
				return parseTypecast(lExpr);
			}
			case WHERE -> {
				// Parse rest of expression, which is: where decls
				return parseExprWhere(lExpr);
			}
			default -> {
				return lExpr;
			}
		}
	}

	/**
	 * Parse typecast part of expression.
	 *
	 * @param lExpr AST of first expression.
	 * @return AST of typecast part of expression.
	 * @throws Report.Error when current symbol not one of: COLON.
	 */
	private AstCastExpr parseTypecast(AstExpr lExpr) {
		switch (currSymbol.token) {
			case COLON -> {
				// Parse rest of expression, which is: : typecast

				Symbol s = checkAndMove(Token.COLON);

				AstType type = parseType();
				return new AstCastExpr(s.location, lExpr, type);
			}
			default -> {
				throw new Report.Error(currSymbol.location, "Syntax error when parsing type cast!");
			}
		}
	}

	/**
	 * Parse where part of expression.
	 *
	 * @param lExpr AST of first expression.
	 * @return AST of where part of expression.
	 * @throws Report.Error when current symbol not one of: WHERE.
	 */
	private AstWhereExpr parseExprWhere(AstExpr lExpr) {
		switch (currSymbol.token) {
			case WHERE -> {
				// Parse rest of expression, which is: where decls

				Symbol s = checkAndMove(Token.WHERE);

				Vector<AstDecl> decls = parseDecls();
				ASTs<AstDecl> declsAst = new ASTs<>(s.location, decls);
				return new AstWhereExpr(s.location, declsAst, lExpr);
			}
			default -> {
				throw new Report.Error(currSymbol.location, "Syntax error when parsing where expression!");
			}
		}
	}

}
