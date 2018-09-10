package iix.util

import groovy.util.logging.Slf4j
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
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp

@Slf4j
class DecToolCommandTest extends Specification {

    def "Test selecting request_ids as input to the deletion process" () {
        given:
        DecToolCommand dtc = new DecToolCommand()

        String db_from =  "MVR"
        String db_to = "MVR_IN"
        int size = 0

        String s = ""

        s = "DBConnect.iiX.MVR_IN.TEST"

        Connection to_conn = dtc.getConnection(s)

        to_conn.setAutoCommit(true);

//        String reqIds = "select * from mvr.d_mvr_requests req join mvr.d_mvr_state_data_enh sd on (req.request_id = sd.request_id)"
        StringBuilder reqIds = new StringBuilder("select * from mvr.d_mvr_requests req join mvr.d_mvr_state_data_enc sd on (req.request_id = sd.request_id) where req.state = 'MS' and sd.line_no = 1 and req.product_code != '31'")

        StringBuilder deleteReqIds = new StringBuilder("delete from mvr.d_mvr_state_data_enh where request_id in ")

        Statement stmt = null
        ResultSet rs = null
        List<Integer> l_reqIds = new ArrayList<>()
        int fetchSize = 5
        reqIds.append(" FETCH FIRST ")
        reqIds.append(fetchSize)
        reqIds.append(" ROWS ONLY")

        int affectedRows=0
        deleteReqIds.append("(")
        try {
            stmt = to_conn.createStatement()
            rs = stmt.executeQuery(reqIds.toString())

            while (rs.next()) {
                deleteReqIds.append(rs.getInt("request_id").toString()+",")
            }
        } catch(SQLException sql) {
            sql.printStackTrace()
        }

        String _str = deleteReqIds.toString()

        if (_str != null && _str.length() > 0 && _str.charAt(_str.length() - 1) == ',') {
            _str = _str.substring(0, _str.length() - 1)
        }

        _str = _str+")"

        PreparedStatement deletePrep = null

        boolean closed = false;
        try {
            deletePrep = to_conn.prepareStatement(_str)
            affectedRows = deletePrep.executeUpdate(_str)
        } catch (SQLException sql) {
            sql.printStackTrace()
        } finally {
            if (deletePrep != null)  {
                deletePrep.close()
                closed = true
            } else {
                throw new SQLException()
            }
        }

        expect:
        affectedRows > 0
        closed
    }

