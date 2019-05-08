import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;
import java.net.*;

public class Server extends Thread{

	/*static {
		try {
			String sql = "CREATE TABLE IF NOT EXISTS USER(name VARCHAR(20), password VARCHAR(20), email VARCHAR(20), number VARCHAR(20), address VARCHAR(30), donor VARCHAR(5))";

			if (!UpdateDatabase.update(sql)) {
				System.out.println("Error creating table User");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}*/

	ServerSocket server;
	Socket socket;

	public Server() {
		try {
			server = new ServerSocket(8000);
			socket = server.accept();
			if (server != null){
				System.out.println("Login server started");
				new LoginConnections(socket).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try{
				socket.close();
				server.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	public class LoginConnections extends Thread {

		private Socket socket;

		LoginConnections(Socket socket) {
			this.socket = socket;
			acceptConnections();
		}

		private void acceptConnections() {
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
                String result = in.readLine();
                System.out.println(result);
                
				String[] token = result.split(",");
				String username = token[0].trim();
				String pass = token[1].trim();
				if (username == null | pass == null)
					System.out.println("Data not received");
				else {
					ResultSet rs = UpdateDatabase.retrieve("SELECT * FROM USER;");
					boolean flag = false;

					while (rs.next()) {
						String name = rs.getString(1);
						String password = rs.getString(2);

						if (username.equals(name)) {
							if (pass.equals(password)) {
								flag = true;
								break;
							}
						} else {
							flag = false;
						}
					}

					if (flag == true) {
						System.out.println("Flag True");
						out.write("SUCCESS\n");
						out.flush();
						out.close();
						in.close();
					} else {
						System.out.println("Flag False");
						out.write("FAILURE\n");
						out.flush();
						out.close();
						in.close();
					}
				}
			} catch (IOException | SQLException e) {
				e.printStackTrace();
			}finally{
				try{
					socket.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
	}
}
