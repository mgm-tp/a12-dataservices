drop procedure if exists create_search_index cascade;
create procedure create_search_index(p_index_name text, p_model_name text, p_expression text, p_comment text, p_schema_name text default 'public', p_index_type text default 'btree')
SECURITY DEFINER
language plpgsql
as
$$
declare
v_sql text;
begin
  v_sql := format('CREATE INDEX %I ON %I.document_search USING %I((%s)) WHERE model_NAME = ''%s'';', p_index_name, p_schema_name, p_index_type, p_expression, p_model_name);
  execute v_sql;
  v_sql := format('COMMENT ON INDEX %I IS ''Conditional index for model name: %s Comment: %s''', p_index_name, p_model_name, p_comment);
  execute v_sql;
end;
$$;

drop procedure if exists create_search_doc_field_index cascade;
create procedure create_search_doc_field_index(p_index_name text, p_model_name text, p_field_path text, p_comment text, p_schema_name text default 'public')
SECURITY DEFINER
language plpgsql
as
$$
declare
v_sql text;
v_paths text[];
begin
  v_paths := string_to_array(ltrim(p_field_path, '/'), '/');
  v_sql := format('CREATE INDEX %I ON %I.document_search USING gin (model_name gin_trgm_ops, (value #>> %L) gin_trgm_ops)  WHERE model_NAME = ''%s''', p_index_name, p_schema_name, v_paths, p_model_name);
execute v_sql;
v_sql := format('COMMENT ON INDEX %I IS ''Conditional index for model name: %s with path: %s Comment: %s''', p_index_name, p_model_name, p_field_path, p_comment);
execute v_sql;
end;
$$;

drop procedure if exists drop_search_index_by_model cascade;
create procedure drop_search_index_by_model(p_model_name text, p_schema_name text default 'public')
SECURITY DEFINER
language plpgsql
as
$$
declare
index_name text;
schema_name text;
begin
FOR index_name, schema_name IN
SELECT
    pgcc.relname,
    pgn.nspname
FROM pg_index pgi
         JOIN pg_class pgc ON pgc.oid = pgi.indrelid
         JOIN pg_class pgcc ON pgcc.oid = pgi.indexrelid
         JOIN pg_namespace pgn ON pgn.oid = pgc.relnamespace and pgn.nspname = p_schema_name
         LEFT JOIN pg_description pgd ON pgd.objoid = pgi.indexrelid
WHERE pgc.relname = 'document_search' AND pgd.description like 'Conditional index for model name: ' || p_model_name ||' %'
    LOOP
        RAISE NOTICE 'Dropping index: %.%', schema_name,index_name;
        EXECUTE 'DROP INDEX IF EXISTS ' || quote_ident(schema_name) || '.' || quote_ident(index_name);
END LOOP;
end;
$$;

drop procedure if exists drop_all_search_index cascade;
create procedure drop_all_search_index(p_schema_name text default 'public')
SECURITY DEFINER
language plpgsql
as
$$
declare
index_name text;
schema_name text;
begin
FOR index_name, schema_name IN
SELECT
    pgcc.relname,
    pgn.nspname
FROM pg_index pgi
         JOIN pg_class pgc ON pgc.oid = pgi.indrelid
         JOIN pg_class pgcc ON pgcc.oid = pgi.indexrelid
         JOIN pg_namespace pgn ON pgn.oid = pgc.relnamespace and pgn.nspname = p_schema_name
         LEFT JOIN pg_description pgd ON pgd.objoid = pgi.indexrelid
WHERE pgc.relname = 'document_search' AND pgd.description like 'Conditional index for model name: %'
    LOOP
        RAISE NOTICE 'Dropping index: %.%', schema_name,index_name;
        EXECUTE 'DROP INDEX IF EXISTS ' || quote_ident(schema_name) || '.' || quote_ident(index_name);
END LOOP;
end;
$$;

drop function if exists get_all_search_indexes_by_model cascade;
CREATE FUNCTION get_all_search_indexes_by_model(p_model_name text, p_schema_name text default 'public')
RETURNS TABLE (id oid, schema_name name, index_name name, description text)
SECURITY DEFINER
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY SELECT
		pgcc.oid as id,
        pgn.nspname as schema_name,
		pgcc.relname as index_name,
		pgd.description
	FROM pg_index pgi
			 JOIN pg_class pgc ON pgc.oid = pgi.indrelid
			 JOIN pg_class pgcc ON pgcc.oid = pgi.indexrelid
			 JOIN pg_namespace pgn ON pgn.oid = pgc.relnamespace and pgn.nspname = p_schema_name
			 LEFT JOIN pg_description pgd ON pgd.objoid = pgi.indexrelid
	WHERE pgc.relname = 'document_search' AND pgd.description like 'Conditional index for model name: ' || p_model_name ||' %';
END;
$$;
