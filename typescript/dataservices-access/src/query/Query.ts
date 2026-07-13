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
import type { RestRequestPayload } from "@com.mgmtp.a12.utils/utils-connector";

import { isNumber, isObject, isString } from "../common/TypeGuardUtils.js";

/**
 * Namespace for aggregation query operations.
 *
 * Aggregation queries allow computing aggregate values (count, sum, avg, min, max)
 * grouped by specified fields.
 *
 * == Validation Rules
 *
 * The backend AggregationController enforces the following rules:
 *
 * *Required:*
 *
 * - `aggregation` - must be present with at least one aggregation function
 * - `targetDocumentModel` - the document model to query
 *
 * *Forbidden (will cause validation error):*
 *
 * - `links` - not allowed for aggregation queries
 * - `exclude: true` - aggregation queries must not exclude
 * - `backReference` - not allowed for aggregation queries
 *
 * *Auto-populated by backend (client values are overwritten):*
 *
 * - `projectionName` - always set to "document"
 * - `paging` - set from server configuration
 *
 */
export namespace Aggregation {
	/**
	 * Request structure for aggregation queries.
	 *
	 * The `queryRoot` should contain:
	 *
	 * - `targetDocumentModel` - the document model to aggregate
	 * - `aggregation` - the aggregation specification with group fields and functions
	 * - `constraint` (optional) - filter criteria for documents to aggregate
	 *
	 * Do NOT include `links`, `exclude: true`, or `backReference` as these will
	 * cause validation errors. The `projectionName` and `paging` fields are
	 * automatically set by the backend and any client-provided values are ignored.
	 */
	export interface Request {
		readonly queryRoot: Query.QueryRoot;
	}

	export namespace Request {
		/**
		 * Builds a REST request payload for aggregation queries.
		 *
		 * The request body is JSON-stringified to ensure proper content type handling
		 * by the backend AggregationController which expects `application/json`.
		 *
		 * Note: The backend automatically sets `projectionName` and `paging` fields,
		 * so these should NOT be included in the queryRoot sent by the client.
		 *
		 * @param request the aggregation request containing the query root
		 * @return a REST request payload ready for dispatch
		 * @see AggregationController#loadAggregations
		 */
		export function build(request: Request): RestRequestPayload {
			return {
				method: "POST",
				relativeUrl: `/aggregation`,
				body: JSON.stringify(request.queryRoot),
				customHeaders: [
					["Accept", "application/json"],
					["Content-Type", "application/json;charset=utf8"]
				]
			};
		}
	}

	export type AggregationTuple = (string | number)[];

	export namespace Response {
		export function isInstance(value: unknown): value is AggregationTuple[] {
			return (
				Array.isArray(value) &&
				value.every(elm => {
					if (!Array.isArray(elm) || elm.length === 0) {
						return false;
					}
					const firstNumberIndex = elm.findIndex(item => isNumber(item));

					// Must contain at least one number
					if (firstNumberIndex === -1) {
						return false;
					}

					// All elements before the first number must be strings
					for (let i = 0; i < firstNumberIndex; i++) {
						// Allow null in string positions
						if (elm[i] !== null && !isString(elm[i])) {
							return false;
						}
					}

					// All elements from the first number onwards must be numbers
					for (let i = firstNumberIndex; i < elm.length; i++) {
						if (!isNumber(elm[i])) {
							return false;
						}
					}

					return true;
				})
			);
		}
	}
}

/**
 * @module Query/api
 * @description Defines the Query namespace containing interfaces and types for constructing and handling complex queries.
 */
export namespace Query {
	/**
	 * Base query structure.
	 */
	export interface QueryTopology {
		readonly fields?: string[];
		readonly aggregation?: Query.AggregationProjector;
		readonly constraint?: Query.Operator;
		readonly links?: QueryLink[];
	}

	/**
	 * Root query object.
	 */
	export interface QueryRoot extends QueryTopology {
		readonly targetDocumentModel: string;
		readonly projectionName: "document" | "document-graph" | "cdd" | "exportCddCsv";
		readonly paging: Query.Paging;
		readonly sort?: Query.Order[];
		readonly exclude?: boolean;
	}

