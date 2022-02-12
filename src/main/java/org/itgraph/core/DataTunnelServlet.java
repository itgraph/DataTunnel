package org.itgraph.core;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.*;
import java.sql.*;

public class DataTunnelServlet implements Servlet {
    private final static Logger LOGGER = LoggerFactory.getLogger(DataTunnelServlet.class);

    private static final int fetchSize = 1000;
    private static char DELIMITER = '\001'; // Ctrl + A

    private String driverClass;
    private String url;
    private String username;
    private String password;
    private boolean includeColumnNames = true;
    private PrintWriter pw;
    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet rs;

    public static void close(AutoCloseable closeable){
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void release() {
        try{
            rs.close();
            pstmt.close();
            connection.close();
            pw.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(pstmt);
            close(connection);
        }
    }

    private void writeCsv(PrintWriter pw, ResultSet rs, boolean includeColumnNames, char delimiter) throws IOException, SQLException {
        CSVWriter writer = new CSVWriter(pw, delimiter, Character.MIN_VALUE, Character.MIN_VALUE, "\n");//separator 分隔符， quotechar引号符， escapechar 转义符
        int count = writer.writeAll(rs, includeColumnNames);
        writer.flush();
        writer.close();
        LOGGER.info(String.valueOf(count));
    }

    @Override
    public void init(ServletConfig servletConfig) {
        LOGGER.info("init()...");
        driverClass = servletConfig.getInitParameter("driverClass");
        url = servletConfig.getInitParameter("url");
        username = servletConfig.getInitParameter("username");
        password = servletConfig.getInitParameter("password");
    }

    @Override
    public ServletConfig getServletConfig() {
        LOGGER.info("getServletConfig()...");
        return null;
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws IOException {
        String sql = request.getParameter("sql").trim();
        if (request.getParameterMap().containsKey("headless")) {
            includeColumnNames = false;
        }
        response.setCharacterEncoding("UTF-8");
        try {
            pw = response.getWriter();
            Class.forName(driverClass);
            connection = DriverManager.getConnection(url, username, password);
            if(url.startsWith("jdbc:oracle")) {
                pstmt = connection.prepareStatement(sql);
                pstmt.setFetchSize(fetchSize);
            } else if(url.startsWith("jdbc:mysql")){
                pstmt = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                pstmt.setFetchSize(Integer.MIN_VALUE);
                pstmt.setFetchDirection(ResultSet.FETCH_REVERSE);
            }
            rs = pstmt.executeQuery();
            writeCsv(pw, rs, includeColumnNames, DELIMITER);
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DATABASE DRIVER NOT FOUND!");
        } finally {
            release();
        }
    }

    @Override
    public String getServletInfo() {
        LOGGER.info("getServletInfo()...");
        return "This is Data Tunnel Servlet";
    }

    @Override
    public void destroy() {
        LOGGER.info("destroy()...");
    }
}
