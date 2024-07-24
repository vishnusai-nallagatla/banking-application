import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/TransactionHistoryServlet")
public class TransactionHistoryServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Database connection parameters
        String url = "jdbc:mysql://localhost:3306/database";
        String user = "root";
        String passwordDB = "vishnu@2003";

        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement p = null;
        ResultSet r = null;
        ResultSet rs = null;

        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to the database
            conn = DriverManager.getConnection(url, user, passwordDB);

            HttpSession session = request.getSession();
            String username = (String) session.getAttribute("username");

            if (username != null) {
                out.println(username);

                // Retrieve account number for the user
                pstmt = conn.prepareStatement("SELECT * FROM admin_dashboard WHERE full_name = ?");
                pstmt.setString(1, username);
                r = pstmt.executeQuery();

                if (r.next()) {
                    String accountNumber = r.getString("account");

                    // Retrieve transaction history for the account number
                    p = conn.prepareStatement("SELECT * FROM transaction_history WHERE account_number = ?");
                    p.setString(1, accountNumber);
                    rs = p.executeQuery();

                    out.println("<!DOCTYPE html>");
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<meta charset=\"UTF-8\">");
                    out.println("<title>Transaction History</title>");
                    out.println("<style>");
                    out.println("table { border-collapse: collapse; width: 100%; }");
                    out.println("th, td { border: 1px solid #dddddd; text-align: left; padding: 8px; }");
                    out.println("tr:nth-child(even) { background-color: #f2f2f2; }");
                    out.println("</style>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h2>Transaction History</h2>");
                    out.println("<table>");
                    out.println("<tr><th>Transaction ID</th><th>Transaction Type</th><th>Amount</th><th>Current Balance</th><th>Transaction Date</th></tr>");

                    while (rs.next()) {
                        out.println("<tr>");
                        out.println("<td>" + rs.getInt("transaction_id") + "</td>");
                        out.println("<td>" + rs.getString("transaction_type") + "</td>");
                        out.println("<td>" + rs.getDouble("amount") + "</td>");
                        out.println("<td>" + rs.getDouble("balance") + "</td>");
                        out.println("<td>" + rs.getTimestamp("transaction_date") + "</td>");
                        out.println("</tr>");
                    }

                    out.println("</table>");
                    out.println("</body>");
                    out.println("</html>");
                } else {
                    out.println("No account found for the user.");
                }
            } else {
                out.println("User not logged in.");
            }
        } catch (Exception e) {
            out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace(out); // Print stack trace to the response for debugging
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (r != null) r.close();
                if (p != null) p.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace(out);
            }
        }
    }
}