	export namespace QueryRoot {
		export function isInstance(obj: unknown): obj is QueryRoot {
			return (
				isObject(obj) &&
				"targetDocumentModel" in obj &&
				isString(obj.targetDocumentModel) &&
				"projectionName" in obj &&
				isString(obj.projectionName) &&
				"paging" in obj &&
				Paging.isInstance(obj.paging)
			);
		}
	}

	/**
	 * Definition of a query link.
	 */
	export interface QueryLink extends QueryTopology {
		readonly relationshipModel: string;
		readonly targetRole: string;
		readonly linkDocumentConstraint?: Query.Operator;
		readonly linkDocumentFields?: string[];
		readonly maxDepth?: number;
		readonly backReference?: string;
	}

	export namespace QueryLink {
		export function isInstance(obj: unknown): obj is QueryLink {
			return (
				isObject(obj) &&
				"relationshipModel" in obj &&
				isString(obj.relationshipModel) &&
				"targetRole" in obj &&
				isString(obj.targetRole)
			);
		}
	}

	/**
	 * Aggregated type for all Logic and Search operators.
	 */
	export type Operator =
		| AndOperator
		| OrOperator
		| NotOperator
		| HasOperator
		| DateRangeOperator
		| DateFragmentRangeOperator
		| DoubleRangeOperator
		| ExactMatchOperator
		| DocRefExactMatchOperator
		| UndefinedMatchOperator
		| SimpleSearchOperator;

	export namespace Operator {
		/**
		 * Checks if an object is an instance of Operator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is an Operator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is Operator {
			return (
				LogicOperator.isInstance(obj) &&
				(Object.values(OPERATORS) as string[]).includes(obj.operator)
			);
		}
	}

	/**
	 * Constants for all supported Query Operators (Logic and Search).
	 */
	export const OPERATORS = {
		AND_OPERATOR: "and",
		OR_OPERATOR: "or",
		NOT_OPERATOR: "not",
		HAS_OPERATOR: "has",
		DATE_RANGE_OPERATOR: "date_range",
		DATE_FRAGMENT_RANGE_OPERATOR: "datefragment_range",
		DOUBLE_RANGE_OPERATOR: "double_range",
		EXACT_MATCH_OPERATOR: "exact_match",
		UNDEFINED_MATCH_OPERATOR: "undefined_match",
		SIMPLE_SEARCH_OPERATOR: "simple_search"
	} as const;

	/**
	 * Constants for all supported Query Aggregation Functions.
	 */
	export const AGGREGATIONS = {
		AVG_FUNCTION: "avg",
		COUNT_FUNCTION: "count",
		MAX_FUNCTION: "max",
		MIN_FUNCTION: "min",
		SUM_FUNCTION: "sum"
	} as const;

	/**
	 * Represents a projector for aggregation queries.
	 * @important This feature is not supported yet.
	 * @interface AggregationProjector
	 */
	export interface AggregationProjector {
		/**
		 * Fields to group by.
		 * @readonly
		 */
		readonly group?: ProjectionField[];
		/**
		 * Aggregation functions to apply.
		 * @important This feature is not supported yet.
		 * @readonly
		 */
		readonly aggregations?: AggregationFunction[];
	}

	/**
	 * Represents a field in a projection.
	 * @important This feature is not supported yet.
	 * @interface ProjectionField
	 */
	export interface ProjectionField {
		/**
		 * The name of the field.
		 * @readonly
		 */
		readonly field: string;
		/**
		 * An alias for the field.
		 * @readonly
		 */
		readonly alias: string;
	}

	/**
	 * Aggregated type for all Aggregation Functions.
	 */
	export type Aggregation =
		| CountAggregationFunction
		| AvgAggregationFunction
		| MaxAggregationFunction
		| MinAggregationFunction
		| SumAggregationFunction;

