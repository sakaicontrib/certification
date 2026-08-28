CREATE TABLE certificate_award (
    id VARCHAR(36) NOT NULL,
    userId VARCHAR(36) NOT NULL,
    certificate_definition_id VARCHAR(36) NOT NULL,
    award_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT certificate_award_pk PRIMARY KEY (id),
    CONSTRAINT certificate_award_user_uk UNIQUE (certificate_definition_id, userId),
    CONSTRAINT certificate_award_definition_fk FOREIGN KEY (certificate_definition_id)
        REFERENCES certificate_definition (id)
);
