import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Server {
  public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    ServerSocket server = new ServerSocket(0xBAD);
    Socket socket = server.accept();
    DataInputStream in = new DataInputStream(socket.getInputStream());
    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    MerkleTree tree = new MerkleTree(in.readInt(), in.readUTF());
    commandLoop:
    while (true) {
      String command = in.readUTF();
      switch (command) {
        case "exit":
          break commandLoop;
        case "read":
          int index = in.readInt();
          byte[] data = tree.read(index);
          out.writeInt(data.length);
          out.write(data);
          List<PathNode> path = tree.generatePath(index);
          out.writeInt(path.size());
          for (PathNode node : path) {
            out.writeBoolean(node.isLeft());
            out.write(node.getHash());
          }
          break;
        case "write":
          data = new byte[in.readInt()];
          in.read(data);
          index = in.readInt();
          tree.write(index, data);
          path = tree.generatePathWithTopNode(index);
          out.writeInt(path.size());
          for (PathNode node : path) {
            out.writeBoolean(node.isLeft());
            out.write(node.getHash());
          }
          break;
        default:
      }
    }
    socket.close();
    server.close();
  }
}
