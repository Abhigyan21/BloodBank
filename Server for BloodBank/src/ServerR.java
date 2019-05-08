import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class ServerR{
	static {
		try {
			String sql = "CREATE TABLE IF NOT EXISTS USER(name VARCHAR(20), password VARCHAR(20), email VARCHAR(20), number VARCHAR(20), address VARCHAR(30), donor VARCHAR(10), lat VARCHAR(20), lng VARCHAR(20))";
			
			if (!UpdateDatabase.update(sql)) {
				System.out.println("Error creating table User");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	ServerSocket server;
	Socket socket;

	public ServerR() {
		try {
			server = new ServerSocket(8001);
			socket = server.accept();
			if (server != null){
				System.out.println("Registration server started");
				new RegistrationConnections(socket).start();
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

	public class RegistrationConnections extends Thread {
		Socket socket;

		RegistrationConnections(Socket socket) {
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
				String email = token[2].trim();
				String address = token[3].trim();
				String number = token[4].trim();
				String isInterested = token[5].trim();
				String lat = token[6].trim();
				String lang = token[7].trim();

				if (username == null | pass == null | email == null | address == null | isInterested == null || lat == null || lang == null)
					System.out.println("Data not received");
				else {
					if (UpdateDatabase.updateR(username, pass, email, address, number, isInterested, lat, lang)) {
						out.write("SUCCESS\n");
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
