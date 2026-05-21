CREATE OR REPLACE FUNCTION createFulltextString()
RETURNS TRIGGER AS $$
BEGIN
    -- Check if field_name starts with '/__meta'
    IF NEW.field_name LIKE '/__meta%' THEN
        NEW.fulltext_string := NULL;
ELSE
        NEW.fulltext_string := array_to_string(
            tsvector_to_array(
                to_tsvector('pg_catalog.simple', NEW.value)
            ),
            ' '
        );
END IF;

RETURN NEW;
END
$$ LANGUAGE plpgsql;
