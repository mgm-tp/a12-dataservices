-- Function to create a full-text search string
CREATE OR REPLACE FUNCTION createLocalizedFulltextString()
RETURNS TRIGGER AS $$
BEGIN
    NEW.localized_fulltext_string := array_to_string(tsvector_to_array(to_tsvector('pg_catalog.simple', NEW.localized_value)), ' ');

RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- Trigger to call the function before each insert or update on localized_fields table
CREATE TRIGGER createFulltextTrigger
    BEFORE INSERT OR UPDATE ON localized_fields
                        FOR EACH ROW
                        EXECUTE FUNCTION createLocalizedFulltextString();