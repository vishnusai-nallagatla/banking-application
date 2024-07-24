import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Admindashboard")
public class Admindashboard extends HttpServlet {
    private static final long serialVersionUID = 1L;

   
    private static final String INSERT_QUERY = "INSERT INTO admin_dashboard(full_name, address, mobile_no, email, account_type, balance, dob, account, id_proff, id_proff_number) VALUES(?,?,?,?,?,?,?,?,?,?)";
    private static final String PASSWORD_MANAGEMENT_INSERT_QUERY = "INSERT INTO password_management(username, password, email) VALUES(?,?,?)";
    private static final String CHECK_EXISTENCE_QUERY = "SELECT COUNT(*) FROM admin_dashboard WHERE email = ? OR mobile_no = ? OR id_proff_number = ?";


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter pw = response.getWriter();
        String full_name = request.getParameter("full_name");
        String address = request.getParameter("address");
        String mobile_no = request.getParameter("mobile_no");
        String email = request.getParameter("email");
        String account_type = request.getParameter("account_type");
        String balanceStr = request.getParameter("balance");
        double balance = Double.parseDouble(balanceStr);
        String dob = request.getParameter("dob");
        String id_proff = request.getParameter("id_proff");
        String id_proff_number = request.getParameter("id_proff_number");

        // Generate account number
        String accountNo = generateAccountNumber();

        // Use date of birth as temporary password
        String tempPassword = dob;

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/database", "root", "vishnu@2003");
            con.setAutoCommit(false); 
            
            
            ps = con.prepareStatement(CHECK_EXISTENCE_QUERY);
            ps.setString(1, email);
            ps.setString(2, mobile_no);
            ps.setString(3, id_proff_number);
            rs = ps.executeQuery();
            rs.next();
            int existingCount = rs.getInt(1);
            if (existingCount > 0) {
                pw.println("<html><head>");
                pw.println("<script src=\"https://cdn.jsdelivr.net/npm/sweetalert2@11\"></script>");
                pw.println("<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/sweetalert2@11/dist/sweetalert2.min.css\">");
                pw.println("</head><body>");
                pw.println("<script>");
                pw.println("Swal.fire('Failed to register customer.', 'Email, mobile number, or ID proof number already exists.', 'error').then(() => { window.location.href='Admindashboard.html'; });");
                pw.println("</script>");
                pw.println("</body></html>");
                return;
            }


            
            ps = con.prepareStatement(INSERT_QUERY);
            ps.setString(1, full_name);
            ps.setString(2, address);
            ps.setString(3, mobile_no);
            ps.setString(4, email);
            ps.setString(5, account_type);
            ps.setDouble(6, balance);
            ps.setString(7, dob);
            ps.setString(8, accountNo); 
            ps.setString(9, id_proff);
            ps.setString(10, id_proff_number);
            int count = ps.executeUpdate();

            if (count > 0) {
                
                ps = con.prepareStatement(PASSWORD_MANAGEMENT_INSERT_QUERY);
                ps.setString(1, full_name); 
                ps.setString(2, dob); 
                ps.setString(3, email);
                int passwordCount = ps.executeUpdate();
                
                if (passwordCount > 0) {
                    con.commit(); 
                    pw.println("<html><head>");
                    pw.println("<script src=\"https://cdn.jsdelivr.net/npm/sweetalert2@11\"></script>");
                    pw.println("<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/sweetalert2@11/dist/sweetalert2.min.css\">");
                    pw.println("</head><body>");
                    pw.println("<script>");
                    pw.println("Swal.fire({");
                    pw.println("  title: 'Customer registered successfully',");
                    pw.println("  text: 'Account number: " + accountNo + ", Temporary password: " + dob + "',");
                    pw.println("  icon: 'success'");
                    pw.println("}).then(() => { window.location.href='Admindashboard.html'; });");
                    pw.println("</script>");
                    pw.println("</body></html>");
                } else {
                    pw.println("<html><head>");
                    pw.println("<script src=\"https://cdn.jsdelivr.net/npm/sweetalert2@11\"></script>");
                    pw.println("<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/sweetalert2@11/dist/sweetalert2.min.css\">");
                    pw.println("</head><body>");
                    pw.println("<script>");
                    pw.println("Swal.fire({");
                    pw.println("  title: 'Failed to register customer',");
                    pw.println("  icon: 'error'");
                    pw.println("}).then(() => { window.location.href='Admindashboard.html'; });");
                    pw.println("</script>");
                    pw.println("</body></html>");
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            pw.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
