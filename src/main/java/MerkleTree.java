import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class MerkleTree {
  private int blockSize;
  private List<byte[]> blocks;
  private MessageDigest digest;

  MerkleTree(int blockSize, String hashFunction) throws NoSuchAlgorithmException {
    this.blockSize = blockSize;
    this.blocks = new ArrayList<>();
    this.digest = MessageDigest.getInstance(hashFunction);
  }

  byte[] read(int index) {
    return blocks.get(index);
  }

  void write(int index, byte[] data) {
    if (index < blocks.size()) {
      blocks.remove(index);
      blocks.add(index, data);
    } else if (index == blocks.size()) {
      blocks.add(data);
    } else {
      throw new ArrayIndexOutOfBoundsException();
    }
  }

  List<PathNode> generatePath(int index) {
    List<byte[]> hashes = new ArrayList<>();
    for (byte[] block : blocks) {
      hashes.add(digest.digest(block));
      digest.reset();
    }
    List<PathNode> path = new ArrayList<>();
    reduceHashesOntoPath(hashes, index, path);
    return path;
  }

  List<PathNode> generatePathWithTopNode(int index) {
    List<byte[]> hashes = new ArrayList<>();
    for (byte[] block : blocks) {
      hashes.add(digest.digest(block));
      digest.reset();
    }
    List<PathNode> path = new ArrayList<>();
    reduceHashesOntoPath(hashes, index, path);
    if (hashes.isEmpty()) {
      byte[] empty = new byte[blockSize];
      Arrays.fill(empty, (byte) 0);
      path.add(new PathNode(true, digest.digest(empty)));
      digest.reset();
    } else {
      path.add(new PathNode(false, hashes.get(0)));
    }
    return path;
  }

  private void reduceHashesOntoPath(List<byte[]> hashes, int index, List<PathNode> path) {
    while (hashes.size() > 1) {
      if (index % 2 == 1) {
        path.add(new PathNode(true, hashes.get(index - 1)));
      } else if (index + 1 < hashes.size()) {
        path.add(new PathNode(false, hashes.get(index + 1)));
      }
      index /= 2;
      for (int i = 0; i + 1 < hashes.size(); i++) {
        digest.update(hashes.remove(i));
        hashes.add(i, digest.digest(hashes.remove(i)));
        digest.reset();
      }
    }
  }

  int size() {
    return blocks.size();
  }
}
