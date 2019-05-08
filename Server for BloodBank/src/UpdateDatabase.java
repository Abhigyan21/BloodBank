import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UpdateDatabase {

    private static Connection connector() throws ClassNotFoundException, SQLException{
        Connection con = null;
        Class.forName("com.mysql.jdbc.Driver");
            do {
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bloodbank", "root", "");
                System.err.println("Error connecting to database.");
            } while (con == null);
            System.out.println("Connected to database."); 
        return con;
    }
    
    public static boolean update(String sql) throws SQLException {
        boolean success = false;
        try {
            PreparedStatement ps;
            try (Connection con = connector()) {
                ps = con.prepareStatement(sql);
                ps.executeUpdate(sql);
                ps.close();
            }
            success = true;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("UpdateDatabase.update :" + e);
            success = false;
        }
        return success;
    }
    
    public static boolean updateR(String username, String pass, String email, String num, String add, String donor, String lat, String lang) throws SQLException {
        boolean success = false;
        try {
            PreparedStatement ps;
            try (Connection con = connector()) {
                ps = con.prepareStatement("INSERT INTO USER VALUES(?,?,?,?,?,?,?,?)");
                ps.setString(1, username);
                ps.setString(2, pass);
                ps.setString(3, email);
                ps.setString(4, num);
                ps.setString(5, add);
                ps.setString(6, donor);
                ps.setString(7, lat);
                ps.setString(8, lang);
                ps.executeUpdate();
                ps.close();
            }
            success = true;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("UpdateDatabase.update :" + e);
            success = false;
        }
        return success;
    }
    
    public static ResultSet retrieve(String sql) {
        ResultSet rs = null;

        PreparedStatement ps = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = connector();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery(sql);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(sql + ": " + "UpdateDatabase.retrieve :" + e);
        }

        return rs;
    }
}
