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
package com.mgmtp.a12.dataservices.query.internal;

import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.mgmtp.a12.dataservices.query.topology.QueryTopology;
import com.mgmtp.a12.dataservices.query.ConstraintAware;
import com.mgmtp.a12.dataservices.query.LinkAware;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.NestingOperator;
import com.mgmtp.a12.dataservices.query.constraint.VariadicOperator;
import com.mgmtp.a12.dataservices.query.internal.QueryVisitor.VisitState;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryWalker {
	private final QueryVisitor visitor;
	private final Function<ConstraintAware, String> getTargetDocumentModel;

	public void walk(QueryTopology root) {
		walkTopology(root, null, VisitState.DIVE, new String[] { "$" });
	}

	private VisitState walkTopology(@NonNull QueryTopology topology, String parentDocumentModel, VisitState originalState, @NonNull String[] path) {
		VisitState state = originalState.getHighest(visitor.visitTopology(topology, parentDocumentModel, path));
		state = state.getHighest(walkOperators(topology, getTargetDocumentModel.apply(topology), state, path));
		if (Objects.requireNonNull(state).ordinal() <= VisitState.DIVE.ordinal() && topology.getLinks() != null) {
			String[] linkPath = ArrayUtils.add(path, "links");
			int i = 0;
			for (QueryTopology l : topology.getLinks()) {
				state = state.getHighest(walkTopology(l, getTargetDocumentModel.apply(topology), state,
					ArrayUtils.addAll(linkPath, "[%d]".formatted(i++))));
				if (state == VisitState.HALT) {
					return state;
				} else if (state == VisitState.BREAK) {
					return originalState;
				}
			}
		}
		return state;
	}

	@NonNull private VisitState walkOperators(ConstraintAware root, String targetDocumentModel, VisitState state, String[] path) {
		state = state.getHighest(walkOperator(root.getConstraint(), targetDocumentModel, state, ArrayUtils.add(path, "constraint")));
		if (root instanceof LinkAware queryLink) {
			state = state.getHighest(walkOperator(queryLink.getLinkDocumentConstraint(), targetDocumentModel, state, ArrayUtils.add(path, "linkDocumentConstraint")));
		}
		return state;
	}

	@NonNull private VisitState walkOperator(ILogicOperator c, String parentDocumentModel, VisitState originalState, String[] path) {
		if (c == null) {
			return originalState;
		}
		@NonNull QueryVisitor.VisitState state = originalState.getHighest(visitor.visitOperator(c, parentDocumentModel, path));
		if (state.ordinal() <= VisitState.DIVE.ordinal()) {
			if (c instanceof NestingOperator nestingOperator) {
				state = state.getHighest(walkOperator(nestingOperator.getOperand(), parentDocumentModel, state, ArrayUtils.add(path, "operand")));
			}
			if (c instanceof VariadicOperator variadicOperator && CollectionUtils.isNotEmpty(variadicOperator.getOperands())) {
				return walkVariadicOperator(variadicOperator, parentDocumentModel, originalState, path, state);
			}
			if (c instanceof ConstraintAware constraintAware) {
				walkOperators(constraintAware, getTargetDocumentModel.apply(constraintAware), state, path);
			}
		}
		return state;
	}

	private @NonNull VisitState walkVariadicOperator(VariadicOperator variadicOperator, String parentDocumentModel, VisitState originalState, String[] path,
		VisitState state) {
		String[] operandsPath = ArrayUtils.add(path, "operands");
		int i = 0;
		for (ILogicOperator o : variadicOperator.getOperands()) {
			state = state.getHighest(walkOperator(o, parentDocumentModel, state, ArrayUtils.add(operandsPath, "[%d]".formatted(i++))));
			if (state == VisitState.HALT) {
				return state;
			} else if (state == VisitState.BREAK) {
				return originalState;
			}
		}
		return state;
	}
}
