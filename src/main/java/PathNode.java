class PathNode {
  private boolean left;
  private byte[] hash;

  PathNode(boolean left, byte[] hash) {
    this.left = left;
    this.hash = hash;
  }

  boolean isLeft() {
    return left;
  }

  byte[] getHash() {
    return hash;
  }
}
