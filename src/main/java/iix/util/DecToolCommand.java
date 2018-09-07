package iix.util;

import iixToolkit.db.connection.DBConnection;
import iixToolkit.xml.XmlParser;
import iixToolkit.xml.XmlParserException;
import io.micronaut.configuration.picocli.PicocliRunner;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import util.toolkit.stringtools.Trimmer;
import util.toolkit.stringtools.exceptions.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class TrimmerSingleton
{
    // static variable trimmer_instance of type TrimmerSingleton
    private static TrimmerSingleton trimmer_instance = null;

    public Trimmer trimmer;

    // private constructor restricted to this class itself
    private TrimmerSingleton()
    {
        try {
            trimmer = new Trimmer("IIX");
        } catch (InvalidInputException e) {
            e.printStackTrace();
        } catch (InitializationException e) {
            e.printStackTrace();
        }
    }

    public static TrimmerSingleton getInstance()
    {
        if (trimmer_instance == null)
            trimmer_instance = new TrimmerSingleton();

        return trimmer_instance;
    }
}
/*@Command(name = "DecToolCommand",
        header = {
                "@|green  __                ___      |@",
                "@|green |  \\ _ _ _   _ |_   | _  _ ||@",
                "@|green |__/(-(_| \\/|_)|_   |(_)(_)||@",
                "@|green           / |               |@"},
        description = "dectool", mixinStandardHelpOptions = true, version = "0.1")*/

@Command(name = "DecToolCommand",
        header = {
                "@|green  __                ___      |@",
                "@|green |  \\ _ _ _   _ |_   | _  _ ||@",
                "@|green |__/(-(_| \\/|_)|_   |(_)(_)||@",
                "@|green           / |               |@"},
        description = "dectool: decrypts using @args file and path (see README.md) ", version = "0.1")
public class DecToolCommand implements Runnable {

    @Option(names = {"-o","--ora_messenger"},    paramLabel = "FILE", description = "Path to datasource ora_messenger.xml definition (default: ${DEFAULT-VALUE})", required = true)
    static File f = new File("c://utils/ora_messenger.xml");

    @Option(names = {"-f", "--from "}, paramLabel = "db_from", description = "The database to read from  (default: ${DEFAULT-VALUE})", required = true)
    static String db_from = "MVR";

    @Option(names = {"-e", "--env_from"}, paramLabel = "env_from", description = "The environment of the source database (default: ${DEFAULT-VALUE})", required = true)
    static String env_from = "TEST";

    @Option(names = {"-t", "-to"}, paramLabel = "db_to", description = "The database to write to", required = true)
    static String db_to = "MVR";

    @Option(names = {"-E", "-env_to"}, paramLabel = "env_to", description = "The database environment to write to", required = true)
    static String env_to = "TEST";

    @Option(names = {"-s", "-fetchSize"}, paramLabel = "fsize", description = "The row fetch first size (default: ${DEFAULT-VALUE})")
    static int fsize = 100;

    @Option(names = {"-c", "-commitCount"}, paramLabel = "commitcnt", description = "The request_id transaction count before commit")
    static int commitcnt = 500;

    /*@Option(names = {"-v", "--verbose"}, description = "Tool description details")
    boolean verbose=false;*/

    @CommandLine.Parameters
    private
    List<String> where;

    private void listNodes(Node node, String indent) {
        if (node instanceof Text) {
            String value = node.getNodeValue().trim();
            if (value.equals("")) {
                return;
            }
        }

        String nodeName = node.getNodeName();

        System.out.println(indent + " Node is: " + nodeName);

    }

    private String listNodes(Node node) {
        if (node instanceof Text) {
            String value = node.getNodeValue().trim();
            if (value.equals("")) {
                return "";
            }
        }

        String nodeName = node.getNodeName();

        return nodeName;
    }

        public  Object getValue(Member member, Object object) {
        Object value = null;

        if ( member instanceof Method ) {
            Method method = ( Method ) member;
            try {
                value = method.invoke( object );
            }
            catch ( IllegalAccessException e ) {
                throw new RuntimeException( "Unable to access " + method.getName(), e );
            }
            catch ( InvocationTargetException e ) {
                throw new RuntimeException( "Unable to access " + method.getName(), e );
            }
        }
        else if ( member instanceof Field) {
            Field field = ( Field ) member;
            try {
                value = field.get( object );
            }
            catch ( IllegalAccessException e ) {
                throw new RuntimeException( "Unable to access " + field.getName(), e );
            }
        }
        return value;
    }

