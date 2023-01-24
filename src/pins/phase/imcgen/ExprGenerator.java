package pins.phase.imcgen;

import java.util.*;

import pins.common.report.Report;
import pins.data.ast.*;
import pins.data.ast.visitor.*;
import pins.data.typ.*;
import pins.data.imc.code.expr.*;
import pins.data.imc.code.stmt.*;
import pins.data.mem.*;
import pins.phase.memory.*;
import pins.phase.seman.*;

/**
 * The expression intermediate code generator.
 */
public class ExprGenerator implements AstVisitor<ImcExpr, Stack<MemFrame>> {

	/**
	 * The visit method for the where expression.
	 *
	 * @param whereExpr The where expression to visit.
	 * @param frames    The stack of memory frames.
	 * @return The where expression intermediate code.
	 */
	@Override
	public ImcExpr visit(AstWhereExpr whereExpr, Stack<MemFrame> frames) {
		whereExpr.decls.accept(new CodeGenerator(), frames);
		ImcExpr code = whereExpr.subExpr.accept(this, frames);
		ImcGen.exprImc.put(whereExpr, code);
		return code;
	}

	/**
	 * The visit method for the statement expression.
	 *
	 * @param stmtExpr The statement expression to visit.
	 * @param frames   The stack of memory frames.
	 * @return The statement expression intermediate code.
	 */
	@Override
	public ImcExpr visit(AstStmtExpr stmtExpr, Stack<MemFrame> frames) {
		// Evaluate all statements in the statement expression first.
		ImcSTMTS stmts = (ImcSTMTS) stmtExpr.stmts.accept(new StmtGenerator(), frames);
		ImcSEXPR sexpr = null;

		// If the last statement in the statement expression is a return statement, then the statement expression's
		// value is the return value of the last statement. Otherwise the statement expression's value is a 0 constant.
		if (stmts.stmts.get(stmts.stmts.size() - 1) instanceof ImcESTMT) {
			ImcESTMT retStmt = (ImcESTMT) stmts.stmts.get(stmts.stmts.size() - 1);
			stmts.stmts.remove(stmts.stmts.size() - 1);
			sexpr = new ImcSEXPR(stmts, retStmt.expr);
		}
		else {
			sexpr = new ImcSEXPR(stmts, new ImcCONST(0));
		}
		ImcGen.exprImc.put(stmtExpr, sexpr);
		return sexpr;
	}

	/**
	 * The visit method for the const expression.
	 *
	 * @param constExpr The const expression to visit.
	 * @param frames    The stack of memory frames.
	 * @return The const expression intermediate code.
	 */
	@Override
	public ImcExpr visit(AstConstExpr constExpr, Stack<MemFrame> frames) {
		ImcCONST constImc = null;

		switch (constExpr.kind) {
			case INT -> {
				// The integer value is it's name.
				long value = Integer.parseInt(constExpr.name);
				constImc = new ImcCONST(value);
			}
			case CHAR -> {
				// The character value is the middle character of the name, because the name is of the form 'c' or '\''.
				long value = -1;
				if (constExpr.name.length() == 3) {
					value = constExpr.name.charAt(1);
				}
				else {
					value = constExpr.name.charAt(2);
				}
				constImc = new ImcCONST(value);
			}
			case VOID, PTR -> {
				constImc = new ImcCONST(0);
			}
		}
		ImcGen.exprImc.put(constExpr, constImc);
		return constImc;
	}

	/**
	 * The visit method for the name expression.
	 *
	 * @param nameExpr The name expression to visit.
	 * @param frames   The stack of memory frames.
	 * @return The name expression intermediate code.
	 */
	@Override
	public ImcExpr visit(AstNameExpr nameExpr, Stack<MemFrame> frames) {
		ImcCONST offset = null;
		MemRelAccess relAccess = null;

		// As the name expression can be a variable or function parameter, we need to check which it is, because they
		// are accessed differently. In case it is a global variable, we generate the intermediate code immediately,
		// otherwise we calculate the offset and generate the memory access code later.
		if (SemAn.declaredAt.get(nameExpr) instanceof AstParDecl) {
			AstParDecl parDecl = (AstParDecl) SemAn.declaredAt.get(nameExpr);
			relAccess = Memory.parAccesses.get(parDecl);
			offset = new ImcCONST(relAccess.offset);
		}
		else if (SemAn.declaredAt.get(nameExpr) instanceof AstVarDecl) {
			AstVarDecl varDecl = (AstVarDecl) SemAn.declaredAt.get(nameExpr);
			// Resolve as relative access - local variable.
			if (Memory.varAccesses.get(varDecl) instanceof MemRelAccess) {
				relAccess = (MemRelAccess) Memory.varAccesses.get(varDecl);
				offset = new ImcCONST(relAccess.offset);
			}
			// Resolve as absolute access - global variable.
			else {
				MemAbsAccess access = (MemAbsAccess) Memory.varAccesses.get(varDecl);
				ImcNAME name = new ImcNAME(access.label);
				ImcMEM mem = new ImcMEM(name);
				ImcGen.exprImc.put(nameExpr, mem);
				return mem;
			}
		}
		else {
			// This should never happen, but it is included so the compiler doesn't throw a warning about
			// a nullPointerException at the 'long depthDifference' line.
			throw new Report.InternalError();
		}

		// The depthDifference is the difference between the current frame and the frame of the variable.
		// It is needed to calculate the number of ImcMEM calls needed to access the function FP register of
		// the function in which the variable is declared.
		long depthDifference = Math.abs(relAccess.depth - frames.peek().depth - 1);

		ImcTEMP FP = new ImcTEMP(frames.peek().FP);
		ImcMEM mem = null;

		if (depthDifference > 0) {
			mem = new ImcMEM(FP);
			for (int i = 1; i < depthDifference; i++) {
				mem = new ImcMEM(mem);
			}
			// The full memory access is the access to the FP register of the function in which the variable is declared
			// plus the offset of the variable.
			ImcMEM fullMem = new ImcMEM(new ImcBINOP(ImcBINOP.Oper.ADD, mem, offset));
			ImcGen.exprImc.put(nameExpr, fullMem);
			return fullMem;
		}
		// The full memory access is the access to the FP register of the function in which the variable is declared
		// plus the offset of the variable.
		ImcMEM fullMem = new ImcMEM(new ImcBINOP(ImcBINOP.Oper.ADD, FP, offset));
		ImcGen.exprImc.put(nameExpr, fullMem);
		return fullMem;
	}

