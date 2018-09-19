package iix.util

import groovy.util.logging.Slf4j
import oracle.sql.CLOB
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

import javax.persistence.EntityManager
import javax.sql.rowset.serial.SerialBlob
import java.sql.Blob
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp


@Slf4j
class DecToolCommandTest extends Specification {

    private DecToolCommand decToolCommand

    private EntityManager entityManager

    def "Stub (mock) DB test using byte[] input and String result against the Trimmer class" () {
        given: "A dectool instance with encrypted data"
        byte[] dec = DecToolCommand.hex2Byte('FB41E4F82540B9F5ADF248ACDFA9489692042060D7FFC29120251C785CF9F2634850D854F333458EB62D019AD925A579FD3B043504BF2880485C2CCFF7A474DA2EB9AE0406965DC8F961F7A789F423928AE22EA13FEA6826CEBABE7211A79B7902D7269607E53BA282E040FE09E314537EA4D08F6FF9082741CFBE47ABA9CDCA072A27A64CDAAB59693ED324E48B92005FC49854C4740A24957D301C748A1665E6223FC723AC3DEF511BB7CDE67593B679E0DC8A787B6DE7F5BEF4C98DFA293671E26DC7442B53E8DDA6711A2E432430C1BBF5767A328CAFF3FCB7A5F1CBF3211F7817C8E05CFBE694B764D407D24E6F4BDA0E305F7007E28C56CF3ABEFA4A4F0DA102F38C87FF1EBFD50531746899F26A3BCAC05BAAF34CC3E21A064CD2CB1D505B7560175B934B8E100F958A327866290C4B686D32B21BC87AD14C0693E72F78FB9CDA31F5EE249DFA5A1DB435C4F857E269A4B3AB2E076FA1078B8B224F99FE909F02D86D4E9C8A44E2AA48FE96A56CF5F6362FF364DD1B5A8F23EA95B0DBC93506C74C041412035111EF677821CCADD34A0508279C4AC60EAB90ADF59B4E346B546358C7C960B0A0FED89E9C8EA79CB795742BB4521B6788495BF9120FDFF333D48078D5AE42C1D8728F529EB5DEE0F744F66CB98D0A71B3DB112C520CCDB53D8E6F066CBE6CD46C65A3840A6DC3DE27396037997E19B39EAE4ABD8A5ABBA2CE5A92D311ACACA166DDD59B55D349D1A5C061D6D94DEBEDAE24313D245927B93C2F952850304C50D9BE6D3A03D22D7FB373B8BDBB13A31050979993F097C67B2743DA26A2B15F57568629BEF40C6520416783FF10011806D6B40F2696446085CECF92FE4C99C8C8BC11658F239C8EF712E9E8B11AE312CCC8512AF3086898FF5F40377EC49393A705978043D5EB8953B5F6473A65E6D567EA6D9D83F08AC1C22CEEF8ED7D12981C67E0026EE822A5041EB545DA42021156D679DB3507433D28226015EE109A6DF1BEA3F549B0EBB2A6EE5E3DDC1C3C9675111DB7F2C1397345309161AC1FDEADEAD2A761D6A93E0B2E22080DF9C4E325C4EC40AA583068DE201D2E783E60F457AFB48029BAFD0B0E4D951E4F06EE98C005AF5780CC0FAFAA58387C8100567FFFA069B0D415DD3C1D1B00EBD0C8BD813AF5EF1E9E40C9A26E109525D4B66DF1568FED1D545736F29C7FF47F01866530FEC792E085646DCDD84CCBE50D4D050F95BB242045627308DE90E3E386D10D4503971C225DAE92160D1ACA36DCDA4BCD172A1C59839875F71832B48B7A8A7F0F93F3C199E357D4A96EAFA644609D2262735DDA20FFFC783D8EF389AEA0B839DB541C25F4058CD78CC20C89A858502D329ECFABE6145A13A8A192BA524F76A1C0E217AA66866A7C63BD7FE219146FB810F5D9C1E35BB35822EF2DDD309D8833782B020A96DD2368097A117B267E2F3F2E0195EFCAAF21D5D3CFBCF27B95656BBFE3B11CC590757B14794279119B86D2E23B9C08442295169F7AF1D7B5032B13ABC6898021DFC74C5D5B43FE9060D6F4F050A38BBB9B43E2E2D95AC34C5B2BDD47E901D053A67F32C33F33BCFD0CF6D4F04461BF91E8DD12723F');
        byte[] _dec = new byte[4000]
        Blob blob = new SerialBlob(dec)
        String xml = '<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"><soap:Body><GetTestRecordResponse xmlns="http://msegov.com/dps/mvr_service"><GetTestRecordResult><Address>1177 ALLEN RD</Address><CdlStatus>ELG</CdlStatus><City>HAZLEHURST</City><DL_number>800001013</DL_number><DOB>01/21/1966</DOB><DateIssued>11/30/2009</DateIssued><EyeColor>BRO</EyeColor><FirstName>THERESA</FirstName><Height>507</Height><LastName>ADAMS</LastName><LicExpDte>01/21/2013</LicExpDte><LocalRefID>907188</LocalRefID><Match>Y</Match><MiddleName>MICHELLE</MiddleName><NonCdlClass>R</NonCdlClass><NonCdlStatus>LIC</NonCdlStatus><Restrictions><Restriction><Desc>G DAYLIGHT DRIVING</Desc></Restriction><Restriction><Desc>J OTHER</Desc></Restriction><Restriction><Desc>B CORRECTIVE LENS</Desc></Restriction></Restrictions><Sex>2</Sex><State>MS</State><Status>Success</Status><StatusDetail>0</StatusDetail><Weight>169</Weight><Zip>390830000</Zip></GetTestRecordResult></GetTestRecordResponse></soap:Body></soap:Envelope>'

        when: "we decrypt the data"
        _dec = DecToolCommand.getTrimmer().trailing("IIX", dec)
        String sdec = new String(_dec)

        then: "we should have equal byte arrays"
        sdec == xml
    }

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
