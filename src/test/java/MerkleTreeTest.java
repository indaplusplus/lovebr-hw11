import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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

  @Test
  void treeWithOddBlockCountHasCorrectTopNode() throws NoSuchAlgorithmException {
    int blockSize = 1024;
    String hashFunction = "SHA-256";
    byte[][] blocks = new byte[3][blockSize];
    Random random = new Random();
    MerkleTree tree = new MerkleTree(blockSize, hashFunction);
    for (byte[] block : blocks) {
      random.nextBytes(block);
      tree.write(tree.size(), block);
    }
    MessageDigest digest = MessageDigest.getInstance(hashFunction);
    byte[] hash0 = digest.digest(blocks[0]);
    digest.reset();
    byte[] hash1 = digest.digest(blocks[1]);
    digest.reset();
    byte[] hash2 = digest.digest(blocks[2]);
    digest.reset();
    digest.update(hash0);
    byte[] hash01 = digest.digest(hash1);
    digest.reset();
    digest.update(hash01);
    byte[] hash012 = digest.digest(hash2);
    digest.reset();
    List<PathNode> path = tree.generatePathWithTopNode(0);
    assertArrayEquals(hash012, path.get(path.size() - 1).getHash());
  }

  @Test
  void treeWithEvenBlockCountHasCorrectTopNode() throws NoSuchAlgorithmException {
    int blockSize = 1024;
    String hashFunction = "SHA-256";
    byte[][] blocks = new byte[4][blockSize];
    Random random = new Random();
    MerkleTree tree = new MerkleTree(blockSize, hashFunction);
    for (byte[] block : blocks) {
      random.nextBytes(block);
      tree.write(tree.size(), block);
    }
    MessageDigest digest = MessageDigest.getInstance(hashFunction);
    byte[] hash0 = digest.digest(blocks[0]);
    digest.reset();
    byte[] hash1 = digest.digest(blocks[1]);
    digest.reset();
    byte[] hash2 = digest.digest(blocks[2]);
    digest.reset();
    byte[] hash3 = digest.digest(blocks[3]);
    digest.reset();
    digest.update(hash0);
    byte[] hash01 = digest.digest(hash1);
    digest.reset();
    digest.update(hash2);
    byte[] hash23 = digest.digest(hash3);
    digest.reset();
    digest.update(hash01);
    byte[] hash1234 = digest.digest(hash23);
    digest.reset();
    List<PathNode> path = tree.generatePathWithTopNode(0);
    assertArrayEquals(hash1234, path.get(path.size() - 1).getHash());
  }
}