	export namespace Aggregation {
		/**
		 * Checks if an object is an instance of known AggregationFunction.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a AggregationFunction, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is Aggregation {
			return (
				isObject(obj) &&
				"function" in obj &&
				"field" in obj &&
				isString(obj.function) &&
				isString(obj.field)
			);
		}
	}

	/**
	 * Defines an aggregation function.
	 * @important This feature is not supported yet.
	 * @interface AggregationFunction
	 */
	export interface AggregationFunction {
		/**
		 * The name of the aggregation function.
		 * @readonly
		 */
		readonly function: string;
		/**
		 * The field to apply the function to.
		 * @readonly
		 */
		readonly field: string;
		/**
		 * An alias for the result.
		 * @readonly
		 */
		readonly alias?: string;
	}

	export namespace AggregationFunction {
		/**
		 * Checks if an object is an instance of known AggregationFunction.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a AggregationFunction, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is AggregationFunction {
			return (
				isObject(obj) &&
				"function" in obj &&
				isString(obj.function) &&
				"field" in obj &&
				isString(obj.field)
			);
		}
	}

	/**
	 * returns the average value of a numerical field.
	 * @important This feature is not supported yet.
	 */
	export interface AvgAggregationFunction extends AggregationFunction {
		readonly function: typeof AGGREGATIONS.AVG_FUNCTION;
	}

	export namespace AvgAggregationFunction {
		/**
		 * Checks if an object is an instance of AvgAggregationFunction.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a AvgAggregationFunction, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is AvgAggregationFunction {
			return Aggregation.isInstance(obj) && obj.function === AGGREGATIONS.AVG_FUNCTION;
		}
	}

	/**
	 * Returns the number of rows in a set values of field.
	 * @important This feature is not supported yet.
	 */
	export interface CountAggregationFunction extends AggregationFunction {
		readonly function: typeof AGGREGATIONS.COUNT_FUNCTION;
	}

	export namespace CountAggregationFunction {
		/**
		 * Checks if an object is an instance of CountAggregationFunction.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a CountAggregationFunction, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is CountAggregationFunction {
			return Aggregation.isInstance(obj) && obj.function === AGGREGATIONS.COUNT_FUNCTION;
		}
	}

	/**
	 * Returns the largest value within the selected field.
	 * @important This feature is not supported yet.
	 */
	export interface MaxAggregationFunction extends AggregationFunction {
		readonly function: typeof AGGREGATIONS.MAX_FUNCTION;
	}

	export namespace MaxAggregationFunction {
		/**
		 * Checks if an object is an instance of MaxAggregationFunction.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a MaxAggregationFunction, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is MaxAggregationFunction {
			return Aggregation.isInstance(obj) && obj.function === AGGREGATIONS.MAX_FUNCTION;
		}
	}

	/**
	 * Returns the smallest value within the selected field.
	 * @important This feature is not supported yet.
	 */
	export interface MinAggregationFunction extends AggregationFunction {
		readonly function: typeof AGGREGATIONS.MIN_FUNCTION;
	}

	export namespace MinAggregationFunction {
		/**
		 * Checks if an object is an instance of MinAggregationFunction.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a MinAggregationFunction, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is MinAggregationFunction {
			return Aggregation.isInstance(obj) && obj.function === AGGREGATIONS.MIN_FUNCTION;
		}
	}

	/**
	 * Returns the total sum of a numerical field.
	 * @important This feature is not supported yet.
	 */
	export interface SumAggregationFunction extends AggregationFunction {
		readonly function: typeof AGGREGATIONS.SUM_FUNCTION;
	}

	export namespace SumAggregationFunction {
		/**
		 * Checks if an object is an instance of SumAggregationFunction.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a SumAggregationFunction, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is SumAggregationFunction {
			return Aggregation.isInstance(obj) && obj.function === AGGREGATIONS.SUM_FUNCTION;
		}
	}

	/**
	 * Base interface for logic operators (including search operators).
	 * @interface LogicOperator
	 */
	export interface LogicOperator {
		/**
		 * The name of logic operator.
		 * @readonly
		 */
		readonly operator: string;
	}

