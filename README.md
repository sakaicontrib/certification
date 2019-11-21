# certification

The master branch is the Sakai 21 version.

20.x branch is for Sakai 20.
19.x branch is for Sakai 19.
12.x branch is for Sakai 12.
11.x branch is for Sakai 11.

Sakai10 Branch is the one needed for Sakai 10.

## Student Numbers

Student numbers are supported in the Report interface, and are provisioned through LDAP using the CandidateDetailProvider in Kernel.

### useInstitutionalNumericID
This is a boolean value, which defaults to false and controls whether student numbers are enabled system wide.

### certification.extraUserProperties.enable
This is a boolean value, whose default is false, which controls whether or not student numbers are enabled.

`certification.extraUserProperties.enable = true`

# Conversion (Users of versions older than 12.0)
Due to the tool refactor, some tables were renamed and some classes were refactored, a conversion script is required to make it work in the 12.x version and newer.

The table **cert_field_mapping** was renamed to **certificate_field_mapping** for consistency reasons:

```
ALTER TABLE cert_field_mapping RENAME TO certificate_field_mapping;
```

Some classes were refactored, update the classes of the **certificate_criterion** table: 

```
UPDATE certificate_criterion SET type = 'org.sakaiproject.certification.api.criteria.gradebook.GreaterThanScoreCriterion' where type = 'com.rsmart.certification.impl.hibernate.criteria.gradebook.GreaterThanScoreCriterionHibernateImpl';

UPDATE certificate_criterion SET type = 'org.sakaiproject.certification.api.criteria.gradebook.WillExpireCriterion' where type = 'com.rsmart.certification.impl.hibernate.criteria.gradebook.WillExpireCriterionHibernateImpl';

UPDATE certificate_criterion SET type = 'org.sakaiproject.certification.api.criteria.gradebook.FinalGradeScoreCriterion' where type = 'com.rsmart.certification.impl.hibernate.criteria.gradebook.FinalGradeScoreCriterionHibernateImpl';

UPDATE certificate_criterion SET type = 'org.sakaiproject.certification.api.criteria.gradebook.DueDatePassedCriterion' where type = 'com.rsmart.certification.impl.hibernate.criteria.gradebook.DueDatePassedCriterionHibernateImpl';
```

The tool id has been changed for consistency:
```
UPDATE sakai_site_tool SET registration = 'sakai.certification' WHERE registration = 'com.rsmart.certification';
```