    def "Test where clauses to be put on the DecTool CLI to fetch the request_id(s) of the encrypted data, decrypt and insert into enh" () {
        given:
        StringBuilder encSelect = new StringBuilder("select sd.* from mvr.d_mvr_state_data_enc sd join mvr.d_mvr_requests req on sd.request_id = req.request_id where ")

            // working
//        String where = " req.state = 'MS'\n" +
//                "  and req.priority = 'S'\n" +
//                "  and req.no_hit in ('C',' ')\n" +
//                "  and req.product_code = '00'\n" +
//                "  and req.time_request_inserted > to_timestamp('2016-04-13 15:30:12', 'yyyy-mm-dd HH24:MI:SS')"
//
        // working
//        String where = "req.state = 'MS'\n" +
//                "     and req.status between 8 and 18\n" +
//                "     and req.account not in '0999%'\n" +
//                "     and req.copy_request_type = 'O'\n" +
//                "     and req.delivery_method != 'D'\n" +
//                "     and nvl(req.no_hit,'x') != 'E'\n" +
//                "     and req.request_id IN (\n" +
//                "637286085,637311017,637311334,637278452,637278486,637280637,637309597,637309909,636375757,636375757)"

        // working
//        String where = "sd.line_no = 1\n" +
//                "   and sd.record_type = 'R'\n" +
//                "   and sd.time_report_start = (select max(time_report_start) from mvr.d_mvr_state_data_enc sm where sm.request_id = req.request_id)"

        // working
        String where = "req.state = 'MS'\n" +
                "  and sd.line_no = 1\n" +
                "  and req.product_code != '31'"

        DecToolCommand dtc = new DecToolCommand()

        String s = ""
        String _s = ""
        String[] r = null

        s = "DBConnect.iiX.MVR.TEST"

        Connection from_conn = dtc.getConnection(s)
        Connection to_conn = dtc.getConnection(s)

        Statement stmt = null
        ResultSet rs = null

        Blob data = null
        byte[] _data = null
        int request_id = 0
        Timestamp trst = null
        Date trs = null
        int line_no = 0
        String state = ""
        String recType = ""
        int fetchSize =  5
        encSelect.append(where)
        encSelect.append("FETCH FIRST ")
        encSelect.append(fetchSize)
        encSelect.append(" ROWS ONLY")
        int affectedRows = 0
        StringBuilder mvr_state =  new StringBuilder("insert into MVR.D_MVR_STATE_DATA_ENH(request_id, time_report_start, line_no, state, data, time_report_start_ts,record_type) values(?,?,?,?,?,?,?)");

        boolean wrongdelimiter = false
        try {
            stmt = from_conn.createStatement()
            rs = stmt.executeQuery(encSelect.toString())

            while ( rs.next() ) {

                data = rs.getBlob("DATA")
                _data = data.getBytes(1, (int) data.length())
                request_id = rs.getInt("request_id")
                trst = rs.getTimestamp("time_report_start_ts")
                trs = rs.getDate("time_report_start")
                line_no = rs.getInt("line_no")
                state = rs.getString("state")
                recType = rs.getString("record_type")
                System.out.println(request_id + " " + trst + " " + line_no + " " + state)
                byte[] dec = new byte[4000]

                try {
                    TrimmerSingleton ts = TrimmerSingleton.getInstance()
                    dec = ts.trimmer.trailing("IIX", _data)
                    System.out.println(new String(dec))
                } catch (InitializationException e) {
                    e.printStackTrace()
                } catch (InvalidInputException e) {
                    e.printStackTrace()
                } catch (FpeDispatcherException e) {
                    e.printStackTrace()
                } catch (NullInputException e) {
                    e.printStackTrace()
                } catch (TimeoutException e) {
                    e.printStackTrace()
                } catch (WrongDelimiterException e) {
                    wrongdelimiter = true
                    e.printStackTrace()
                } catch (InvalidSyntaxException e) {
                    e.printStackTrace()
                } catch (ClientErrorException e) {
                    e.printStackTrace()
                }

                PreparedStatement pstm = to_conn.prepareStatement(mvr_state.toString())

                pstm.setInt(1, request_id)
                pstm.setDate(2, trs)
                pstm.setInt(3, line_no)
                pstm.setString(4, state)
                pstm.setString(5, new String(dec))
                pstm.setTimestamp(6, trst)
                pstm.setString(7, recType)

                affectedRows += pstm.executeUpdate()

                System.out.println("affectedRows: " + affectedRows)

                System.out.println("Blob length: " + data.length())

                to_conn.commit()
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            to_conn.close()
            from_conn.close()
        }

        expect:
            !wrongdelimiter
            affectedRows > 0
    }

    def "Test query to fetch request_id(s) against a where clause" () {
        given:
//        StringBuilder encSelect = new StringBuilder("select sd.* from mvr.d_mvr_state_data_enc sd join mvr.d_mvr_requests req on sd.request_id = req.request_id where ")
        String where = "req.state = 'MS'  and sd.line_no = 1 and req.product_code != '31'"
        DecToolCommand dtc = new DecToolCommand()

        List<Integer> l = new ArrayList<>()
        l = dtc.getReqIds(where)
        log.debug("Request ids array:")
        for (int i = 0; i < l.size(); i++) {
            log.debug(l.get(i).toString())
        }

        expect:
            l.size() > 0

    }

    def "Test the Trimmer instance for processing TrimConfig.properties correctly using TrimmerSingleton" () {
        given:
        TrimmerSingleton ts = TrimmerSingleton.getInstance()
        log.debug("TrimmerSingleton: `${ts}`")

        expect:
        ts
    }

    /*def "getConnection test this method for returning a valid jdbc connection string from the ora_messenger.xml file" () {
        given:
        File f = new File("src/main/resources/ora_messenger.xml")
        String sysDate = ";"

        DecToolCommand hwc = new DecToolCommand()
        OraMessengerBean _omb = hwc.parseOraMessenger(f, "MVR")
        java.sql.Connection conn = null
        try {
//            conn = hwc.getConnection("MVR", "TEST", f)
            conn =  DBConnection.CreateConnection(_omb.get_javaURL(), _omb.getUser(), _omb.getEncpPswd())
            sysDate = DBConnection.getDBSysdate(conn)
        } catch (Exception e) {
            e.printStackTrace()
        }

        expect:
        sysDate.contains("Sysdate")
    }*/

    /*def "ParseJavaURL test this method for the string extracted from the right side of the '@' sign from JavaURl"() {
        given:
        String url = "(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=DEVTIP)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=oratestM.util)))"
        DecToolCommand hwc = new DecToolCommand()
        String _url =  hwc.parseJavaURL(url)

        expect:
        _url.equals("jdbc:oracle:thin:@//DEVTIP:1521/oratestM.util")
    }*/

    /*def "ParseOraMessenger test this method for the default local file: src/main/resources/ora_messenger.xmls"() {
        given:
        File f = new File("src/main/resources/ora_messenger.xml")

        DecToolCommand hwc = new DecToolCommand()
        String oraMessenger = hwc.parseOraMessenger(f, "MVR")

        expect:
        oraMessenger.contains("JavaURL")
    }*/

}