	export namespace LogicOperator {
		/**
		 * Checks if an object is an instance of known LogicOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a LogicOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is LogicOperator {
			return isObject(obj) && "operator" in obj && isString(obj.operator);
		}
	}

	/**
	 * Represents an AND logic operator.
	 * @interface AndOperator
	 * @extends {LogicOperator}
	 */
	export interface AndOperator extends LogicOperator {
		readonly operator: typeof OPERATORS.AND_OPERATOR;
		/**
		 * The collection of operands to be evaluated. Result will be true if and only if all the operands are true.
		 * @readonly
		 */
		readonly operands: Operator[];
	}

	export namespace AndOperator {
		/**
		 * Checks if an object is an instance of AndOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a AndOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is AndOperator {
			return Query.LogicOperator.isInstance(obj) && obj.operator === OPERATORS.AND_OPERATOR;
		}
	}

	/**
	 * Represents an OR logic operator.
	 * @interface OrOperator
	 * @extends {LogicOperator}
	 */
	export interface OrOperator extends LogicOperator {
		readonly operator: typeof OPERATORS.OR_OPERATOR;
		/**
		 * The collection of operands to be evaluated. Result will be true if and only if one or more of its operands is true.
		 * @readonly
		 */
		readonly operands: Operator[];
	}

	export namespace OrOperator {
		/**
		 * Checks if an object is an instance of OrOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a OrOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is OrOperator {
			return Query.LogicOperator.isInstance(obj) && obj.operator === OPERATORS.OR_OPERATOR;
		}
	}

	/**
	 * Represents a NOT logic operator.
	 * @interface NotOperator
	 * @extends {LogicOperator}
	 */
	export interface NotOperator extends LogicOperator {
		readonly operator: typeof OPERATORS.NOT_OPERATOR;
		/**
		 * The logic operator takes truth to falsity and vice versa.
		 * @readonly
		 */
		readonly operand: Operator;
	}

	export namespace NotOperator {
		/**
		 * Checks if an object is an instance of NotOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a NotOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is NotOperator {
			return Query.LogicOperator.isInstance(obj) && obj.operator === OPERATORS.NOT_OPERATOR;
		}
	}

	/**
	 * Represents a HAS logic operator.
	 * Operator for querying linked documents.
	 *
	 * @interface HasOperator
	 * @extends {LogicOperator}
	 */
	export interface HasOperator extends LogicOperator {
		readonly operator: typeof OPERATORS.HAS_OPERATOR;
		/**
		 * Name of the relationship model.
		 * @readonly
		 */
		readonly relationshipModel: string;
		/**
		 * Target document role.
		 * @readonly
		 */
		readonly targetRole: string;
		/**
		 * Constraint applied on target document.
		 * @readonly
		 */
		readonly constraint?: Operator;
		/**
		 * Constraint applied on link document.
		 * @readonly
		 */
		readonly linkDocumentConstraint?: Operator;
		/**
		 * Maximum depth of nesting.
		 * @readonly
		 */
		readonly maxDepth?: number;
	}

	export namespace HasOperator {
		/**
		 * Checks if an object is an instance of HasOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a HasOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is HasOperator {
			return Query.LogicOperator.isInstance(obj) && obj.operator === OPERATORS.HAS_OPERATOR;
		}
	}

	/**
	 * Represents a DATE_FRAGMENT_RANGE logic operator.
	 * Any date formats that are defined by `Kernel` are supported.
	 * - Both `from` and `to` are not mandatory but at least one is mandatory.
	 * - Boundaries are included in result.
	 * @interface DateFragmentRangeOperator
	 * @extends {LogicOperator}
	 */
	export interface DateFragmentRangeOperator extends LogicOperator {
		readonly operator: typeof OPERATORS.DATE_FRAGMENT_RANGE_OPERATOR;
		/**
		 * Name of date fragment field to be evaluated.
		 * @readonly
		 */
		readonly field: string;
		/**
		 * The starting date of range.
		 * @readonly
		 */
		readonly from?: string;
		/**
		 * The ending date of range.
		 * @readonly
		 */
		readonly to?: string;
	}