	/**
	 * The visit method for the call expression.
	 *
	 * @param callExpr The call expression to visit.
	 * @param frames   The stack of memory frames.
	 * @return The call expression intermediate code.
	 */
	@Override
	public ImcExpr visit(AstCallExpr callExpr, Stack<MemFrame> frames) {
		// The vectors of arguments and offsets. The offsets are needed to calculate the correct offset of the
		// arguments in the stack. The offset of the first argument (SL) is 0, next is increased by size of the
		// argument datatype (should always be 8), and so on.
		Vector<ImcExpr> args = new Vector<>();
		Vector<Long> offs = new Vector<>();

		// Find the called function's stack frame and get it's label.
		MemFrame funFrame = Memory.frames.get((AstFunDecl) SemAn.declaredAt.get(callExpr));
		MemLabel label = funFrame.label;
		ImcTEMP FP = new ImcTEMP(frames.peek().FP);
		// The depthDifference is the difference between the current frame and the frame of the variable.
		// It is needed to calculate the number of ImcMEM calls needed to access the function FP register of
		// the function in which the variable is declared.
		long depthDifference = Math.abs(funFrame.depth - frames.peek().depth - 1);

		if (depthDifference > 0) {
			ImcMEM mem = new ImcMEM(FP);
			for (int i = 1; i < depthDifference; i++) {
				mem = new ImcMEM(mem);
			}
			args.add(mem);
			offs.add(0L);
		}
		else {
			args.add(FP);
			offs.add(0L);
		}

		// Loop through all arguments and calculate their offsets. Starting offset is 8 due to the static link.
		long offset = 8L;
		for (AstExpr arg : callExpr.args.asts()) {
			offs.add(offset);
			args.add(arg.accept(this, frames));
			offset += SemAn.exprOfType.get(arg).size();
		}

		ImcCALL call = new ImcCALL(label, offs, args);
		ImcGen.exprImc.put(callExpr, call);
		return call;
	}

	/**
	 * The visit method for the binary expression.
	 *
	 * @param binExpr The binary expression to visit.
	 * @param frames  The stack of memory frames.
	 * @return The binary expression intermediate code.
	 */
	@Override
	public ImcExpr visit(AstBinExpr binExpr, Stack<MemFrame> frames) {
		ImcExpr left = binExpr.fstSubExpr.accept(this, frames);
		ImcExpr right = binExpr.sndSubExpr.accept(this, frames);

		ImcBINOP binop = null;

		switch (binExpr.oper) {
			case ADD -> {
				binop = new ImcBINOP(ImcBINOP.Oper.ADD, left, right);
			}
			case SUB -> {
				binop = new ImcBINOP(ImcBINOP.Oper.SUB, left, right);
			}
			case MUL -> {
				binop = new ImcBINOP(ImcBINOP.Oper.MUL, left, right);
			}
			case DIV -> {
				binop = new ImcBINOP(ImcBINOP.Oper.DIV, left, right);
			}
			case MOD -> {
				binop = new ImcBINOP(ImcBINOP.Oper.MOD, left, right);
			}
			case AND -> {
				binop = new ImcBINOP(ImcBINOP.Oper.AND, left, right);
			}
			case OR -> {
				binop = new ImcBINOP(ImcBINOP.Oper.OR, left, right);
			}
			case EQU -> {
				binop = new ImcBINOP(ImcBINOP.Oper.EQU, left, right);
			}
			case NEQ -> {
				binop = new ImcBINOP(ImcBINOP.Oper.NEQ, left, right);
			}
			case LEQ -> {
				binop = new ImcBINOP(ImcBINOP.Oper.LEQ, left, right);
			}
			case LTH ->  {
				binop = new ImcBINOP(ImcBINOP.Oper.LTH, left, right);
			}
			case GEQ -> {
				binop = new ImcBINOP(ImcBINOP.Oper.GEQ, left, right);
			}
			case GTH -> {
				binop = new ImcBINOP(ImcBINOP.Oper.GTH, left, right);
			}
			case ARR -> {
				// The array indexing operator is a special case, since it is not a simple left-right operator. It
				// returns the value of the array element.

				// Get the size of an array element.
				long size = SemAn.exprOfType.get(binExpr).size();

				// First multiply the index by the size of an array element.
				ImcBINOP mul = new ImcBINOP(ImcBINOP.Oper.MUL, right, new ImcCONST(size));
				// Add the index to the array address.
				ImcBINOP add = new ImcBINOP(ImcBINOP.Oper.ADD, ((ImcMEM)left).addr, mul);
				ImcMEM fullMem = new ImcMEM(add);
				ImcGen.exprImc.put(binExpr, fullMem);
				return fullMem;
			}
		}
		ImcGen.exprImc.put(binExpr, binop);
		return binop;
	}

