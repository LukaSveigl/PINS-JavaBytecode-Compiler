package pins.phase.imcgen;

import java.util.*;

import pins.common.report.Report;
import pins.data.ast.*;
import pins.data.ast.visitor.*;
import pins.data.imc.code.stmt.*;
import pins.data.imc.code.expr.*;
import pins.data.mem.*;
import pins.phase.seman.SemAn;

/**
 * The statement intermediate code generator.
 */
public class StmtGenerator implements AstVisitor<ImcStmt, Stack<MemFrame>> {

	/**
	 * The visit method for the assignment statement.
	 *
	 * @param assignStmt The assignment statement to visit.
	 * @param frames The stack of memory frames.
	 * @return The assignment statement intermediate code.
	 */
	@Override
	public ImcStmt visit(AstAssignStmt assignStmt, Stack<MemFrame> frames) {
		// Assemble the assignment statement.
		ImcStmt code = new ImcMOVE(assignStmt.fstSubExpr.accept(new ExprGenerator(), frames),
				assignStmt.sndSubExpr.accept(new ExprGenerator(), frames));
		ImcGen.stmtImc.put(assignStmt, code);
		return code;
	}

	/**
	 * The visit method for the expression statement.
	 *
	 * @param exprStmt The expression statement to visit.
	 * @param frames The stack of memory frames.
	 * @return The expression statement intermediate code.
	 */
	@Override
	public ImcStmt visit(AstExprStmt exprStmt, Stack<MemFrame> frames) {
		ImcExpr expr = exprStmt.expr.accept(new ExprGenerator(), frames);
		ImcESTMT eStmt = new ImcESTMT(expr);
		ImcGen.stmtImc.put(exprStmt, eStmt);
		return eStmt;
	}

	/**
	 * The visit method for the if statement.
	 *
	 * @param ifStmt The if statement to visit.
	 * @param frames The stack of memory frames.
	 * @return The if statement intermediate code.
	 */
	@Override
	public ImcStmt visit(AstIfStmt ifStmt, Stack<MemFrame> frames) {
		// The if statement is represented by a list of statements, such as labels, jumps, etc.
		ImcSTMTS stmts = new ImcSTMTS(new Vector<>());
		ImcExpr cond = ifStmt.condExpr.accept(new ExprGenerator(), frames);

		// Create if positive and negative jump labels.
		MemLabel posLabel = new MemLabel();
		MemLabel negLabel = new MemLabel();

		// Create if conditional jump, which jumps to the positive label (then body) if the condition is true,
		// and to the negative label (else body) if the condition is false.
		ImcCJUMP cjump = new ImcCJUMP(cond, posLabel, negLabel);

		ImcStmt thenBody = ifStmt.thenBodyStmt.accept(this, frames);

		if (ifStmt.elseBodyStmt != null) {
			// Create end label to jump to. This is needed to skip the else body.
			MemLabel endLabel = new MemLabel();

			ImcStmt elseBody = ifStmt.elseBodyStmt.accept(this, frames);

			// Assemble the if statement of form:
			// CJUMP(cond, PL, NL) - PL - thenBody - JUMP(end) - NL - elseBody - end
			stmts.stmts.add(cjump);
			stmts.stmts.add(new ImcLABEL(posLabel));
			stmts.stmts.add(thenBody);
			stmts.stmts.add(new ImcJUMP(endLabel));
			stmts.stmts.add(new ImcLABEL(negLabel));
			stmts.stmts.add(elseBody);
			stmts.stmts.add(new ImcLABEL(endLabel));
		}
		else {
			// Assemble the if statement of form:
			// CJUMP(cond, PL, NL) - PL - thenBody - NL
			stmts.stmts.add(cjump);
			stmts.stmts.add(new ImcLABEL(posLabel));
			stmts.stmts.add(thenBody);
			stmts.stmts.add(new ImcLABEL(negLabel));
		}

		ImcGen.stmtImc.put(ifStmt, stmts);
		return stmts;
	}

	/**
	 * The visit method for the while statement.
	 *
	 * @param whileStmt The while statement to visit.
	 * @param frames The stack of memory frames.
	 * @return The while statement intermediate code.
	 */
	@Override
	public ImcStmt visit(AstWhileStmt whileStmt, Stack<MemFrame> frames) {
		// The while loop is represented by a list of statements, such as labels, jumps, etc.
		ImcSTMTS stmts = new ImcSTMTS(new Vector<>());
		ImcExpr cond = whileStmt.condExpr.accept(new ExprGenerator(), frames);

		// Create the labels needed for the while loop, which are the condition label, the positive label,
		// and the negative label.
		MemLabel condLabel = new MemLabel();
		MemLabel posLabel = new MemLabel();
		MemLabel negLabel = new MemLabel();

		// Create loop conditional jump and the loopback jump.
		ImcCJUMP cjump = new ImcCJUMP(cond, posLabel, negLabel);
		ImcJUMP jump = new ImcJUMP(condLabel);

		ImcStmt body = whileStmt.bodyStmt.accept(this, frames);

		// Assemble the loop statement of form:
		// condLabel - CJUMP(cond, PL, NL) - PL - body - JUMP(condLabel) - NL
		stmts.stmts.add(new ImcLABEL(condLabel));
		stmts.stmts.add(cjump);
		stmts.stmts.add(new ImcLABEL(posLabel));
		stmts.stmts.add(body);
		stmts.stmts.add(jump);
		stmts.stmts.add(new ImcLABEL(negLabel));

		ImcGen.stmtImc.put(whileStmt, stmts);
		return stmts;
	}

	/**
	 * The visit method for the list of statements.
	 *
	 * @param stmts The list of statements to visit.
	 * @param frames The stack of memory frames.
	 * @return The list of statements intermediate code.
	 */
	@Override
	public ImcStmt visit(ASTs<?> stmts, Stack<MemFrame> frames) {
		// Create statement list
		ImcSTMTS temp = new ImcSTMTS(new Vector<>());

		// Evaluate each statement
		for (AST aStmt : stmts.asts()) {
			ImcStmt iStmt = aStmt.accept(this, frames);
			ImcGen.stmtImc.put((AstStmt)aStmt, iStmt);
			temp.stmts.add(iStmt);
		}

		return temp;
	}

}