	export namespace DateFragmentRangeOperator {
		/**
		 * Checks if an object is an instance of DateFragmentRangeOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a DateFragmentRangeOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is DateFragmentRangeOperator {
			return (
				Query.LogicOperator.isInstance(obj) &&
				obj.operator === OPERATORS.DATE_FRAGMENT_RANGE_OPERATOR
			);
		}
	}

	/**
	 * Represents a DATE_RANGE logic operator.
	 * Any date formats that are defined by `Kernel` are supported.
	 * - Both `from` and `to` are not mandatory but at least one is mandatory.
	 * - Boundaries are included in result.
	 * @interface DateRangeOperator
	 * @extends {LogicOperator}
	 */
	export interface DateRangeOperator extends LogicOperator {
		readonly operator: typeof OPERATORS.DATE_RANGE_OPERATOR;
		/**
		 * Name of date field to be evaluated.
		 * @readonly
		 */
		readonly field: string;
		/**
		 * The starting date of range.
		 * @readonly
		 */
		readonly from?: string;
		/**
		 * The ending date of range.
		 * @readonly
		 */
		readonly to?: string;

		/**
		 * Date value used for reverse range
		 * @readonly
		 */
		readonly value?: string;

		/**
		 * If true, checks if the specified date or date range is within the range stored in the database.
		 * If false (default), checks if the database value is within the specified date range.
		 * @readonly
		 */
		readonly reverse?: boolean;
	}

	export namespace DateRangeOperator {
		/**
		 * Checks if an object is an instance of DateRangeOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a DateRangeOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is DateRangeOperator {
			return Query.LogicOperator.isInstance(obj) && obj.operator === OPERATORS.DATE_RANGE_OPERATOR;
		}
	}

	/**
	 * Represents a DOUBLE RANGE logic operator.
	 * Matches if the value in the field is within a range defined in the operator.
	 * - Both `from` and `to` are not mandatory but at least one is mandatory.
	 * - Boundaries are included in result.
	 * @interface DoubleRangeOperator
	 * @extends {LogicOperator}
	 */
	export interface DoubleRangeOperator extends LogicOperator {
		readonly operator: typeof OPERATORS.DOUBLE_RANGE_OPERATOR;
		/**
		 * Name of numeric field to be evaluated.
		 * @readonly
		 */
		readonly field: string;
		/**
		 * The starting number of range.
		 * @readonly
		 */
		readonly from?: number;
		/**
		 * The ending number of range.
		 * @readonly
		 */
		readonly to?: number;
	}

	export namespace DoubleRangeOperator {
		/**
		 * Checks if an object is an instance of DoubleRangeOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a DoubleRangeOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is DoubleRangeOperator {
			return (
				Query.LogicOperator.isInstance(obj) && obj.operator === OPERATORS.DOUBLE_RANGE_OPERATOR
			);
		}
	}

	/**
	 * Represents an EXACT MATCH search operator.
	 * Returns all results that have fields with value that matches exactly the input value.
	 * This operator can be applied on types:
	 * - StringType
	 * - DateType
	 * - DateTimeType
	 * - DateFragmentType
	 * - NumberType
	 * - EnumerationType
	 * - BooleanType
	 * - ConfirmType
	 * @interface ExactMatchOperator
	 * @extends {LogicOperator}
	 */
	export interface ExactMatchOperator extends LogicOperator {
		readonly operator: typeof OPERATORS.EXACT_MATCH_OPERATOR;
		/**
		 * Name of field to be evaluated.
		 * @readonly
		 */
		readonly field: string;
		/**
		 * Value for comparison with field's value.
		 * For matching with number value please use numeric value here, for other types please use `string`.
		 * Either this or `values` must be provided.
		 * @readonly
		 */
		readonly value?: number | string;
		/**
		 * Multiple values to be compared with the field's value.
		 *
		 * This is an alternative to the `value` parameter, allowing multiple values to be matched simultaneously.
		 * The behavior is similar to `value`, but it matches documents where the field equals any of the specified
		 * values (logical OR). Either this or `value` must be provided.
		 * @readonly
		 */
		readonly values?: (number | string)[];
		/**
		 * Enable to match exact case-sensitive value.
		 * @default true
		 * @readonly
		 */
		readonly caseSensitive?: boolean;
	}

