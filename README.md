##DecTool (decrypt tool)

###Usage Help
* Type at the command line prompt: `java -jar dectool-0.1.jar`
with no command line arguments (help message is displayed)
* The command line args are in alphabetical order and currently working to reorder the argument list.
* The defaults (and required values) are:
  * Both to and from DB names
  * Both to and from environments
 * The optional argument values are:
   * A list of CSV request_id(s)
   * A where clause the constrains the request_id(s)
 * The outcome: the returned request_id(s) data column is decrypted and written to the to_db.