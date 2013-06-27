spring-security-oauth2-migrator
===============================

===============================
1. Description
===============================

This is a tool that migrates spring security oauth2 data stored in a JDBC store between pre 1.0 versions of spring security oauth2 
such as 1.0.0.M6d and newer version like 1.0.5.RELEASE

At Avego http://www.avego.com we used this tool to migrate access and refresh token data when upgrading spring security ouath2.

The tool supports MySql and Oracle but can be extended easily for other databases.

If building the tool using Maven you must first make the Oracle ODJBC 6 jar available either in your own hosted mvn repository
or install it into your local m2 repository. To install it into your local repo:

mvn install:install-file -Dfile={ORACLE_HOME}/jdbc/lib/ojdbc6.jar -Dpackaging=jar\
 -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.3.0
where {ORACLE_HOME} is the path to the Oracle Database installation.

Alternatively you can delete the com.oracle dependency and artifaction item references from the pom.xml file and remove the OracleOauthMigrationDao class.


===============================
2. How to use
===============================
The oauth migration tool is packaged as a tgz file that contains:

a) An executable jar file - this can be run as a standard executable jar with the class path including the jars in the newlib dir

b) A newlib dir - this contains the dependency jars that should be added to the class path 

c) An oldlib dir - this contains jars of the old version of spring security and oauth 2 that is being migrated, you can add any custom
jars that are needed for deserialization here e.g. if you have your own principle or other class implementations.

If you want to use different versions of the spring jars you can replace them in the newlib and old lib dir

To execute you can run this command from the same directory as this readme

Linux/Mac:

CLASSPATH=$(echo "newlib"/*.jar | tr ' ' ':'):$(echo *.jar | tr ' ' ':')

java -Dcom.avego.oauth.migration.OauthMigrationDao=OauthMigrationDaoFQN -cp $CLASSPATH com.avego.oauth.migration.OauthDataMigrator dburl dbuser dbpw remove_refresh_tokens serialize_new_token_values oauth_access_token_table oauth_refresh_token_table

where: 

1: OauthMigrationDaoFQN The com.avego.oauth.migration.OauthMigrationDao property is set to the FQN class name of the db implementation, this can be either:

a) com.avego.oauth.migration.MysqlOauthMigrationDao for MySQL

b) com.avego.oauth.migration.OracleOauthMigrationDao for Oracle

c) Another implementation that you provide

If this property is not specified this defaults to the MySql implementation

2: db url is the jdbc url to the oauth security db

3: db user is the database username to use 

4: dbpw is the database password to use.

5: remove_refresh_tokens Whether to remove refresh tokens, older version of spring sec oauth2 always created a refresh token for each access token
so if you know you dont need refresh tokens migrated set this to true to remove them all

6: serialize_new_token_values Whether the serialized authentication and token blobs in the refresh and access token tables should have the stored
token value in them changed to the hashed one or if it should stay as non hashed. Set to false unless you know the version of spring security
you are upgrading to has changed to store them as hashed.

7: oauth_access_token_table This is the table name that stores the access tokens, default if not provided is oauth_access_token

8: oauth_refresh_token_table This is the table name that stores the refresh tokens, default if not provided is oauth_refresh_token


MySQL Examples:

a) For the default config that uses the db schema api_security and the username/pw combo of root: root  and uses the default table names and migrates and keeps refresh tokens:

java -cp $CLASSPATH com.avego.oauth.migration.OauthDataMigrator "jdbc:mysql://127.0.0.1:3306/api_security" root root

b) if you dont want to keep refresh tokens and the version of spring migrating to expects unhashed tokens in the serialized data (as is the case with v 1.0.5):

java -cp $CLASSPATH com.avego.oauth.migration.OauthDataMigrator "jdbc:mysql://127.0.0.1:3306/api_security" root root true false

c) if you want to keep refresh tokens and run the migration on backup tables called oauth_access_token_bak and oauth_refresh_token_table_bak rather than the active ones:

java -cp $CLASSPATH com.avego.oauth.migration.OauthDataMigrator "jdbc:mysql://127.0.0.1:3306/api_security" root root false false oauth_access_token_bak oauth_refresh_token_table_bak

Oracle Examples:

For the default config that uses the SPRING_SEC_OAUTH2 schema on localhost and default table names:

java -Dcom.avego.oauth.migration.OauthMigrationDao=com.avego.oauth.migration.OracleOauthMigrationDao -cp $CLASSPATH com.avego.oauth.migration.OauthDataMigrator "jdbc:oracle:thin:@//localhost:1521/xe" SPRING_SEC_OAUTH2 SPRING_SEC_OAUTH2_PW

===============================
3. Extending to use a different DB
===============================

The current version includes support for a MySql and Oracle implementations, to support other databases you need to add an implementation
of com.avego.oauth.migration.OauthMigrationDao and then set the system property com.avego.oauth.migration.OauthMigrationDao to the fully qualified
classname. 

For example if you have a class called foo.bar.OracleOauthMigrationDao then you would run it with 

java -cp $CLASSPATH -Dcom.avego.oauth.migration.OauthMigrationDao=foo.bar.OracleOauthMigrationDao com.avego.oauth.migration.OauthDataMigrator dburl dbuser dbpw