	export namespace ExactMatchOperator {
		/**
		 * Checks if an object is an instance of ExactMatchOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a ExactMatchOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is ExactMatchOperator {
			return Query.LogicOperator.isInstance(obj) && obj.operator === OPERATORS.EXACT_MATCH_OPERATOR;
		}
	}

	/**
	 * Similar to an ExactMatchOperator but exclusively set field to `/__meta/docRef`.
	 * @interface DocRefExactMatchOperator
	 * @extends {LogicOperator}
	 */
	export interface DocRefExactMatchOperator extends ExactMatchOperator {
		/**
		 * Name of field to be evaluated.
		 * @readonly
		 */
		readonly field: "/__meta/docRef";
	}

	export namespace DocRefExactMatchOperator {
		/**
		 * Checks if an object is an instance of DocRefExactMatchOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a DocRefExactMatchOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is DocRefExactMatchOperator {
			return Query.ExactMatchOperator.isInstance(obj) && obj.field === "/__meta/docRef";
		}
	}

	/**
	 * Represents the UNDEFINED_MATCH operator that checks if a given field has value `null` or if it is not present.
	 * @interface UndefinedMatchOperator
	 * @extends {LogicOperator}
	 */
	export interface UndefinedMatchOperator extends LogicOperator {
		readonly operator: typeof OPERATORS.UNDEFINED_MATCH_OPERATOR;
		/**
		 * Name of field to be evaluated.
		 * @readonly
		 */
		readonly field: string;
	}

	export namespace UndefinedMatchOperator {
		/**
		 * Checks if an object is an instance of UndefinedMatchOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a UndefinedMatchOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is UndefinedMatchOperator {
			return (
				Query.LogicOperator.isInstance(obj) && obj.operator === OPERATORS.UNDEFINED_MATCH_OPERATOR
			);
		}
	}

	export enum TermJoinType {
		AND = "AND",
		OR = "OR"
	}

	/**
	 * Represents a SIMPLE SEARCH search operator.
	 * Return all documents which have fields with a value that contains the input string.
	 * This operator can be applied on types:
	 * - StringType
	 * @interface SimpleSearchOperator
	 * @extends {LogicOperator}
	 */
	export interface SimpleSearchOperator extends LogicOperator {
		readonly operator: typeof OPERATORS.SIMPLE_SEARCH_OPERATOR;
		/**
		 * List of fields to be evaluated.
		 * If no fields are specified, the search ranges over all fields of the document.
		 * @readonly
		 */
		readonly fields?: string[];
		/**
		 * Value for comparison with field values.
		 * @readonly
		 */
		readonly value: string;

		/**
		 * Multiple values to be compared with all fields.
		 *
		 * Providing a list of values is faster than combining
		 * multiple conditions on the same field with an OR condition.
		 * @readonly
		 */
		readonly values?: string[];
	}

	export namespace SimpleSearchOperator {
		/**
		 * Checks if an object is an instance of SimpleSearchOperator.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a SimpleSearchOperator, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is SimpleSearchOperator {
			return (
				Query.LogicOperator.isInstance(obj) && obj.operator === OPERATORS.SIMPLE_SEARCH_OPERATOR
			);
		}
	}

	/**
	 * Represents the result of a document tree query.
	 * @interface DocumentTreeResult
	 */
	export interface DocumentTreeResult {
		/**
		 * Document reference of the document.
		 */
		readonly docRef: string;
		/**
		 * Relationship link model if linked.
		 */
		readonly relationshipModel?: string;
		/**
		 * Source role of the link if linked.
		 */
		readonly sourceRole?: string;
		/**
		 * Source document reference if linked.
		 */
		readonly sourceDocRef?: string;
		/**
		 * Target role of the link if linked.
		 */
		readonly targetRole?: string;
		/**
		 * Target document reference if linked.
		 */
		readonly targetDocRef?: string;
		/**
		 * Content of the document.
		 */
		readonly document: any;
		/**
		 * Type of this node.
		 */
		readonly type: DocumentTreeNodeType;
		/**
		 * ID of the relationship link.
		 */
		readonly linkId?: number;
		/**
		 * Document model name of the document.
		 */
		readonly documentModelName?: string;
	}

