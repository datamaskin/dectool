package iix.util

import iix.util.DecToolCommand
import iix.util.DecToolCommand
import iix.util.OraMessengerBean
import iixToolkit.db.connection.DBConnection
import spock.lang.Specification
import util.toolkit.stringtools.Trimmer
import util.toolkit.stringtools.exceptions.ClientErrorException
import util.toolkit.stringtools.exceptions.FpeDispatcherException
import util.toolkit.stringtools.exceptions.InitializationException
import util.toolkit.stringtools.exceptions.InvalidInputException
import util.toolkit.stringtools.exceptions.InvalidSyntaxException
import util.toolkit.stringtools.exceptions.NullInputException
import util.toolkit.stringtools.exceptions.TimeoutException
import util.toolkit.stringtools.exceptions.WrongDelimiterException

import java.sql.Blob
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp

class DecToolCommandTest extends Specification {

    def "Test where clauses to be put on the DecTool CLI to fetch the request_id(s) of the encrypted data" () {
        given:
        StringBuilder encSelect = new StringBuilder("select * from mvr.d_mvr_requests req join mvr.d_mvr_state_data_enc sd on (req.request_id = sd.request_id)  where ");
        String where = "req.state = 'MS' \n" +
                "   and req.time_request_inserted > trunc(sysdate) - 2 \n" +
                "   and req.status between 8 and 18 \n" +
                "   and req.account not in '0999%' \n" +
                "   and req.copy_request_type = 'O' \n" +
                "   and req.delivery_method != 'D' \n" +
                "   and nvl(req.no_hit,'x') != 'E' \n" +
                "   and req.request_id IN ( \n" +
                "638096010,638096011,638096444,638096445,638096451,638096473,638096474,638096477,638096498,638096502, \n" +
                "638144339,638144439,638144728,638144854)";

        DecToolCommand dtc = new DecToolCommand();
//        File f = new File("./ora_messenger.xml");

//        String xmlString = this.getClass().getResource( ' ora_messenger.xml').text

        File f = new File("src/main/resources/ora_messenger.xml");

        String db_from =  "MVR";

        OraMessengerBean from_omb = dtc.parseOraMessenger(f, db_from);
//        OraMessengerBean to_omb = dtc.parseOraMessenger(f, db_to);

        Connection from_conn = dtc.getConnection(from_omb);

        Statement stmt = null;
        ResultSet rs = null;

        Blob data = null;
        byte[] _data = null;
        int request_id = 0;
        Timestamp ts = null;
        int line_no = 0;
        String state = "";
        encSelect.append(where);
        int affectedRows = 0;
        int fetchSize =  0;
        try {
            stmt = from_conn.createStatement();
            rs = stmt.executeQuery(encSelect.toString());
            fetchSize = rs.getFetchSize();
            while ( rs.next() ) {

                data = rs.getBlob("DATA");
                _data = data.getBytes(1, (int) data.length());
                request_id = rs.getInt("request_id");
                ts = rs.getTimestamp("time_report_start");
                line_no = rs.getInt("line_no");
                state = rs.getString("state");

                byte[] dec = new byte[4000];

                File props = new File("src/main/resources/trimconfig.properties");
                try {
                    Trimmer trimmer = new Trimmer(props, "IIX");
                    dec = trimmer.trailing("IIX", _data);
                    System.out.println(new String(dec));
                } catch (InitializationException e) {
                    e.printStackTrace();
                } catch (InvalidInputException e) {
                    e.printStackTrace();
                } catch (FpeDispatcherException e) {
                    e.printStackTrace();
                } catch (NullInputException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (WrongDelimiterException e) {
                    e.printStackTrace();
                } catch (InvalidSyntaxException e) {
                    e.printStackTrace();
                } catch (ClientErrorException e) {
                    e.printStackTrace();
                }

                from_conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        expect:
            fetchSize > 0;
    }

//    def "Trimmer leading to insert some test DATA that can be decrypted since we should know the TrimConfig.properties and group e.g IIX, CVA" () {
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

//    }

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
