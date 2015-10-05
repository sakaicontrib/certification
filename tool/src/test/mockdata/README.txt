This directory contains files to prepopulate the CLE with mock data to test the Certification Tool.

Instructions:

1) create a site in the CLE and copy down the site ID. Add gradebook and certification tools.
2) edit certificationtestdata.sql and replace 'real site_id' with the ID from step 1
	eg. in vi, do the following:
		:%s/real site_id/<your new ID from step 1>/g
3) rename the folder 'templates/real site_id' to 'templates/<your new ID from step 1>'
4) create a gradebook two gradebook items in your site and copy down their IDs (may need to look in the database)
5) edit certificationtestdata.sql and replace 'gradebookitem1' and gradebookitem2' with the IDs
6) run the SQL script:
	mysql -u <username> -p <database> < certificationtestdata.sql
7) copy the entire templates directory into $TOMCAT_HOME
	cp -r templates $TOMCAT_HOME