	export namespace DocumentTreeResult {
		/**
		 * Checks if an object is an instance of DocumentTreeResult.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a DocumentTreeResult, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is DocumentTreeResult {
			return (
				isObject(obj) &&
				"docRef" in obj &&
				isString(obj.docRef) &&
				"type" in obj &&
				DocumentTreeNodeType.isInstance(obj.type) &&
				"document" in obj
			);
		}
	}

	/**
	 * Enum representing the type of node in a document tree.
	 * - ROOT type is set to all documents that are matched will be returned in the result.
	 * - CHILD documents are additionally loaded via CDM or links parameters via relationship links.
	 * - LINK type is reserved for link documents CHILD nodes.
	 * @enum {string}
	 */
	export enum DocumentTreeNodeType {
		ROOT = "ROOT",
		CHILD = "CHILD",
		LINK = "LINK"
	}

	export namespace DocumentTreeNodeType {
		/**
		 * Checks if a value is a valid DocumentTreeNodeType enum value.
		 * @param value - The value to check.
		 * @returns {boolean} True if the value is a valid DocumentTreeNodeType, false otherwise.
		 */
		export function isInstance(value: unknown): value is DocumentTreeNodeType {
			return Object.values(DocumentTreeNodeType).includes(value as DocumentTreeNodeType);
		}
	}

	/**
	 * Interface for pagination configuration.
	 * Paging is mandatory for every Query operation.
	 * @interface Paging
	 */
	export interface Paging {
		/**
		 * The zero-based number of the specific page, indicate the proper order of the pages.
		 * @important This number must be lower than `com.mgmtp.a12.dataservices.query.pageRequest.pageNumberLimit` defined in Dataservices configuration.
		 * @readonly
		 */
		readonly pageNumber: number;
		/**
		 * Maximum amount of documents in 1 page.
		 * @important This number must be lower than `com.mgmtp.a12.dataservices.query.pageRequest.pageSizeLimit` defined in Dataservices configuration.
		 * @readonly
		 */
		readonly pageSize: number;
	}

	export namespace Paging {
		/**
		 * Checks if an object is an instance of Paging.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a Paging, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is Paging {
			return (
				isObject(obj) &&
				"pageNumber" in obj &&
				isNumber(obj.pageNumber) &&
				"pageSize" in obj &&
				isNumber(obj.pageSize)
			);
		}
	}

	/**
	 * Interface for direct field ordering configuration.
	 * Allows sorting by a field on the current (or related) document.
	 * @interface DirectFieldOrder
	 */
	export interface DirectFieldOrder {
		/**
		 * The document field to sort by.
		 * @readonly
		 */
		readonly field: string;
		/**
		 * Direction of ordering.
		 * @readonly
		 */
		readonly direction: Direction;
		/**
		 * True if sorting should be case-insensitive; false if case-sensitive.
		 * @readonly
		 */
		readonly ignoreCase?: boolean;
		/**
		 * Null handling strategy.
		 * @readonly
		 */
		readonly nullHandling?: NullHandling;
	}

	export namespace DirectFieldOrder {
		/**
		 * Checks if an object is an instance of DirectFieldOrder.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a DirectFieldOrder, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is DirectFieldOrder {
			return (
				isObject(obj) &&
				"field" in obj &&
				isString(obj.field) &&
				"direction" in obj &&
				Direction.isInstance(obj.direction) &&
				"ignoreCase" in obj &&
				typeof obj.ignoreCase === "boolean" &&
				"nullHandling" in obj &&
				NullHandling.isInstance(obj.nullHandling)
			);
		}
	}

	/**
	 * Interface for relationship-based ordering configuration.
	 * Allows sorting by fields of related documents through to-1 relationships.
	 * @interface RelationshipOrder
	 */
	export interface RelationshipOrder {
		/**
		 * Name of the relationship model to traverse.
		 * @readonly
		 */
		readonly relationshipModel: string;
		/**
		 * Target role in the relationship.
		 * @readonly
		 */
		readonly targetRole: string;
		/**
		 * Nested sort specification (either another RelationshipOrder for multi-hop traversal
		 * or a DirectFieldOrder for the terminal field).
		 * @readonly
		 */
		readonly sortBy: Order;
	}

