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
import java.util.List;

@Command(name = "DecToolCommand",
        header = {
                "@|green  __                ___      |@",
                "@|green |  \\ _ _ _   _ |_   | _  _ ||@",
                "@|green |__/(-(_| \\/|_)|_   |(_)(_)||@",
                "@|green           / |               |@"},
        description = "Micronaut dectool", mixinStandardHelpOptions = true)
public class DecToolCommand implements Runnable {

    @Option(names = {"-o","--ora_messenger"},    paramLabel = "FILE", description = "Path to datasource ora_messenger.xml definition (default: ${DEFAULT-VALUE})", required = true)
    File f = new File("c://utils/ora_messenger.xml");

    @Option(names = {"-f", "--from "}, paramLabel = "db_from", description = "The database to read from  (default: ${DEFAULT-VALUE})", required = true)
    String db_from = "MVR";

    @Option(names = {"-e", "--env_from"}, paramLabel = "env_from", description = "The environment of the source database (default: ${DEFAULT-VALUE})", required = true)
    String env_from = "TEST";

    @Option(names = {"-t", "-to"}, paramLabel = "db_to", description = "The database to write to", required = true)
    String db_to = "";

    @Option(names = {"-E", "-env_to"}, paramLabel = "env_to", description = "The database environment to write to", required = true)
    String env_to = "";

    @Option(names = {"-q", "-request_ids"}, split=",", paramLabel = "request_ids", description = "The list of request ids to return in the select", required = false)
    int[] request_ids ;

    @Option(names = {"-v", "--verbose"}, description = "Shows some project details")
    boolean verbose;

    @Option(names = {"-w", "-where"}, description = "Decryption where clause", required = false)
    String where = "";

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

    private java.sql.Connection getConnection(OraMessengerBean omb) {

        java.sql.Connection conn = null;
        try {
            conn =  DBConnection.CreateConnection(omb.get_javaURL(), omb.getUser(), omb.getEncpPswd());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    java.sql.Connection getConnection(String dbname, String envname) {

        java.sql.Connection conn = null;
        StringBuilder connectionString = new StringBuilder("iiX.");
        connectionString.append(dbname);
        connectionString.append(".");
        connectionString.append(envname);

        try {
            conn =  DBConnection.CreateConnection(connectionString.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

   private int updateEnc(int ...args) {
       for (int arg : args) {
           System.out.println("args: " + arg);
       }
       return 0;
   }

    public void run() {
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<iix-util>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        DecToolCommand hwc = new DecToolCommand();
        hwc.updateEnc(request_ids);

        OraMessengerBean from_omb = parseOraMessenger(f, db_from);
        OraMessengerBean to_omb = parseOraMessenger(f, db_to);

       Connection from_conn = getConnection(from_omb);
       Connection to_conn = getConnection(to_omb);

        /*try {
            String encPswd = PasswordEncryption.encrypt("lutefisk");
//                System.out.println(encPswd);
            Class.forName("org.postgresql.Driver").newInstance();
            _conn =  DBConnection.CreateConnection("jdbc:postgresql://localhost:5433/postgres", "postgres", encPswd);

        } catch (Exception e) {
            e.printStackTrace();
        }*/

        String mvrEnc = "SELECT * FROM mvr.d_mvr_state_data_enc order by request_id, line_no ";

//       StringBuilder encSelect = new StringBuilder("SELECT * from mvr.d_mvr_state_data_enc");
       StringBuilder encSelect = new StringBuilder("select * from mvr.d_mvr_requests req join mvr.d_mvr_state_data_enc sd on (req.request_id = sd.request_id) WHERE sd.request_id = ");
       String orderBy = " order by request_id, line_no";
//       StringBuilder whereReqId = new StringBuilder(" where request_id = ");
//       whereReqId.append("'");
//       whereReqId.append(request_ids[0]);
//       whereReqId.append("'");
//       encSelect.append(whereReqId);
        encSelect.append("'");
        encSelect.append(request_ids[0]);
        encSelect.append("'");

       System.out.println("encSelect: " + encSelect.toString());
        Statement stmt = null;
        ResultSet rs = null;

        Blob data = null;
        byte[] _data = null;
        int request_id = 0;
        Timestamp ts = null;
        int line_no = 0;
        String state = "";
        StringBuilder mvr_state =  new StringBuilder("insert into mvr.d_mvr_state_data(request_id, time_report_start, line_no, state, data) values(?,?,?,?,?)");

        try  {
            stmt = from_conn.createStatement();
            rs = stmt.executeQuery(encSelect.toString());
            while ( rs.next() ) {

                data =  rs.getBlob("DATA");
                _data = data.getBytes(1, (int) data.length());
                request_id = rs.getInt("request_id");
                ts = rs.getTimestamp("time_report_start");
                line_no = rs.getInt("line_no");
                state = rs.getString("state");

                //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

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

                //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

                // request_id, time_report_start, line_no, state, data
                PreparedStatement pstm = to_conn.prepareStatement(mvr_state.toString());

                pstm.setInt(1, request_id);
                pstm.setTimestamp(2, ts);
                pstm.setInt(3, line_no);
                pstm.setString(4, state);
                pstm.setString(5, new String(dec));

                int affectedRows = pstm.executeUpdate();

                System.out.println("affectedRows: " + affectedRows);

                System.out.println("Blob length: " + data.length());
                System.out.println(new String(_data));
            }

            to_conn.commit();
            to_conn.close();
            from_conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("request_ids: "  + request_ids);

        if (verbose) {
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<DecToolCommand details>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
    }
}
