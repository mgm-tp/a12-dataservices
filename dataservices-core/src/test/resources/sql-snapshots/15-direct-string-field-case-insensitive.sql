-- {
--   "targetDocumentModel": "Contract",
--   "sort": [
--     {
--       "field": "/ContractRoot/name",
--       "direction": "ASC",
--       "ignoreCase": true,
--       "nullHandling": "NULLS_LAST"
--     }
--   ]
-- }
-- field type: IStringType, ignoreCase=true  →  #>> without COLLATE "C"; no JOIN, ORDER BY in CTE
-- params: p0=type, p1=internal_id
WITH "Contract_0001" AS (SELECT *
  FROM (SELECT *, doc_ref AS result_doc_ref,  COUNT(*) OVER() AS total_count
    FROM document_search AS target_document
    WHERE target_document.model_name IN ('Contract')) AS cte_root
  ORDER BY
    cte_root.original_value #>> '{"ContractRoot","name"}'  NULLS LAST
  OFFSET 0 LIMIT 0)SELECT doc_ref :: TEXT AS doc_ref,model_name :: TEXT AS model_name,original_value :: JSONB AS content,'' :: TEXT AS relationship_model,'' :: TEXT AS source_role,'' :: TEXT AS source_docref,'' :: TEXT AS target_role,'' :: TEXT AS target_docref,NULL :: TEXT AS link_document,'' :: TEXT AS link_id,:p0 :: TEXT AS type,'' :: TEXT AS back_reference,:p1 :: TEXT AS internal_id,FALSE :: BOOLEAN AS fields_projection,0 :: NUMERIC AS depth, ROW_NUMBER () OVER ()  AS row_num,total_count :: NUMERIC AS total_count
FROM "Contract_0001" AS roots
