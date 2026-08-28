ALTER TABLE certificate_award
    ADD CONSTRAINT certificate_award_user_uk
    UNIQUE (certificate_definition_id, userId);