	export namespace RelationshipOrder {
		/**
		 * Checks if an object is an instance of RelationshipOrder.
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a RelationshipOrder, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is RelationshipOrder {
			return (
				isObject(obj) &&
				"relationshipModel" in obj &&
				isString(obj.relationshipModel) &&
				"targetRole" in obj &&
				isString(obj.targetRole) &&
				"sortBy" in obj &&
				Order.isInstance(obj.sortBy)
			);
		}
	}

	/**
	 * Type for ordering configuration (union of direct field and relationship-based ordering).
	 * @type Order
	 */
	export type Order = DirectFieldOrder | RelationshipOrder;

	export namespace Order {
		/**
		 * Checks if an object is an instance of Order (either DirectFieldOrder or RelationshipOrder).
		 * @param obj - The object to check.
		 * @returns {boolean} True if the object is a valid Order, false otherwise.
		 */
		export function isInstance(obj: unknown): obj is Order {
			return DirectFieldOrder.isInstance(obj) || RelationshipOrder.isInstance(obj);
		}
	}

	/**
	 * Enum representing the direction of ordering.
	 * @enum {string}
	 */
	export enum Direction {
		/**
		 * Ascending order.
		 */
		ASC = "ASC",
		/**
		 * Descending order.
		 */
		DESC = "DESC"
	}

	export namespace Direction {
		/**
		 * Checks if a value is a valid Direction enum value.
		 * @param value - The value to check.
		 * @returns {boolean} True if the value is a valid Direction, false otherwise.
		 */
		export function isInstance(value: unknown): value is Direction {
			return Object.values(Direction).includes(value as Direction);
		}
	}

	/**
	 * Enum representing how null values should be handled in ordering.
	 * @enum {string}
	 */
	export enum NullHandling {
		/**
		 * A hint to the used data store to order entries with null values before non null entries.
		 */
		NULLS_FIRST = "NULLS_FIRST",
		/**
		 * A hint to the used data store to order entries with null values after non null entries.
		 */
		NULLS_LAST = "NULLS_LAST"
	}

	export namespace NullHandling {
		/**
		 * Checks if a value is a valid NullHandling enum value.
		 * @param value - The value to check.
		 * @returns {boolean} True if the value is a valid NullHandling, false otherwise.
		 */
		export function isInstance(value: unknown): value is NullHandling {
			return Object.values(NullHandling).includes(value as NullHandling);
		}
	}

	// === Convenience typings
	// should only be added when there are specific needs for it

	/**
	 * Narrows down the `Query.QueryRoot` typing for exact match queries against docRefs.
	 */
	export interface DocRefExactMatchQueryRoot extends Omit<Query.QueryRoot, "aggregation"> {
		readonly constraint: Query.DocRefExactMatchOperator;
		readonly projectionName: "document";
	}

	/**
	 * Narrows down the `Query.QueryRoot` typing for export queries.
	 */
	export interface ExportCddQueryRoot extends Omit<Query.QueryRoot, "aggregation"> {
		readonly projectionName: "exportCddCsv";
	}

	/**
	 * Narrows down the `Query.QueryRoot` typing for queries that load document entries.
	 */
	export interface LoadDocumentEntriesQueryRoot extends Omit<Query.QueryRoot, "aggregation"> {
		readonly projectionName: "cdd" | "document";
	}

	/**
	 * Narrows down the `Query.QueryRoot` typing for aggregation queries.
	 */
	export interface LoadAggregationEntriesQueryRoot extends Query.QueryRoot {
		readonly projectionName: "cdd" | "document";
		readonly aggregation: NonNullable<Query.QueryRoot["aggregation"]>;
	}
}
