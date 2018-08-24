##DecTool (decrypt tool)

###Usage Help
* Type at the command line prompt: `java -jar dectool-0.1.jar`
with no command line arguments (help message is displayed)
* The command line args are in alphabetical order and currently working to reorder the argument list.
* The defaults (and required values) are:
  * Both to and from DB names
  * Both to and from environments
  * A where clause that will constrain the rows
 * The outcome: the returned data column is decrypted and written to the to_db along with the remaining column data.
 
 ####The where clause
 This tool is designed only to accept the where clause as a positional parameter.
 This means there is no named option for the where clause as is the case with the db name, environment and ora_messenger.xml location.
 The where clause must have a leading double dash (--) without parentheses to signal the end of named options.
 The tool assumes and consumes everything after the double dash as where clause.
 To build the tool with the tests a copy of ora_messenger.xml must be placed in the Java resources directory.
 In the case tests are not wanted as part of the build the mvnw cmd should be:
 ./mvnw -DskipTests=true clean compile package.