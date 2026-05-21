-- Function to create a full-text search string
CREATE OR REPLACE FUNCTION createFulltextString()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fulltext_string := array_to_string(tsvector_to_array(to_tsvector('pg_catalog.simple', NEW.value)), ' ');

RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- Trigger to call the function before each insert or update on document_fields table
CREATE TRIGGER createFulltextTrigger
    BEFORE INSERT OR UPDATE ON document_fields
                        FOR EACH ROW
                        EXECUTE FUNCTION createFulltextString();