    String parseJavaURL(String url) {
       String _url = "jdbc:oracle:thin:@//";
       String[] url_ = url.split("\\(", 8);
       List<String> urlList = new ArrayList<>();

//       System.out.println("JavaURL: " + url_[4].split("\\)", 2)[0]);
//       System.out.println("JavaURL: " + url_[5].split("\\)", 2)[0]);
//       System.out.println("JavaURL: " + url_[7].split("\\)", 2)[0]);

       urlList.add(url_[4].split("\\)", 2)[0].split("=", 2)[1]);
       urlList.add(url_[5].split("\\)", 2)[0].split("=", 2)[1]);
       urlList.add(url_[7].split("\\)", 2)[0].split("=", 2)[1]);

       _url = _url + url_[4].split("\\)", 2)[0].split("=", 2)[1] + ":" +
                            (url_[5].split("\\)", 2)[0].split("=", 2)[1] + "/" +
                                    url_[7].split("\\)", 2)[0].split("=", 2)[1]);

        return _url;
    }

    public static void main(String[] args) throws Exception {
        try {
            CommandLine.ParseResult parseResult = new CommandLine(DecToolCommand.class).parseArgs(args);
            if (!CommandLine.printHelpIfRequested(parseResult)) {
                System.out.println("Fetch size has matched option?: " + parseResult.hasMatchedOption('s'));
                System.out.println(parseResult.matchedOption('s'));
                System.out.println(parseResult.originalArgs());
                int sizeVal = parseResult.matchedOptionValue('s', 100);
                File fVal = parseResult.matchedOptionValue('o', new File("c://utils/ora_messenger.xml"));
                String dbfromVal = parseResult.matchedOptionValue('f', "MVR");
                String envfromVal = parseResult.matchedOptionValue('e', "TEST");
                String dbtoVal = parseResult.matchedOptionValue('t', "MVR");
                int cntVal = parseResult.matchedOptionValue('c', 500);
                System.out.println("Matched fsize value: " + sizeVal);
                if (parseResult.hasMatchedOption('s')) {
                   DecToolCommand.fsize = sizeVal;
                }
                if (parseResult.hasMatchedOption('o')) {
                    DecToolCommand.f = fVal;
                }
                if (parseResult.hasMatchedOption('f')) {
                    DecToolCommand.db_from = dbfromVal;
                }
                if (parseResult.hasMatchedOption('e')) {
                    DecToolCommand.env_from = envfromVal;
                }
                if (parseResult.hasMatchedOption('t')) {
                    DecToolCommand.db_to = dbtoVal;
                }
                if (parseResult.hasMatchedOption('c')) {
                    DecToolCommand.commitcnt = cntVal;
                }
            }
        } catch (CommandLine.ParameterException ex) { // command line arguments could not be parsed
            System.err.println(ex.getMessage());
            ex.getCommandLine().usage(System.err);
        }
        PicocliRunner.run(DecToolCommand.class, args);
    }

