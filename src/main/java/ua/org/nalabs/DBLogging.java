package ua.org.nalabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DBLogging {
    private DBLogging(){}
    private static final String QUERY = "INSERT INTO sensors_data(publisher, t, temp, humidity) values(?,?,?,?)";
    private static final Logger LOGGER = LoggerFactory.getLogger(DBLogging.class.getName());

    public static void writeToDb(String publisher, long timestamp, double temp, int humidity) {
        InputStream resourcesInputStream = DBLogging.class.getClassLoader().getResourceAsStream("application.properties");
        Properties properties = new Properties();
        try {
            properties.load(resourcesInputStream);
        } catch (IOException e) {
            LOGGER.warn("Cannot read property ", e);
        }
        String dataBaseName = properties.getProperty("db.name");
        String dataBaseURL = properties.getProperty("db.url");
        String dataUserName = properties.getProperty("db.username");
        String dataUserPass = properties.getProperty("db.password");
        String dataBaseConnParam = properties.getProperty("db.connparams");
        String dbUrl = dataBaseURL + dataBaseName + dataBaseConnParam;

        try(Connection connection = DriverManager.getConnection(dbUrl, dataUserName, dataUserPass);
            PreparedStatement preparedStatement = connection.prepareStatement(QUERY);
        ) {
            preparedStatement.setString(1, publisher);
            preparedStatement.setDate(2, new java.sql.Date(timestamp));
            preparedStatement.setDouble(3, temp);
            preparedStatement.setInt(4, humidity);
            preparedStatement.execute();
        } catch (SQLException s) {
                LOGGER.warn("statement was not executed",s);
        }
        }
}
