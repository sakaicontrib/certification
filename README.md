# certification

The master branch is the Sakai 12 version.

11.x branch is for Sakai 11.

Sakai10 Branch is the one needed for Sakai 10.

## Extra Properties

Extra user properties are implemented in Certification for the purpose of displaying in the reporting interface 
only. They are not currently implemented as additional criterion for earning certificates. These properties are 
provisioned through LDAP, and are accessed through user.getProperties(). It can however be used without LDAP, 
with the caveat that the data would have to be manually provisioned into the sakai_user_property table.

A user is only able to view these extra properties if their user role has the realm permission 
`certificate.extraprops.view` and the sakai.property below (enable) is set to true.

It is possible to have administrator defined extra properties via the use of the following sakai properties:
- certification.extraUserProperties.enable
- certification.extraUserProperties.keys
- certification.extraUserProperties.titles

All these properties are defined within the `sakai.properties` file.

### certification.extraUserProperties.enable
This is a boolean value, whose default is false, which controls whether or not the extra properties are enabled.

`certification.extraUserProperties.enable = true`

### certification.extraUserProperties.keys

This is a list of strings of the extra properties. These values are the property names which can be accessed through user.getProperties().

This value must be defined in one of the following manners:

#### As a comma-delimited list
`certification.extraUserProperties.keys = employeeNumber, studentNumber, department`

#### Individual
```
certification.extraUserProperties.keys.count = 3
certification.extraUserProperties.keys.1 = employeeNumber
certification.extraUserProperties.keys.2 = studentNumber
certification.extraUserProperties.keys.3 = department
```

### certification.extraUserProperties.titles
This is a list of the titles for the keys which is displayed to the user in the UI. This list corresponds to the keys list and is defined in a similar manner.

#### As a comma-delimited list
`certification.extraUserProperties.titles = Employee Number, Student Number, Department`

#### Individual
```
certification.extraUserProperties.titles.count = 3
certification.extraUserProperties.titles.1 = Employee Number
certification.extraUserProperties.titles.2 = Student Number
certification.extraUserProperties.titles.3 = Department
```