	/**
	 * The visit method for the prefix expression.
	 *
	 * @param preExpr The prefix expression to visit.
	 * @param frames  The stack of memory frames.
	 * @return The prefix expression intermediate code.
	 */
	@Override
	public ImcExpr visit(AstPreExpr preExpr, Stack<MemFrame> frames) {
		ImcExpr subExpr = preExpr.subExpr.accept(this, frames);
		ImcUNOP unop = null;

		// Assemble the prefix expression intermediate code. Note that new and del must be handled differently, as
		// they are not only unary operators, but also built-in functions.
		switch (preExpr.oper) {
			case NEW -> {
				Vector<ImcExpr> args = new Vector<>();
				Vector<Long> offs = new Vector<>();
				// Add the static link as the first argument.
				args.add(new ImcTEMP(frames.peek().FP));
				offs.add(0L);
				// Add the subexpression as the argument of the new function and set it's offset to 8. This is because
				// the new keyword invokes a function, which needs a static link.
				args.add(subExpr);
				offs.add(8L);
				ImcCALL call = new ImcCALL(new MemLabel("new"), offs, args);
				ImcGen.exprImc.put(preExpr, call);
				return call;
			}
			case DEL -> {
				Vector<ImcExpr> args = new Vector<>();
				Vector<Long> offs = new Vector<>();
				// Add the static link as the first argument.
				args.add(new ImcTEMP(frames.peek().FP));
				offs.add(0L);
				// Add the subexpression as the argument of the del function and set it's offset to 8. This is because
				// the new keyword invokes a function, which needs a static link.
				args.add(subExpr);
				offs.add(8L);
				ImcCALL call = new ImcCALL(new MemLabel("del"), offs, args);
				ImcGen.exprImc.put(preExpr, call);
				return call;
			}
			case NOT -> {
				unop = new ImcUNOP(ImcUNOP.Oper.NOT, subExpr);
			}
			case ADD -> {
				// The plus prefix operator is a special case, since it does nothing.
				ImcGen.exprImc.put(preExpr, subExpr);
				return subExpr;
			}
			case SUB -> {
				unop = new ImcUNOP(ImcUNOP.Oper.NEG, subExpr);
			}
			case PTR -> {
				// The pointer prefix operator is another special case, since it returns the
				// address of the sub-expression.
				ImcExpr expr = null;
				if (subExpr instanceof ImcMEM) {
					expr = ((ImcMEM) subExpr).addr;
				}
				else {
					expr = subExpr;
				}
				ImcGen.exprImc.put(preExpr, expr);
				return expr;
			}
		}
		ImcGen.exprImc.put(preExpr, unop);
		return unop;
	}

	/**
	 * The visit method for the postfix expression.
	 *
	 * @param pstExpr The postfix expression to visit.
	 * @param frames  The stack of memory frames.
	 * @return The postfix expression intermediate code.
	 */
	@Override
	public ImcExpr visit(AstPstExpr pstExpr, Stack<MemFrame> frames) {
		// The only postfix operator is the pointer operator, which returns the value that a pointer points to.
		ImcExpr subExpr = pstExpr.subExpr.accept(this, frames);
		ImcMEM mem = new ImcMEM(subExpr);
		ImcGen.exprImc.put(pstExpr, mem);
		return mem;
	}

	/**
	 * The visit method for the cast expression.
	 *
	 * @param castExpr The cast expression to visit.
	 * @param frames   The stack of memory frames.
	 * @return The cast expression intermediate code.
	 */
	@Override
	public ImcExpr visit(AstCastExpr castExpr, Stack<MemFrame> frames) {
		ImcExpr expr = castExpr.subExpr.accept(this, frames);
		ImcGen.exprImc.put(castExpr, expr);
		return expr;
	}

}
