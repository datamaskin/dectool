package iix.util;

import iixToolkit.db.connection.DBConnection;
import io.micronaut.configuration.picocli.PicocliRunner;

import org.w3c.dom.Node;
import org.w3c.dom.Text;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import util.toolkit.stringtools.Trimmer;
import util.toolkit.stringtools.exceptions.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.sql.*;
import java.sql.Date;
import java.util.*;

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

    @Option(names = {"-f", "--from "}, paramLabel = "db_from", description = "The database to read from  (default: ${DEFAULT-VALUE})", required = true)
    static String db_from = "MVR";

    @Option(names = {"-e", "--env_from"}, paramLabel = "env_from", description = "The environment of the source database (default: ${DEFAULT-VALUE})", required = true)
    static String env_from = "TEST";

    @Option(names = {"-t", "-to"}, paramLabel = "db_to", description = "The database to write to", required = true)
    static String db_to = "MVR_IN";

    @Option(names = {"-E", "-env_to"}, paramLabel = "env_to", description = "The database environment to write to", required = true)
    static String env_to = "TEST";

    @Option(names = {"-s", "-fetchSize"}, paramLabel = "fsize", description = "The row fetch first size (default: ${DEFAULT-VALUE})")
    static int fsize = 100;

    @Option(names = {"-c", "-commitCount"}, paramLabel = "commitcnt", description = "The request_id transaction count before commit (default: ${DEFAULT-VALUE})")
    static int commitcnt = 500;

    @Option(names = {"-m", "-multiple"}, description = "Query multiple line no. per request id (default: ${DEFAULT-VALUE})")
    static char multiple = 'n';

    /*@Option(names = {"-v", "--verbose"}, description = "Tool description details")
    boolean verbose=false;*/

    private static Trimmer trimmer = null;

    public static byte[] hex2Byte(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    static Trimmer getTrimmer() {
        if(trimmer == null) {
            try {
                trimmer = new Trimmer("IIX");
            } catch (InvalidInputException | InitializationException e) {
                throw new RuntimeException("Failure to initialize Trimmer", e);
            }
        }
        return trimmer;
    }

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

       urlList.add(url_[4].split("\\)", 2)[0].split("=", 2)[1]);
       urlList.add(url_[5].split("\\)", 2)[0].split("=", 2)[1]);
       urlList.add(url_[7].split("\\)", 2)[0].split("=", 2)[1]);

       _url = _url + url_[4].split("\\)", 2)[0].split("=", 2)[1] + ":" +
                            (url_[5].split("\\)", 2)[0].split("=", 2)[1] + "/" +
                                    url_[7].split("\\)", 2)[0].split("=", 2)[1]);

        return _url;
    }

    public static void getOptions(CommandLine.ParseResult parseResult) {
        DecToolCommand.fsize = parseResult.matchedOptionValue('s', 100);
        DecToolCommand.db_from = parseResult.matchedOptionValue('f', "MVR");
        DecToolCommand.env_from = parseResult.matchedOptionValue('e', "TEST");
        DecToolCommand.db_to = parseResult.matchedOptionValue('t', "MVR");
        DecToolCommand.env_to = parseResult.matchedOptionValue('E', "TEST");
        DecToolCommand.commitcnt = parseResult.matchedOptionValue('c', 500);
        DecToolCommand.multiple = parseResult.matchedOptionValue('m', 'n');
    }

    public static void main(String[] args) throws Exception {
        try {
            CommandLine.ParseResult parseResult = new CommandLine(DecToolCommand.class).parseArgs(args);
            if (!CommandLine.printHelpIfRequested(parseResult)) {
                System.out.println(Arrays.toString(args));
                System.out.println("Fetch size has matched option?: " + parseResult.hasMatchedOption('s'));
                System.out.println(parseResult.matchedOption('s'));
                System.out.println(parseResult.originalArgs());
                getOptions(parseResult);
                /*DecToolCommand.fsize = parseResult.matchedOptionValue('s', 100);
                DecToolCommand.db_from = parseResult.matchedOptionValue('f', "MVR");
                DecToolCommand.env_from = parseResult.matchedOptionValue('e', "TEST");
                DecToolCommand.db_to = parseResult.matchedOptionValue('t', "MVR");
                DecToolCommand.env_to = parseResult.matchedOptionValue('E', "TEST");
                DecToolCommand.commitcnt = parseResult.matchedOptionValue('c', 500);
                DecToolCommand.multiple = parseResult.matchedOptionValue('m', true);*/
                System.out.println("Matched fsize value: " + fsize);
                Map env = new HashMap();
                env.put("TRIM_CONFIG_PATH","c:\\utils\\TrimConfig.properties");
                setEnv(env);
            }
        } catch (CommandLine.ParameterException ex) { // command line arguments could not be parsed
            System.err.println(ex.getMessage());
            ex.getCommandLine().usage(System.err);
        }
        PicocliRunner.run(DecToolCommand.class, args);
    }

    java.sql.Connection getConnection(String inconn) {

        java.sql.Connection conn = null;

        try {
            System.out.println(String.format("Creating DB Connection: %s",inconn));
            conn =  DBConnection.CreateConnection(inconn);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to create DB Connection: %s",inconn), e);
        }
        return conn;
    }

    List<Integer> getReqIds(Connection connection, String where) {

        StringBuilder encSelect = new StringBuilder("select * from mvr.d_mvr_requests req where ");
        StringBuilder _encSelect = new StringBuilder("select * from mvr.d_mvr_state_data_enc req where ");
//        StringBuilder _encSelect = new StringBuilder("select * from mvr.d_mvr_state_data_enc where request_id = 659460165 and line_no >= 1");

        DecToolCommand dtc = new DecToolCommand();

        Statement stmt = null;
        ResultSet rs = null;

        encSelect.append(where);
        _encSelect.append(where);

        if (DecToolCommand.multiple == 'y') {
            _encSelect.append(" and line_no >= 1");
        }

        if (fsize > 0) {
            encSelect.append(" FETCH FIRST ");
            encSelect.append(fsize);
            encSelect.append(" ROWS ONLY");
        }

        List<Integer> l_reqids = new ArrayList<>();
        List<Integer> l_lineno = new ArrayList<>();
        int count = 0;

        try {
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            if (multiple == 'y') {
                rs = stmt.executeQuery(_encSelect.toString());
            } else {
                rs = stmt.executeQuery(encSelect.toString());
            }
            while (rs.next()) {
                count++;
                l_lineno.add(rs.getInt("line_no"));
                int reqTmp = rs.getInt("request_id");
                if (!l_reqids.contains(reqTmp)) {
                    l_reqids.add(reqTmp);
                }
            }
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Number of rows : "+count+" <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            /*if (multiple == 'n' && count > 1) {
                throw new InvalidInputException("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Multiple line_no found " + count + " for no multiple input >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }*/
        } catch (SQLException e) {
            throw new RuntimeException("Failure to read the list of request IDs to copy", e);
        } finally {
            try {
                if(rs != null) { rs.close(); };
                if(stmt != null) { stmt.close(); };
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(-5);
            }
        }

        return l_reqids;
    }

    /*private byte[] getDec(int request_id) {
        DecTool decTool = entityManager.find(DecTool.class, request_id);
        return decTool.getDec();
    }*/

    private int updateDec(int request_id, Connection toConnection, Connection fromConnection, int commCnt) {

        StringBuilder encSelect = new StringBuilder("select * from mvr.d_mvr_state_data_enc where request_id = " + Integer.toString(request_id));

        if (multiple == 'y') {
            encSelect.append(" and line_no >= 1");
        }

        int affectedRows = 0;
        if (request_id >= 0) {

            try {
                toConnection.setAutoCommit(false);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            Statement stmt = null;
            ResultSet rs = null;

            Blob data = null;
            byte[] _data = null;
            Timestamp trst = null;
            Date trs = null;
            int line_no = 0;
            String state = "";
            String recType = "";

            StringBuilder mvr_state = new StringBuilder("insert into MVR.D_MVR_STATE_DATA_ENH(request_id, time_report_start, line_no, state, data, time_report_start_ts,record_type) values(?,?,?,?,?,?,?)");
            PreparedStatement pstm = null;

            try {
                stmt = fromConnection.createStatement();
                rs = stmt.executeQuery(encSelect.toString());

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


                    dec = getTrimmer().trailing("IIX", _data);

                    pstm = toConnection.prepareStatement(mvr_state.toString());

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
                throw new RuntimeException("Failed to process data rows", e);
            } finally {
                try {
                    if(rs != null) { rs.close(); };
                    if(pstm != null ) { pstm.close(); }
                    if(stmt != null ) { stmt.close(); }
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.exit(-3);
                }
            }
        }

        return affectedRows;
    }

    public String getDbString(String db, String env) {
        StringBuilder s = new StringBuilder("DBConnect.iiX.");
        s.append(db);
        s.append(".");
        s.append(env);
        return s.toString();
    }

    private int deleteDec(int request_id, Connection connection, int commCnt) {

        StringBuilder deleteReqId = new StringBuilder("delete from mvr.d_mvr_state_data_enh where request_id = " + Integer.toString(request_id));

        if (multiple == 'y') {
            deleteReqId.append(" and line_no >= 1");
        }

        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        PreparedStatement deletePrep = null;

        int deletedRows = 0;

        try {
            deletePrep = connection.prepareStatement(deleteReqId.toString());
            deletedRows = deletePrep.executeUpdate(deleteReqId.toString());
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Failed to delete data for request_id $s",Integer.toString(request_id)),e);
        } finally {
            try {
                if(deletePrep != null) { deletePrep.close(); }
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(-4);
            }
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
            for (int i=0; i < where.size(); i++) {
                _where.append(where.get(i));
                _where.append(" ");
            }

            String strtmp =_where.toString().trim();

            Connection toConnection = null;
            Connection fromConnection = null;

            try {
                toConnection = getConnection(getDbString(db_to,env_to));
                fromConnection = getConnection(getDbString(db_from,env_from));

                List<Integer> rIds = hwc.getReqIds(fromConnection,strtmp);
                int i = 0;

                for (int rId: rIds) {
                    deletedRows += hwc.deleteDec(rId, toConnection, commitcnt);
                    affectedRows += hwc.updateDec(rId,toConnection, fromConnection, commitcnt);
                    i++;
                    if(i >= commitcnt) {
                        toConnection.commit();
                        i = 0;
                    }
                }
                toConnection.commit();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                if(toConnection != null ){
                    try {
                        toConnection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
                if(fromConnection != null ){
                    try {
                        fromConnection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.exit(-2);
                    }
                }
            }
        } else {
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< where clause error >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Deleted rows: " + deletedRows + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Affected rows: " + affectedRows + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

    }

    private static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }

}
