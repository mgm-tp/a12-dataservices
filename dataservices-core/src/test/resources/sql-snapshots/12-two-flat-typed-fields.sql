-- {
--   "targetDocumentModel": "Contract",
--   "sort": [
--     {
--       "relationshipField": {
--         "relationshipModel": "ContractBusinessPartner",
--         "targetRole": "Partner",
--         "field": "/BusinessPartnerRoot/StartOfRelationship"
--       },
--       "direction": "ASC",
--       "nullHandling": "NULLS_LAST"
--     },
--     {
--       "relationshipField": {
--         "relationshipModel": "ContractBusinessPartner",
--         "targetRole": "Partner",
--         "field": "/BusinessPartnerRoot/EndOfRelationship"
--       },
--       "direction": "ASC",
--       "nullHandling": "NULLS_LAST"
--     }
--   ]
-- }
-- field types: StartOfRelationship → IDateType (df1.timestamp_value)
--              EndOfRelationship   → IDateTimeType (df2.timestamp_value)
-- Both sort orders are independent; each anchors on cte_root.doc_ref.
-- params: p0..p3=sort1, p4..p7=sort2; p8=type, p9=internal_id
WITH "Contract_0001" AS (SELECT cte_root.*,
  ROW_NUMBER() OVER (ORDER BY
    df1.timestamp_value NULLS LAST ,
    df2.timestamp_value NULLS LAST
  ) AS _sort_rank
  FROM (SELECT *, doc_ref AS result_doc_ref,  COUNT(*) OVER() AS total_count
    FROM document_search AS target_document
    WHERE target_document.model_name IN ('Contract')) AS cte_root
  LEFT JOIN (SELECT source_role.role_docref AS source_docref, target_role.role_docref AS target_docref
    FROM relationship_role AS source_role, relationship_link AS link, relationship_role AS target_role
    WHERE link.relationship_model = :p0
      AND source_role.relationship_id = link.id
      AND link.id = target_role.relationship_id
      AND source_role.role_name = :p1
      AND target_role.role_name = :p2
  ) AS rr1 ON rr1.source_docref = cte_root.doc_ref
  LEFT JOIN document_search AS d1 ON d1.doc_ref = rr1.target_docref
  LEFT JOIN document_fields AS df1 ON df1.doc_ref = d1.doc_ref AND df1.field_name = :p3
  LEFT JOIN (SELECT source_role.role_docref AS source_docref, target_role.role_docref AS target_docref
    FROM relationship_role AS source_role, relationship_link AS link, relationship_role AS target_role
    WHERE link.relationship_model = :p4
      AND source_role.relationship_id = link.id
      AND link.id = target_role.relationship_id
      AND source_role.role_name = :p5
      AND target_role.role_name = :p6
  ) AS rr2 ON rr2.source_docref = cte_root.doc_ref
  LEFT JOIN document_search AS d2 ON d2.doc_ref = rr2.target_docref
  LEFT JOIN document_fields AS df2 ON df2.doc_ref = d2.doc_ref AND df2.field_name = :p7
  ORDER BY _sort_rank OFFSET 0 LIMIT 0)SELECT doc_ref :: TEXT AS doc_ref,model_name :: TEXT AS model_name,original_value :: JSONB AS content,'' :: TEXT AS relationship_model,'' :: TEXT AS source_role,'' :: TEXT AS source_docref,'' :: TEXT AS target_role,'' :: TEXT AS target_docref,NULL :: TEXT AS link_document,'' :: TEXT AS link_id,:p8 :: TEXT AS type,'' :: TEXT AS back_reference,:p9 :: TEXT AS internal_id,FALSE :: BOOLEAN AS fields_projection,0 :: NUMERIC AS depth, ROW_NUMBER () OVER ()  AS row_num,total_count :: NUMERIC AS total_count
FROM "Contract_0001" AS roots
ORDER BY roots._sort_rank
