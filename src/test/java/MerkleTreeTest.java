import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class MerkleTreeTest {
  @Test
  void emptyTreeHasCorrectTopNode() throws NoSuchAlgorithmException {
    int blockSize = 1024;
    String hashFunction = "SHA-256";
    MerkleTree tree = new MerkleTree(blockSize, hashFunction);
    byte[] empty = new byte[blockSize];
    Arrays.fill(empty, (byte) 0);
    assertArrayEquals(
        MessageDigest.getInstance(hashFunction).digest(empty),
        tree.generatePathWithTopNode(0).get(0).getHash());
  }
}
