package iix.util

import iix.util.DecToolCommand
import iix.util.DecToolCommand
import iix.util.OraMessengerBean
import iixToolkit.db.connection.DBConnection
import spock.lang.Specification

class HelloWorldCommandTest extends Specification {

    def "Trimmer leading to insert some test DATA that can be decrypted since we should know the TrimConfig.properties and group e.g IIX, CVA" () {
       /* given:
        File f = new File("c://utils/ora_messenger.xml");
        String sysDate = ";"
        String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas euismod ultricies congue";

        DecToolCommand hwc = new DecToolCommand();
        OraMessengerBean _omb = hwc.parseOraMessenger(f);
        java.sql.Connection conn = null;
        try {
            conn =  DBConnection.CreateConnection(_omb.get_javaURL(), _omb.getUser(), _omb.getEncpPswd());
            sysDate = DBConnection.getDBSysdate(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }

        expect:*/

    }

    def "getConnection test this method for returning a valid jdbc connection string from the ora_messenger.xml file" () {
        given:
        File f = new File("c://utils/ora_messenger.xml");
        String sysDate = ";"

        DecToolCommand hwc = new DecToolCommand();
        OraMessengerBean _omb = hwc.parseOraMessenger(f, "MVR");
        java.sql.Connection conn = null;
        try {
            conn =  DBConnection.CreateConnection(_omb.get_javaURL(), _omb.getUser(), _omb.getEncpPswd());
            sysDate = DBConnection.getDBSysdate(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }

        expect:
        sysDate.contains("Sysdate");
    }

    def "ParseJavaURL test this method for the string extracted from the right side of the '@' sign from JavaURl"() {
        given:
        String url = "(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=DEVTIP)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=oratestM.util)))";
        DecToolCommand hwc = new DecToolCommand();
        String _url =  hwc.parseJavaURL(url);

        expect:
        _url.equals("jdbc:oracle:thin:@//DEVTIP:1521/oratestM.util");
    }

    def "ParseOraMessenger test this method for the default local file: c://utils/ora_messenger.xml"() {
        given:
        File f = new File("c://utils/ora_messenger.xml");

        DecToolCommand hwc = new DecToolCommand();
        String oraMessenger = hwc.parseOraMessenger(f, "MVR");

        expect:
        oraMessenger.contains("JavaURL");
    }

    def "Run"() {

    }
}
