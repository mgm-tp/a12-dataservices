/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */
package com.mgmtp.a12.query.internal.debug;

import org.jetbrains.annotations.NotNull;
import org.python.util.PythonInterpreter;

import lombok.Getter;

public class PygmentsHighlighter implements AutoCloseable {

	private final PythonInterpreter interpreter = new PythonInterpreter();
	private final Lexer lexer;
	private final Style style;

	public PygmentsHighlighter(Lexer lexer, Style style) {
		this.lexer = lexer;
		this.style = style;
	}

	CharSequence highlight(String code) {
		interpreter.set("code", code);
		interpreter.exec(constructPythonCode(lexer, style.getStyleName()));
		return interpreter.get("result").asString();

	}

	@NotNull private static String constructPythonCode(Lexer lexer, String style) {
		return """
			from pygments import highlight
			from pygments.formatters.terminal256 import Terminal256Formatter
			%s
			from pygments.styles import get_all_styles
			result = highlight(code, %s, Terminal256Formatter(style='%s'))
			""".formatted(lexer.getAnImport(), lexer.getConstructor(), style);
	}

	@Override public void close() {
		interpreter.close();
	}

	public enum Lexer {
		SQL("from pygments.lexers.sql import SqlLexer", "SqlLexer()"),
		JSON("from pygments.lexers.web import JsonLexer", "JsonLexer()"),
		YAML("from pygments.lexers.text import YamlLexer", "YamlLexer()");

		@Getter private final String anImport;
		@Getter private final String constructor;

		Lexer(String anImport, String constructor) {
			this.anImport = anImport;
			this.constructor = constructor;
		}
	}

	/**
	 * See https://pygments.org/styles/
	 */
	public enum Style {
		DEFAULT("default"),
		NATIVE("native"),
		MONOKAI("monokai"),
		PERLDOC("perldoc"),
		PASTIE("pastie"),
		FRIENDLY("friendly"),
		EMACS("emacs"),
		FRUITY("fruity"),
		MURPHY("murphy"),
		BORLAND("borland"),
		BW("bw"),
		RRT("rrt"),
		TRAC("trac"),
		MANNI("manni"),
		TANGO("tango"),
		AUTUMN("autumn"),
		VIM("vim"),
		VS("vs"),
		COLORFUL("colorful");

		private final String styleName;

		Style(String styleName) {
			this.styleName = styleName;
		}

		public String getStyleName() {
			return styleName;
		}
	}
}
