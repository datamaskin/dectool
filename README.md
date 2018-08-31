#DecTool (decrypt tool)

##Usage Help
* Type at the command line prompt: `java -jar dectool-0.1.jar`
with no command line arguments (help message is displayed)
* The command line args are in alphabetical order and currently working to reorder the argument list.
* The defaults (and required values) are:
  * Both to and from DB names
  * Both to and from environments
  * A where clause that will constrain the rows
 * The outcome: the returned data column is decrypted and written to the to_db along with the remaining column data.
 
###The where clause
 This tool is designed only to accept the where clause as a positional parameter.
 This means there is no named option for the where clause as is the case with the db name, environment and ora_messenger.xml location.
 The where clause must have a leading double dash (--) without parentheses to signal the end of named options.
 The tool assumes and consumes everything after the double dash as where clause.
 Example command line:
 `java -jar dectool-1.0-SNAPSHOT.jar -e TEST -f MVR -o ..\..\..\..\..\..\utils\ora_messenger.xml -t MVR_IN -E TEST -s 5 -- sd.line_no = 1 and sd.record_type = 'R' and sd.time_report_start = (select max(time_report_start) from mvr.d_mvr_state_data_enc sm where sm.request_id = req.request_id)
`
 
####Git clone and build
 Use git bash here in Explorer ostensibly right clicking your favorite directory.
 Run: `git clone ssh://git.iix.com/git/Common/dectool.git`
 
 To build the tool with the tests cases a copy of ora_messenger.xml must be placed in the Java resources directory with the following snippet:
 ```
 <TEST>
         <TnsName>oratestm.world</TnsName>
         <Schema>MVR</Schema>
         <User></User>
         <Pswd></Pswd>
         <EncpPswd></EncpPswd>
         <JavaURL>jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=DEVTIP)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=oratestM.world)))</JavaURL>
 </TEST>
 ```
 It is recommended that if the end user does not wish to do further development then the tool should be built without the test cases.
 To build without test cases the mvnw cmd should be:
* `./mvnw -DskipTests=true clean compile package` (Linux)
* `mvnw -DskipTests=true clean compile package` (Windows)
* JDK: 1.8
 
#####Running dectool
 * The correct version of `TrimConfig.properties` for the target user must be placed in the resources directory to enable decryption.
 * To use a CLI input file please see the dectool args template at: `src/main/resources/dectoolargs.template`
 * Assuming the minimal requirements the user should be able to execute the following:
 
 `java -jar target/dectool-0.1.jar @src/main/resources/dectoolargs`
 * Use the dectoolargs.template as just that: a template.
 * Name the file to reflect the where clause or dbname and environment used.
 * There is a reference in the template file and repeated here for instructions on how to construct an input file.
 `https://picocli.info/#AtFiles`
 
 ######Tool creation and config
 Assuming sdkman is installed:
 * JDK 1.8+ installed
 * Micronaut installed 1.0.0
 * At the CLI run:
 
 `mn create-cli-app --build maven --test spock dectool`