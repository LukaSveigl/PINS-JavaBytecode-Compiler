package pins;

import java.util.*;
import pins.common.report.*;
import pins.data.ast.*;
import pins.data.mem.MemAccess;
import pins.data.mem.MemRelAccess;
import pins.phase.btcemt.BtcEmt;
import pins.phase.btcemt.CodeConverter;
import pins.phase.btcemt.CodeEmitter;
import pins.phase.btcgen.BtcGen;
import pins.phase.btcgen.ClassGenerator;
import pins.phase.lexan.*;
import pins.phase.refan.RefAn;
import pins.phase.refan.RefEvaluator;
import pins.phase.synan.*;
import pins.phase.seman.*;
import pins.phase.memory.*;
import pins.phase.imcgen.*;
import pins.phase.imclin.*;

/**
 * The PINS'22 compiler.
 */
public class Compiler {

	/** All phases of the compiler. */
	private static final String phases = "none|lexan|synan|abstr|seman|memory|imcgen|imclin|refan|btcgen|btcemt";

	/** The compilation method. */
	private static final String method = "interp|compile";

	/** Values of command line arguments. */
	private static final HashMap<String, String> cmdLine = new HashMap<String, String>();

	public static void main(String[] args) {

		try {
			Report.info("This is the PINS'22 compiler:");

			// Scan the command line.
			for (int argc = 0; argc < args.length; argc++) {
				if (args[argc].startsWith("--")) {
					// Command-line switch.
					if (args[argc].matches("--src-file-name=.*")) {
						if (cmdLine.get("--src-file-name") == null) {
							cmdLine.put("--src-file-name", args[argc]);
							continue;
						}
					}
					if (args[argc].matches("--dst-file-name=.*")) {
						if (cmdLine.get("--dst-file-name") == null) {
							cmdLine.put("--dst-file-name", args[argc]);
							continue;
						}
					}
					if (args[argc].matches("--target-phase=(" + phases + "|all)")) {
						if (cmdLine.get("--target-phase") == null) {
							cmdLine.put("--target-phase", args[argc].replaceFirst("^[^=]*=", ""));
							continue;
						}
					}
					if (args[argc].matches("--logged-phase=(" + phases + "|all)")) {
						if (cmdLine.get("--logged-phase") == null) {
							cmdLine.put("--logged-phase", args[argc].replaceFirst("^[^=]*=", ""));
							continue;
						}
					}
					if (args[argc].matches("--comp-method=(" + method + ")")) {
						if (cmdLine.get("--comp-method") == null) {
							cmdLine.put("--comp-method", args[argc].replaceFirst("^[^=]*=", ""));
							continue;
						}
					}
					Report.warning("Command line argument '" + args[argc] + "' ignored.");
				} else {
					// Source file name.
					if (cmdLine.get("--src-file-name") == null) {
						cmdLine.put("--src-file-name", args[argc]);
					} else {
						Report.warning("Source file '" + args[argc] + "' ignored.");
					}
				}
			}
			if (cmdLine.get("--src-file-name") == null) {
				throw new Report.Error("Source file not specified.");
			}
			if (cmdLine.get("--dst-file-name") == null) {
				cmdLine.put("--dst-file-name", cmdLine.get("--src-file-name").replaceFirst("\\.[^./]*$", "") + ".class");
			}
			if ((cmdLine.get("--target-phase") == null) || (cmdLine.get("--target-phase").equals("all"))) {
				cmdLine.put("--target-phase", phases.replaceFirst("^.*\\|", ""));
			}

			cmdLine.putIfAbsent("--comp-method", "interp");

			// The compilation process carried out phase by phase.
			while (true) {

				// Lexical analysis.
				if (cmdLine.get("--target-phase").equals("lexan")) {
					try (LexAn lexan = new LexAn(cmdLine.get("--src-file-name"))) {
						pins.data.symbol.Symbol symbol;
						do {
							symbol = lexan.lexer();
							symbol.log("");
						} while (symbol.token != pins.data.symbol.Token.EOF);
					}
					break;
				}

				// Syntax analysis.
				AST ast = null;
				try (LexAn lexan = new LexAn(cmdLine.get("--src-file-name")); SynAn synan = new SynAn(lexan)) {
					ast = synan.parser();
				}
				if (cmdLine.get("--target-phase").equals("synan"))
					break;

				// Abstract syntax.
				if (cmdLine.get("--target-phase").equals("abstr")) {
					ast.log("");
					break;
				}

				// Semantic analysis.
				try (SemAn seman = new SemAn()) {
					ast.accept(new NameResolver(), null);
					ast.accept(new TypeChecker(), null);
				}
				if (cmdLine.get("--target-phase").equals("seman")) {
					ast.log("");
					break;
				}

				// Memory: accesses and stack frames.
				try (Memory memory = new Memory()) {
					ast.accept(new MemEvaluator(), null);
				}
				if (cmdLine.get("--target-phase").equals("memory")) {
					ast.log("");
					break;
				}

				// Intermediate code generation.
				try (ImcGen imcgen = new ImcGen()) {
					ast.accept(new CodeGenerator(), null);
				}
				if (cmdLine.get("--target-phase").equals("imcgen")) {
					ast.log("");
					break;
				}
				
				// Linearization of intermediate code.
				try (ImcLin imclin = new ImcLin()) {
					ast.accept(new ChunkGenerator(), null);
				}
				if (cmdLine.get("--target-phase").equals("imclin"))
					break;

				// Interpretation of linearized intermediate code.
				if (cmdLine.get("--comp-method").equals("interp")) {
					Interpreter interpreter = new Interpreter(ImcLin.dataChunks(), ImcLin.codeChunks());
					System.out.println("EXIT CODE: " + interpreter.run("_main"));
					break;
				}
				// Compilation of the AST to JVM code.
				if (cmdLine.get("--comp-method").equals("compile")) {
					try (RefAn refan = new RefAn(); BtcGen btcgen = new BtcGen(); BtcEmt btcemt = new BtcEmt()) {
						// Reference analysis.
						ast.accept(new RefEvaluator(), null);

						// Bytecode generation.
						ast.accept(new ClassGenerator(cmdLine.get("--dst-file-name")), null);

						// Bytecode emission.
						CodeConverter codeConverter = new CodeConverter();
						codeConverter.convert();

						CodeEmitter codeEmitter = new CodeEmitter();
						codeEmitter.emit();
					}
					break;
				}
				break;

			}

			Report.info("Done.");
		} catch (

		Report.Error __) {
			System.exit(1);
		}
	}

}
