import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Client {
  public static void main(String[] args)
      throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    String address = args.length > 0 ? args[0] : "localhost";
    Socket socket = new Socket(address, 0xBAD);
    DataInputStream in = new DataInputStream(socket.getInputStream());
    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    int blockSize = args.length > 1 ? Integer.parseInt(args[1]) : 1024;
    out.writeInt(blockSize);
    String hashFunction = args.length > 2 ? args[2] : "SHA-256";
    MessageDigest digest = MessageDigest.getInstance(hashFunction);
    out.writeUTF(hashFunction);
    Scanner scanner = new Scanner(System.in);
    byte[] topHash = new byte[0];
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    SecretKey key = keyGenerator.generateKey();
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    CipherInputStream cipherIn = new CipherInputStream(socket.getInputStream(), cipher);
    CipherOutputStream cipherOut = new CipherOutputStream(socket.getOutputStream(), cipher);
    commandLoop:
    while (true) {
      System.out.print(">");
      String[] command = scanner.nextLine().split("\\s");
      switch (command[0]) {
        case "exit":
          out.writeUTF("exit");
          break commandLoop;
        case "read":
          out.writeUTF("read");
          out.writeInt(Integer.parseInt(command[1]));
          byte[] data = new byte[in.readInt()];
          in.read(data);
          int pathSize = in.readInt();
          byte[] currentHash = digest.digest(data);
          digest.reset();
          for (int i = 0; i < pathSize; i++) {
            boolean isLeft = in.readBoolean();
            byte[] hash = new byte[currentHash.length];
            in.read(hash);
            if (isLeft) {
              digest.update(hash);
              currentHash = digest.digest(currentHash);
              digest.reset();
            } else {
              digest.update(currentHash);
              currentHash = digest.digest(hash);
              digest.reset();
            }
          }
          if (!Arrays.equals(currentHash, topHash)) {
            System.out.println("The server responded incorrectly!");
          } else {
            cipher.init(Cipher.DECRYPT_MODE, key);
            data = cipher.doFinal(data);
            System.out.println(new String(data));
          }
          break;
        case "write":
          out.writeUTF("write");
          cipher.init(Cipher.ENCRYPT_MODE, key);
          data = cipher.doFinal(command[1].getBytes());
          out.writeInt(data.length);
          out.write(data);
          out.writeInt(Integer.parseInt(command[2]));
          pathSize = in.readInt();
          currentHash = digest.digest(data);
          digest.reset();
          for (int i = 0; i < pathSize - 1; i++) {
            boolean isLeft = in.readBoolean();
            byte[] hash = new byte[currentHash.length];
            in.read(hash);
            if (isLeft) {
              digest.update(hash);
              currentHash = digest.digest(currentHash);
              digest.reset();
            } else {
              digest.update(currentHash);
              currentHash = digest.digest(hash);
              digest.reset();
            }
          }
          in.readBoolean();
          topHash = new byte[currentHash.length];
          in.read(topHash);
          if (!Arrays.equals(currentHash, topHash)) {
            System.out.println("The server responded incorrectly!");
          }
          break;
        default:
          System.out.println("The command " + command[0] + " does not exist.");
      }
    }
    socket.close();
  }
}
