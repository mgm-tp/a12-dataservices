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
import type {
	Localizable,
	LocalizationTreeMap
} from "@com.mgmtp.a12.utils/utils-localization/lib/main/index.js";
import {
	initializeKeys,
	localizableFromLocalizationTreeMap
} from "@com.mgmtp.a12.utils/utils-localization/lib/main/index.js";

/**
 * Attachment errors, that are thrown by the A12 server.
 */
export namespace AttachmentServerError {
	/**
	 * Localization keys of attachment errors thrown by the server.
	 *
	 * These keys are identical to the keys of the corresponding exceptions,
	 * that are thrown by the server.
	 */
	export const LOCALIZATION_KEYS = {
		error: {
			attachment: {
				thumbnail: {
					/** Key of an error if the given attachment is incomplete */
					incompleteInput: "",
					generation: {
						/** Key of an error if the thumbnail could not be generated */
						error: ""
					}
				},
				/** Key of an error if the attachment cannot be found */
				notFound: "",
				/** Key of a general error */
				general: "",
				data: {
					/** Key of an error if the attachment data is corrupted */
					corrupted: ""
				},
				/** Key of an error if the attachment file is empty */
				emptyFile: ""
			},
			security: {
				notAuthorized: {
					/** Key for the error message displayed when the user lacks permission to upload an attachment */
					title: "",
					description: ""
				}
			},
			"content-store": {
				content: {
					/** Key for the error message displayed when attachment size exceeds the maximum allowed size */
					invalidSize: ""
				}
			}
		}
	};

	initializeKeys(LOCALIZATION_KEYS);

	const en: typeof LOCALIZATION_KEYS = {
		error: {
			attachment: {
				thumbnail: {
					incompleteInput: "The entered data is incomplete.",
					generation: {
						error: "The thumbnail cannot be generated."
					}
				},
				notFound: "The selected file cannot be found any longer.",
				general: "The attachment cannot be (un)assigned.",
				data: {
					corrupted: "The attachment data is corrupted."
				},
				emptyFile: "The selected file is empty. Please select a different file."
			},
			security: {
				notAuthorized: {
					title: "The attachment cannot be uploaded due to insufficient permissions.",
					description: "The attachment cannot be uploaded due to insufficient permissions."
				}
			},
			"content-store": {
				content: {
					invalidSize:
						"The attachment cannot be uploaded because it exceeds the maximum allowed size."
				}
			}
		}
	};

	const de: typeof LOCALIZATION_KEYS = {
		error: {
			attachment: {
				thumbnail: {
					incompleteInput: "Die eingegebenen Daten sind unvollständig.",
					generation: {
						error: "Das Vorschaubild kann nicht erzeugt werden."
					}
				},
				notFound: "Die ausgewählte Datei kann nicht mehr gefunden werden.",
				general: "Die Zuweisung des Anhangs kann nicht vorgenommen/aufgehoben werden.",
				data: {
					corrupted: "Die Daten im Anhang sind beschädigt."
				},
				emptyFile: "Die ausgewählte Datei ist leer. Bitte wählen Sie eine andere Datei aus."
			},
			security: {
				notAuthorized: {
					title: "Der Anhang kann aufgrund unzureichender Berechtigungen nicht hochgeladen werden.",
					description:
						"Der Anhang kann aufgrund unzureichender Berechtigungen nicht hochgeladen werden."
				}
			},
			"content-store": {
				content: {
					invalidSize:
						"Die Anlage kann nicht hochgeladen werden, da sie die maximale zulässige Größe überschreitet."
				}
			}
		}
	};

	const errorTexts: LocalizationTreeMap = { en, de };

	/**
	 * Creates a {@link Localizable} for the given localization key.
	 *
	 * For known keys the Localizable contains default translations for english
	 * and german. For custom keys a Localizable without default translations
	 * will be returned.
	 */
	export function createLocalizable(key: string): Localizable {
		return localizableFromLocalizationTreeMap(key, errorTexts);
	}
}
