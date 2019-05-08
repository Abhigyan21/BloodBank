import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServerM extends Thread{
	ServerSocket server;
	Socket socket;

	public ServerM() {
		try {
			server = new ServerSocket(8002);
			socket = server.accept();
			if (server != null){
				System.out.println("Marker server started");
				new MarkerConnections(socket).start();
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

	public class MarkerConnections extends Thread {

		private Socket socket;

		MarkerConnections(Socket socket) {
			this.socket = socket;
			acceptConnections();
		}

		private void acceptConnections() {
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
                String result = in.readLine();
                System.out.println(result);
                
				if (result.equals("RETRIEVE")){
					ResultSet rs = UpdateDatabase.retrieve("SELECT * FROM USER;");
					StringBuilder builder = new StringBuilder();
					
					while (rs.next()) {
						builder.append(rs.getString(1) + ",");
						builder.append(rs.getString(5) + ",");
						builder.append(rs.getString(3) + ",");
						builder.append(rs.getString(4) + ",");
						builder.append(rs.getString(7) + ",");
						builder.append(rs.getString(8) + "\n");
						out.write(builder.toString());
						out.flush();
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
