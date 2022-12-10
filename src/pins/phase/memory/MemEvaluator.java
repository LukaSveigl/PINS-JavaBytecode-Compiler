package pins.phase.memory;

import pins.data.ast.*;
import pins.data.ast.visitor.*;
import pins.data.mem.*;
import pins.data.typ.*;
import pins.phase.seman.*;

/**
 * Computing memory layout: frames and accesses.
 */
public class MemEvaluator extends AstFullVisitor<Object, MemEvaluator.FunContext> {

	/**
	 * Functional context, i.e., used when traversing function and building a new
	 * frame, parameter accesses and variable accesses.
	 */
	protected class FunContext {
		public int depth = 0;
		public long locsSize = 0;
		public long argsSize = 0;
		public long parsSize = new SemPtr(new SemVoid()).size();
	}

	// GENERAL PURPOSE

	/**
	 * General purpose visit method, which traverses the abstract syntax trees.
	 *
	 * @param trees The abstract syntax trees to traverse.
	 * @param ctx The current function context.
	 * @return The result of the traversal.
	 */
	@Override
	public Object visit(ASTs<?> trees, FunContext ctx) {
		// If no current function context exists, create one and set starting depth.
		if (ctx == null) {
			ctx = new FunContext();
			ctx.depth = 1;
		}

		// Traverse the abstract syntax trees.
		for (AST tree : trees.asts()) {
			tree.accept(this, ctx);
			// If the subtree accepted was a function, reset its parameters size.
			if (tree instanceof AstFunDecl) {
				ctx.parsSize = new SemPtr(new SemVoid()).size();
			}
		}

		return trees;
	}

	// DECLARATIONS

	/**
	 * Visit method for variable declarations.
	 *
	 * @param varDecl The variable declaration to visit.
	 * @param ctx The current function context.
	 * @return The result of the visit.
	 */
	@Override
	public Object visit(AstVarDecl varDecl, FunContext ctx) {
		// If a local variable - add variable with relative access.
		if (ctx.depth > 1) {
			long size = SemAn.describesType.get(varDecl.type).size();
			// Calculate variable offset, which is FP - sizeof(loc1) - sizeof(loc2) - ... - sizeof(locN)
			// -ctx.locsSize = FP - sizeof(loc1) - sizeof(loc2) - ... - sizeof(locN-1).
			int offset = (int) (-ctx.locsSize - size);

			// Increase size of local variables.
			ctx.locsSize += size;

			Memory.varAccesses.put(varDecl, new MemRelAccess(size, offset, ctx.depth));
		}
		// If global variable - add variable with absolute access.
		else {
			Memory.varAccesses.put(varDecl, new MemAbsAccess(
					SemAn.describesType.get(varDecl.type).size(),
					new MemLabel(varDecl.name)));
		}

		return varDecl;
	}

	/**
	 * Visit method for function declarations.
	 *
	 * @param funDecl The function declaration to visit.
	 * @param ctx The current function context.
	 * @return The result of the visit.
	 */
	@Override
	public Object visit(AstFunDecl funDecl, FunContext ctx) {
		// When entering a function body, increase static depth as those variables are not part of current scope.
		ctx.depth += 1;

		// Create named label for function.
		MemLabel label = new MemLabel(funDecl.name);

		// Visit function parameters.
		for (AstParDecl parDecl : funDecl.pars.asts()) {
			parDecl.accept(this, ctx);
		}
		// Visit function body.
		funDecl.expr.accept(this, ctx);

		// When leaving a function body, decrease static depth.
		ctx.depth -= 1;

		// If function is not defined in the global scope, set its label as an anonymous label.
		if (ctx.depth > 1) {
			label = new MemLabel();
		}

		Memory.frames.put(funDecl, new MemFrame(label, ctx.depth, ctx.locsSize, ctx.argsSize));

		// Reset the function context after the frame is computed.
		ctx.argsSize = 0;
		ctx.locsSize = 0;

		return funDecl;
	}

	// PARAMETERS

	/**
	 * Visit method for parameter declarations.
	 *
	 * @param parDecl The parameter declaration to visit.
	 * @param ctx The current function context.
	 * @return The result of the visit.
	 */
	@Override
	public Object visit(AstParDecl parDecl, FunContext ctx) {
		long size = SemAn.describesType.get(parDecl.type).size();

		// Add parameter to function frame as a relative access, with it's offset being previous parameters size.
		Memory.parAccesses.put(parDecl, new MemRelAccess(size, (int) ctx.parsSize, ctx.depth));
		ctx.parsSize += size;

		return parDecl;
	}

	// EXPRESSIONS

	/**
	 * Visit method for call expressions.
	 *
	 * @param callExpr The call expression to visit.
	 * @param ctx The current function context.
	 * @return The result of the visit.
	 */
	@Override
	public Object visit(AstCallExpr callExpr, FunContext ctx) {
		long argsSize = 0;

		// Visit each argument and add it's size to the total size.
		for (AstExpr expr : callExpr.args.asts()) {
			argsSize += SemAn.exprOfType.get(expr).size();
			expr.accept(this, ctx);
		}

		// Function's arguments size is the bigger of the two:
		// 1. The current argument size.
		// 2. Current call expression's parameters size + size of static link pointer.
		ctx.argsSize = Math.max(ctx.argsSize, argsSize + new SemPtr(new SemVoid()).size());


		return callExpr;
	}

	/**
	 * Visit method for expressions.
	 *
	 * @param stmtExpr The statement expression to visit.
	 * @param ctx The current function context.
	 * @return The result of the visit.
	 */
	@Override
	public Object visit(AstStmtExpr stmtExpr, FunContext ctx) {
		// Visit each statement in the statement expression.
		for (AstStmt stmt : stmtExpr.stmts.asts()) {
			stmt.accept(this, ctx);
		}
		return stmtExpr;
	}

	/**
	 * Visit method for where expressions.
	 *
	 * @param whereExpr The where expression to visit.
	 * @param ctx The current function context.
	 * @return The result of the visit.
	 */
	@Override
	public Object visit(AstWhereExpr whereExpr, FunContext ctx) {
		// First visit the where expression declarations.
		for (AstDecl decl : whereExpr.decls.asts()) {
			if (decl instanceof AstFunDecl) {
				// Create a new function context for the function.
				FunContext nextCtx = new FunContext();
				nextCtx.depth = ctx.depth;
				// Accept the function declaration.
				decl.accept(this, nextCtx);
			}
			else {
				decl.accept(this, ctx);
			}
		}
		whereExpr.subExpr.accept(this, ctx);
		return whereExpr;
	}

	// STATEMENTS

	/**
	 * Visit method for expression statements.
	 *
	 * @param exprStmt The expression statement to visit.
	 * @param ctx The current function context.
	 * @return The result of the visit.
	 */
	@Override
	public Object visit(AstExprStmt exprStmt, FunContext ctx) {
		exprStmt.expr.accept(this, ctx);
		return exprStmt;
	}

}
