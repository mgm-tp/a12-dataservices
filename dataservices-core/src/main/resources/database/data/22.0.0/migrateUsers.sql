UPDATE DOCUMENT
   SET XML_DOC = REPLACE(XML_DOC, '<DomainAccessRight>', CONCAT(CONCAT('<DomainAccessRight id="', ID),'">')) WHERE MODEL_NAME = 'DomainAccessRight' AND XML_DOC not like '<DomainAccessRight id="%';

UPDATE DOCUMENT
   SET XML_DOC = REPLACE(XML_DOC, '<DomainRole>', CONCAT(CONCAT('<DomainRole id="', ID),'">')) WHERE MODEL_NAME = 'DomainRole' AND XML_DOC not like '<DomainRole id="%';

UPDATE DOCUMENT
   SET XML_DOC = REPLACE(XML_DOC, '<DomainUser>', CONCAT(CONCAT('<DomainUser id="', ID),'">')) WHERE MODEL_NAME = 'DomainUser' AND XML_DOC not like '<DomainUser id="%';