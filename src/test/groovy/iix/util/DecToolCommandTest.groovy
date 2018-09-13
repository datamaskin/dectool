package iix.util

import groovy.util.logging.Slf4j
import picocli.CommandLine
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

        String s = ""

        s = "DBConnect.iiX.MVR_IN.TEST"

        Connection to_conn = dtc.getConnection(s)

        to_conn.setAutoCommit(true)

        StringBuilder reqIds = new StringBuilder("select request_id from mvr.d_mvr_requests req where req.state = 'MS' and req.request_id = '692367523'")

        StringBuilder deleteReqIds = new StringBuilder("delete from mvr.d_mvr_state_data_enh where request_id in ")

        Statement stmt = null
        ResultSet rs = null

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

        boolean closed = false
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
        StringBuilder encSelect = new StringBuilder("select * from mvr.d_mvr_state_data_enc where ")

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
        String where = "request_id = '692367523'"

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
        encSelect.append(" FETCH FIRST ")
        encSelect.append(fetchSize)
        encSelect.append(" ROWS ONLY")
        int affectedRows = 0
        StringBuilder mvr_state =  new StringBuilder("insert into MVR.D_MVR_STATE_DATA_ENH(request_id, time_report_start, line_no, state, data, time_report_start_ts,record_type) values(?,?,?,?,?,?,?)")

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
                    dec = DecToolCommand.getTrimmer().trailing("IIX", _data)
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

        String where = "req.state = 'MS' and req.request_id != '692367523'"
        DecToolCommand dtc = new DecToolCommand()
        String db_from =  "MVR"
        String db_to = "MVR_IN"
        String from_env = "TEST"

        List<Integer> l = new ArrayList<>()
        Connection fromConnection = dtc.getConnection("DBConnect.iiX.MVR.TEST")
        dtc.fsize = 5
        l = dtc.getReqIds(fromConnection, where)
        log.debug("Request ids array:")
        for (int i = 0; i < l.size(); i++) {
            log.debug(l.get(i).toString())
        }

        expect:
            l.size() > 0

    }

    def "Test the Trimmer instance for processing " () {
        given:
        Trimmer t = DecToolCommand.getTrimmer()
        log.debug("Trimmer: `${t}`")

        expect:
        t
    }

    def "Test the picoCLI default args for non-null values" () {
        given:

        DecToolCommand dtc = new DecToolCommand()
        CommandLine cl = new CommandLine(DecToolCommand.class)
        List<CommandLine> l = cl.parse("-e", "TEST","-f","MVR","-t","MVR_IN","-E","TEST","-s","0","-c","5","req.state='MS' and req.request_id='692367523'" )

        expect:
        l.size() > 0
        l.get(0).getCommandSpec().options().get(0).initialValue() == "MVR"
        l.get(0).getCommandSpec().options().get(1).initialValue() == "TEST"
    }

}