    public OraMessengerBean parseOraMessenger(File f, String db_name) {
        boolean found = false;
        OraMessengerBean omb = new OraMessengerBean();

        if (f!=null) {
//            System.out.println("Datasource definition exists: " + f.exists());

            try {
                XmlParser xp = new XmlParser(f);
                String root = xp.getRootNode().getNodeName();
                Node rootNode = xp.getRootNode();
                if (root.equals("DBConnect")) {
                    System.out.println("Root node found: " + root);
                    List<Node> nodes = new ArrayList<>();
                    nodes = xp.getNodes("iiX");
                    System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Nodes>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

                    Document doc = xp.getDocument();
                    NodeList nodeList = rootNode.getChildNodes(); // iiX
                    System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Level1>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    for (int i = 0; i < nodeList.getLength(); i++) { // iiX, cvE, cvA
//                        System.out.println(nodeList.item(i).getNodeName());
                        NodeList _nodeList = nodeList.item(i).getChildNodes();
                        for (int j = 0; j < _nodeList.getLength(); j++) { // MOMVR, MNMVR, MVRSTATEB, MVR, MVR_IN, ARCHIVE, ARCHIVE_IN, DGD, COMMON, INVOICE, POLM, PM, cvE, ELIENS, cvA, ALIR
//                            listNodes(_nodeList.item(j), ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+_nodeList.item(j).getNodeName());
                            String node = listNodes(_nodeList.item(j));

                            omb = setOMB(omb, node, db_name);

                            NodeList nodeList_ = _nodeList.item(j).getChildNodes();
                            for (int k = 0; k < nodeList_.getLength() && !found; k++) { // PROD, TEST, AAAMI_PROD, AAAMI_ACPT, AAAMI_TEST, DAS_PROD, DAS_ACPT, DAS_TEST, PROD_IN, ACPT, sunTEST
//                                System.out.println(nodeList_.item(k).getNodeName());
//                                listNodes(nodeList_.item(k), ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                                String env = listNodes(nodeList_.item(k));
                                String _dbname = nodeList_.item(k).getParentNode().getNodeName();
//                                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Parent node: "+ _dbname);
                                Method[] methods = omb.getClass().getMethods();

                                String __dbname = "";
                                for( int index =0; index < methods.length; index++){
                                    if( methods[index].getName().contains( "get")) {
                                        if (getValue(methods[index], omb) != null) {
                                            __dbname = getValue(methods[index], omb).toString();
                                            if (__dbname.equals(_dbname) && env.equals(env_from)) {
//                                                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> :  " +_dbname + " == "+ __dbname+" --> "+env);
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (found) {
                                    NodeList _nodeList_ = nodeList_.item(k).getChildNodes();
                                    for (int m = 0; m <_nodeList_.getLength() ; m++) { // TnsName, User, Pswd, EncpPswd, JavaURL
//                                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Parent to values: "+_nodeList_.item(m).getParentNode().getNodeName());
                                        NodeList __nodeList = _nodeList_.item(m).getChildNodes();
                                        for (int n = 0; n < __nodeList.getLength(); n++) {
//                                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>found: "+__nodeList.item(n).getNodeName());
                                            switch (_nodeList_.item(m).getNodeName()) { // Text content of the previous for loop
                                                case "TnsName" :
//                                                    System.out.println("TnsName: "+XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    omb.setTnsName(XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    break;
                                                case "Schema" :
//                                                    System.out.println("Schema: "+XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    omb.setSchema(XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    break;
                                                case "User" :
//                                                    System.out.println( "User: "+XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    omb.setUser(XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    break;
                                                case "Pswd" :
//                                                    System.out.println("Pswd: "+XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    omb.setPswd(XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    break;
                                                case "EncpPswd" :
//                                                    System.out.println("EncpPswd:"+XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    omb.setEncpPswd(XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    break;
                                                case "JavaURL" : // jdbc:oracle:thin:@//192.168.117.230:1521/oratesta.util
                                                    System.out.println("JavaURL:"+XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    omb.setJavaURL(XmlParser.getNodeValue(_nodeList_.item(m)));
                                                    String url = omb.getJavaURL();
                                                    String _url[] = url.split("@", 2);
                                                    url = parseJavaURL(_url[1]);
                                                    omb.set_javaURL(url);
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } // END parse OraMessenger.xml

//                System.out.println(omb.toString());

            } catch ( XmlParserException | IOException | ParserConfigurationException | SAXException  e) {
                e.printStackTrace();
            }
        }
        return omb;
    }

    private OraMessengerBean setOMB(OraMessengerBean omb, String node, String _db_name) {

        switch (node) {
            case "MOMVR" :
                if (node.equals(_db_name)) omb.setMOMVR(node);
                break;
            case "MNMVR" :
                if (node.equals(_db_name)) omb.setMNMVR(_db_name);
                break;
            case "MVRSTATEB" :
                if (node.equals(_db_name)) omb.setMVRSTATEDB(_db_name);
                break;
            case "MVR" :
                if (node.equals(_db_name)) omb.setMVR(_db_name);
                break;
            case "MVR_IN" :
                if (node.equals(_db_name)) omb.setMVR_IN(_db_name);
                break;
            case "ARCHIVE" :
                if (node.equals(_db_name)) omb.setARCHIVE(_db_name);
                break;
            case "ARCHIVE_IN" :
                if (node.equals(_db_name)) omb.setARCHIVE_IN(_db_name);
                break;
            case "DGD" : if (node.equals(_db_name)) omb.setDGD(_db_name);
                break;
            case "COMMON" : if (node.equals(_db_name)) omb.setCOMMON(_db_name);
                break;
            case "INVOICE" : if (node.equals(_db_name)) omb.setINVOICE(_db_name);
                break;
            case "POLM" : if (node.equals(_db_name)) omb.setPOLM(_db_name);
                break;
            case "PM" : if (node.equals(_db_name)) omb.setPM(_db_name);
                break;
            case "cvE" : if (node.equals(_db_name)) omb.setCvE(_db_name);
                break;
            case "ELIENS" : if (node.equals(_db_name)) omb.setELIENS(_db_name);
                break;
            case "cvA" : if (node.equals(_db_name)) omb.setCvA(_db_name);
                break;
            case "ALIR" : if (node.equals(_db_name)) omb.setALIR(_db_name);
                break;
        }
        return omb;
    }

    public java.sql.Connection getConnection(OraMessengerBean omb) {

        java.sql.Connection conn = null;
        try {

            conn =  DBConnection.CreateConnection(omb.get_javaURL(), omb.getUser(), omb.getEncpPswd());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    private java.sql.Connection getConnection(String inconn, String inuser, String pw) {

        java.sql.Connection conn = null;

        try {
            conn =  DBConnection.CreateConnection(inconn, inuser, pw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    java.sql.Connection getConnection(String inconn) {

        java.sql.Connection conn = null;

        try {
            conn =  DBConnection.CreateConnection(inconn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    List<Integer> getReqIds(String where) {

        StringBuilder encSelect = new StringBuilder("select sd.* from mvr.d_mvr_state_data_enc sd join mvr.d_mvr_requests req on sd.request_id = req.request_id where ");

        String s = "DBConnect.iiX.MVR.TEST";
        DecToolCommand dtc = new DecToolCommand();
        Connection from_conn = dtc.getConnection(s);
        Statement stmt = null;
        ResultSet rs = null;
        encSelect.append(where);
        encSelect.append("FETCH FIRST ");
        encSelect.append(fsize);
        encSelect.append(" ROWS ONLY");
        List<Integer> l_reqids = new ArrayList<>();

        try {
            stmt = from_conn.createStatement();
            rs = stmt.executeQuery(encSelect.toString());
            while (rs.next()) {
                l_reqids.add(rs.getInt("request_id"));
            }
            from_conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return l_reqids;
    }

    private int updateDec(int request_id, int _affectedRows, int commCnt) throws Exception {

        StringBuilder encSelect = new StringBuilder("select * from mvr.d_mvr_requests req join mvr.d_mvr_state_data_enc sd on (req.request_id = sd.request_id) where ");
        String _encSelect = "select * from mvr.d_mvr_state_data_enc where request_id = " + Integer.toString(request_id);

        int affectedRows = 0;
        if (request_id >= 0) {

            StringBuilder s = new StringBuilder("DBConnect.iiX.");
            s.append(db_from);
            s.append(".");
            s.append(env_from);

            StringBuilder _s = new StringBuilder("DBConnect.iiX.");
            _s.append(db_to);
            _s.append(".");
            _s.append(env_to);

            Connection from_conn = getConnection(s.toString());
            Connection to_conn = getConnection(_s.toString());

            to_conn.setAutoCommit(false);

            Statement stmt = null;
            ResultSet rs = null;

            Blob data = null;
            byte[] _data = null;
            Timestamp trst = null;
            Date trs = null;
            int line_no = 0;
            String state = "";
            String recType = "";
            encSelect.append(request_id);
            encSelect.append("FETCH FIRST ");
            encSelect.append(fsize);
            encSelect.append(" ROWS ONLY");

            StringBuilder mvr_state = new StringBuilder("insert into MVR.D_MVR_STATE_DATA_ENH(request_id, time_report_start, line_no, state, data, time_report_start_ts,record_type) values(?,?,?,?,?,?,?)");
            PreparedStatement pstm = null;

            TrimmerSingleton ts = TrimmerSingleton.getInstance();

            try {
                stmt = from_conn.createStatement();
                rs = stmt.executeQuery(_encSelect);

                if (rs.next()) {

                    data = rs.getBlob("DATA");
                    _data = data.getBytes(1, (int) data.length());
                    request_id = rs.getInt("request_id");
                    trst = rs.getTimestamp("time_report_start_ts");
                    trs = rs.getDate("time_report_start");
                    line_no = rs.getInt("line_no");
                    state = rs.getString("state");
                    recType = rs.getString("record_type");
//                    System.out.println(request_id + " " + trst + " " + line_no + " " + state);
                    byte dec[] = new byte[4000];
                    Arrays.fill(dec, (byte)8);


                    dec = ts.trimmer.trailing("IIX", _data);

                    pstm = to_conn.prepareStatement(mvr_state.toString());

                    pstm.setInt(1, request_id);
                    pstm.setDate(2, trs);
                    pstm.setInt(3, line_no);
                    pstm.setString(4, state);
                    pstm.setString(5, new String(dec));
                    pstm.setTimestamp(6, trst);
                    pstm.setString(7, recType);

                    affectedRows = pstm.executeUpdate();

//                    System.out.println("affectedRows: " + affectedRows);

//                    System.out.println("Blob length: " + data.length());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (pstm != null) pstm.close();
                if (_affectedRows >= commCnt)
                    to_conn.commit();
                to_conn.close();
                from_conn.close();
            }
        }

        return affectedRows;
    }

    private int deleteDec(int request_id, int _deletedRows, int commCnt) throws Exception {

//        String reqIds = "select * from mvr.d_mvr_requests req join mvr.d_mvr_state_data_enh sd on (req.request_id = sd.request_id)";
        StringBuilder reqIds = new StringBuilder("select * from mvr.d_mvr_requests req join mvr.d_mvr_state_data_enc sd on (req.request_id = sd.request_id) where ");
        String reqId = "select * from mvr.d_mvr_state_data_enc where request_id = " + Integer.toString(request_id);

        String s = "DBConnect.iiX." + db_to +
                "." +
                env_to;
        Connection to_conn = getConnection(s);

        String deleteReqId = "delete from mvr.d_mvr_state_data_enh where request_id = " + Integer.toString(request_id);
        to_conn.setAutoCommit(false);

        Statement stmt = null;
        ResultSet rs = null;

        PreparedStatement deletePrep = null;

        int deletedRows = 0;

        try {
            deletePrep = to_conn.prepareStatement(deleteReqId);
            deletedRows = deletePrep.executeUpdate(deleteReqId);
        } catch (SQLException sql) {
            sql.printStackTrace();
        } finally {
            if (deletePrep != null) deletePrep.close();
            if (_deletedRows >= commCnt)
                to_conn.commit();
            to_conn.close();
        }
        return deletedRows;
    }

    public void run() {
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<iix-util-dectool>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        DecToolCommand hwc = new DecToolCommand();
        int affectedRows = 0;
        int deletedRows =  0;
        if (where != null && !where.isEmpty() ) {
            StringBuilder _where = new StringBuilder();
            int j=0;
            for (int i=0; i< where.size()-1; i++) {
                _where.append(where.get(i));
                _where.append(" ");
                j=i;
            }
            j++;
            _where.append(where.get(j));
            List<Integer> rIds = hwc.getReqIds(_where.toString());

            try {
                for (int rId: rIds) {
                    if (deletedRows >= commitcnt)
                        deletedRows = 0;
                    deletedRows += hwc.deleteDec(rId, deletedRows, commitcnt);
                    if (affectedRows >= commitcnt)
                        affectedRows = 0;
                    affectedRows += hwc.updateDec(rId,affectedRows, commitcnt);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< where clause error >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Deleted rows: " + deletedRows + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Affected rows: " + affectedRows + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

    }
}
