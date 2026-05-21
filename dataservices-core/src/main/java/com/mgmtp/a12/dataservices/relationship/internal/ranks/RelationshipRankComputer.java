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
package com.mgmtp.a12.dataservices.relationship.internal.ranks;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.mgmtp.a12.dataservices.relationship.spec.LinkPosition.BOTTOM;
import static com.mgmtp.a12.dataservices.relationship.spec.LinkPosition.TOP;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RelationshipRankComputer {

	public static final String ALPHABET = "!#$%&()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_abcdefghijklmnopqrstuvwxyz|~";
	public static final String[] ALPHABET_SARRAY = ALPHABET.chars().mapToObj(c -> (char) c).map(Object::toString).toArray(String[]::new);
	public static final int ALPHABET_SIZE = ALPHABET.length();
	public static final String INITIAL_RANK = "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss";
	public static final int MAX_LENGTH = 255;
	private static final String SAFE_APPEND = "!~";
	public static final char FIRST_CHARACTER = '!';
	public static final char LAST_CHARACTER = '~';
	public static final char MIDDLE_CHARACTER = '_';

	/**
	 * Method  returns next available rank for different combinations of already existing ranks.
	 * If there is no previous and following rank we are initializing the collection
	 * If there is no following rank, we are inserting to the head
	 * otherwise we insert in the middle
	 *
	 * @param range boundaries for searching the rank
	 * @return rank in between previous and following
	 */
	public static String computeRank(@NonNull Pair<String , String> range) throws OutOfRanksException {
		// initial entry
		if (StringUtils.isBlank(range.getLeft()) && StringUtils.isBlank(range.getRight())) {
			return INITIAL_RANK;
		} else if (StringUtils.isBlank(range.getLeft()) && StringUtils.isNotBlank(range.getRight())) {
			// head
			return computePreviousHigherRank(range.getRight());
		}
		// tail (There is no following rank after the previous rank)
		if (StringUtils.isNotBlank(range.getLeft()) && StringUtils.isBlank(range.getRight())) {
			return computeFollowingLowerRank(range.getLeft());
		} else {
			// middle
			String potentialRank = computeFollowingLowerRank(range.getLeft());
			int compared = potentialRank.compareTo(range.getRight());
			if (compared == 0) {
				return range.getLeft() + MIDDLE_CHARACTER;
			} else if (compared < 0) {
				return potentialRank;
			} else {
				if (StringUtils.endsWith(range.getRight(), SAFE_APPEND)) {
					return potentialRank + SAFE_APPEND;
				} else {
					return computePreviousHigherRank(range.getRight());
				}
			}
		}
	}

	public static List<String> spreadEqually(long size) {
		return spreadEquallyInternal(size, ALPHABET_SIZE, 1, 1).toList();
	}

	/**
	 * method is for calculating new head of the list
	 *
	 * @param previousRank that should follow after newly generated previousRank
	 * @return lower previousRank
	 */
	private static String computeFollowingLowerRank(String previousRank) throws OutOfRanksException {
		char lastCharacter = previousRank.charAt(previousRank.length() - 1);
		if (LAST_CHARACTER == lastCharacter) {
			if (previousRank.length() < MAX_LENGTH) {
				return previousRank + FIRST_CHARACTER;
			} else {
				throw new OutOfRanksException(previousRank, BOTTOM);
			}
		} else {
			char newLastCharacter = characterAfter(lastCharacter);
			return StringUtils.substring(previousRank, 0, previousRank.length() - 1) + newLastCharacter;
		}
	}

	private static String computePreviousHigherRank(String previousRank) throws OutOfRanksException {
		char lastCharacter = previousRank.charAt(previousRank.length() - 1);
		if (FIRST_CHARACTER == lastCharacter) {
			if (previousRank.length() > 1) {
				return StringUtils.substring(previousRank, 0, previousRank.length() - 1);
			} else {
				throw new OutOfRanksException(previousRank, TOP);
			}
		} else {
			char newLastCharacter = characterBefore(lastCharacter);
			return StringUtils.substring(previousRank, 0, previousRank.length() - 1) + newLastCharacter;
		}
	}

	private static Stream<String> spreadEquallyInternal(long size, int alphabetSize, int frontBumper, int rearBumper) {
		if (size == 0) {
			return Stream.empty();
		}
		final int reducedAlphabetSize = alphabetSize - frontBumper - rearBumper;
		if (size > reducedAlphabetSize) {
			long perBucket = size / reducedAlphabetSize;
			long reminder = size % reducedAlphabetSize;
			final List<String> spread = spreadEquallyInternal(perBucket, alphabetSize, 0, 0)
				.toList();
			final List<String> spreadMiddle = reminder == 0 ? spread
				: spreadEquallyInternal(reminder + perBucket, alphabetSize, 0, 0)
				.toList();
			final int alphabetMiddle = alphabetSize / 2;
			return IntStream.range(0, alphabetSize)
				.skip(frontBumper)
				.limit(reducedAlphabetSize)
				.boxed()
				.flatMap(a -> (a == alphabetMiddle ? spreadMiddle : spread).stream()
					.map(s -> ALPHABET_SARRAY[a].concat(s)));
		} else {
			int step = (int) ((reducedAlphabetSize) / size);
			return IntStream.iterate(0, i -> i + step)
				.skip(frontBumper)
				.limit(size)
				.mapToObj(x -> ALPHABET_SARRAY[x]);
		}
	}

	private static char characterBefore(char character) {
		return ALPHABET.charAt(StringUtils.indexOf(ALPHABET, character) - 1);
	}

	private static char characterAfter(char character) {
		return ALPHABET.charAt(StringUtils.indexOf(ALPHABET, character) + 1);
	}
}
