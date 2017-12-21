import java.util.*;

/**
 * 15パズルの解法についてA*探索及びIDA*探索を行う。
 * その際の実行時間と探索状態数を測定する。
 */
public class SlidePuzzle{
  public static void main(String[] args){
    int[][] testcase = {{2,8,3},{1,6,4},{7,0,5}};
    int ans=0;
    
    for (int i=0;i<6;i++){
      Search s;
      if (i<3) s = new AstarSearch(testcase,2-i);
      else s = new IDAstarSearch(testcase,5-i);

      if (i==0){
        ans = s.getAns();
        System.out.print(ans);
      }

      if (ans != s.getAns()) System.out.print(",ERR,ERR");
      else System.out.print(","+s.getCnt()+","+s.getTime());
    }
    System.out.println();
  }
}

/**
 * 探索の抽象クラス。
 * 測定関係の関数などをここで定義している。
 */
abstract class Search{
  protected int cnt = 0; // 探索状態数
  protected int ans = -1; // 最短手数
  private long startTime; // 開始時刻
  private long reqTime = -1; // 所要時間[ns]
  private static final long timeLimit = 3000000000L; // 3 [s]

  // アクセサ
  public int getCnt(){ return cnt; }
  public int getAns(){ return ans; }
  public long getTime(){ return reqTime; }
  
  // 探索を行う関数本体。測定の対象となる。
  abstract protected void launch();

  // launch()を実行し、時間を測定する。
  protected void run(){
    this.startTime = System.nanoTime();
    launch();
    this.reqTime = timeNow();
  }

  // 現在までの実行時間を返す。
  private long timeNow(){
    return System.nanoTime() - this.startTime;
  }

  // 指定のタイムリミットに達しているかを返す。
  protected boolean timeover(){
    return timeNow() >= timeLimit;
  }
}

// IDA*探索
class IDAstarSearch extends Search{
  private int[][] start; // 開始状態
  private int hnum; // 使用するヒューリスティック関数

  IDAstarSearch(int[][] start, int hnum){
    this.start = start;
    this.hnum = hnum;
    run();
  }

  protected void launch(){
    // Step1
    Node startNode = new Node(start, hnum);
    int limit = startNode.h();
    if (startNode.isGoal()) { // スタートがゴール
      ans = 0;
      return;
    }
    while(true){
      // Step2
      Stack<Node> st = new Stack<Node>();
      st.push(new Node(start, hnum));
      while (true){
        if (timeover()) { // 時間切れ
          ans = -1;
          return;
        }
        // Step3
        if (st.empty()){
          // スタックが空なので再スタート
          limit++;
          break;
        } else {
          Node n = st.peek();
          if (n.getNext() == 4){
            // 子ノードすべて展開済み
            st.pop();
            continue;
          }
          Node n1 = n.move(n.getNext()); // 子ノード
          n.incNext();
          if (n1 == null) continue;
          cnt++;
          if (n1.isGoal()) { // 探索成功
            ans = n1.getG();
            return;
          }
          if (n1.getCost() <= limit){
            st.push(n1);
          }
        }
      }
    }
  }
}

class AstarSearch extends Search{
  private PriorityQueue<Node> open;
  private ArrayList<Node> closed;

  AstarSearch(int[][] start, int hnum){
    open = new PriorityQueue<Node>(Comparator.comparing(Node::getCost));
    closed = new ArrayList<Node>();
    // Step1
    open.add(new Node(start,hnum));
    run();
  }

  protected void launch(){
    while(true){
      // Step2
      Node n = open.poll();
      if (n == null || timeover()) { // 時間切れ
        ans = -1;
        return;
      }
      if (n.isGoal()) { // 探索成功
        ans = n.getG();
        return;
      }
      // Step3
      closed.add(n);
      for (int i=0; i<4; i++){
        Node n1 = n.move(i);
        if (n1 == null) continue;
        cnt++;
        int prev = closed.indexOf(n1);
        if (prev == -1){
          // n'がclosedリストにない
          if (open.contains(n1)){
            // n'がopenリストにある
            Node prevNode = null;
            for (Node t: open){
              if (t.equals(n1)){
                prevNode = t;
                break;
              }
            }
            if (prevNode.getCost()>n1.getCost()){
              open.remove(prevNode);
              open.add(n1);
            }
          } else {
            // n'がopenリストにない
            open.add(n1);
          }
        } else {
          // n'がclosedリストにある
          Node prevNode = closed.get(prev);
          if (prevNode.getCost()>n1.getCost()){
            closed.remove(prev);
            open.add(n1);
          }
        }
      }
    }
  }
}

// 状態ノード
class Node{
  private static final int[] dx = {0,-1,1,0};
  private static final int[] dy = {-1,0,0,1};
  private static final String[] dir = {"L","U","D","R"};

  private int board[][]; // 盤面
  public static final int size = 3;
  private static final int[][] goal = {{1,2,3},{8,0,4},{7,6,5}};
  
  private final Node p; // 親ノード
  private final Integer c; // コスト（g + h）
  private final Integer g; // ここまでに要した実コスト
  private int next = 0; // （IDA*探索で）次に展開すべき子ノード
  private final Integer hnum; // 使用するヒューリスティック関数
  private final Integer moveDir; // （空白を）動かした方向

  Node(int[][] board){
    this(board, null, null, null, null, null);
  }

  Node(int[][] board, Integer hnum){
    // make start node
    this.p = null;
    this.g = 0;
    this.hnum = hnum;
    this.board = board;
    this.c = h();
    this.moveDir = null;
  }

  Node(int[][] board, Node p, Integer c, Integer g, Integer hnum, Integer moveDir){
    this.p = p;
    this.c = c;
    this.g = g;
    this.hnum = hnum;
    this.board = board;
    this.moveDir = moveDir;
  }

  // アクセサ
  void incNext(){ next++; }
  Integer getCost(){ return c; }
  Node getParent(){ return p; }
  Integer getG(){ return g; }
  Integer getNext(){ return next; }
  Integer getDir(){ return moveDir; }

  // そのノードに至る手順を返す。
  // 導いたゴールノードに対して用いれば、最短手順を得られる。
  String trace(){
    Stack<Node> st = new Stack<Node>();
    Node t = this;
    while (t!=null){
      st.push(t);
      t = t.getParent();
    }
    String ans = "";
    st.pop();
    while (!(st.empty())){
      t = st.pop();
      ans += dir[t.getDir()];
    }
    return ans;
  }

  @Override
  public boolean equals(Object obj){
    if (this == obj) return true;
    if (obj == null) return false;
    int[][] otherboard = null;
    if (obj instanceof int[][]){
      otherboard = (int[][]) obj;
    } else if (obj instanceof Node) {
      if (size!=((Node) obj).size) return false;
      else otherboard = ((Node) obj).board;
    } else return false;

    for (int i=0;i<size;i++){
      for (int j=0;j<size;j++){
        if (board[i][j]!=otherboard[i][j]) return false;
      }
    }
    return true;
  }

  @Override
  public String toString(){
    String ret = "Node:\n";
    for (int i=0;i<size;i++){
      for (int j=0;j<size;j++){
        ret += board[i][j] + "\t";
      }
      // if (i == size - 1) break;
      ret += "\n";
    }
    ret += "p[" + p + "],c[" + c + "],g[" + g + "],d[" + moveDir + "]\n";
    return ret;
  }

  // このノードがゴールか否かを返す
  public boolean isGoal(){
    return this.equals(goal);
  }

  // 指定した方向に（空白を）動かした次のノードを返す
  public Node move(int d){
    int[][] ret = move(board,d);
    if (ret == null) return null;
    else return new Node(ret,this,h(ret,hnum)+g+1,g+1,hnum,d);
  }

  // 指定した方向に（空白を）動かした盤面を返す
  private static int[][] move(int[][] b, int d){
    int x=-1,y=-1;
    for (int i=0;i<size;i++){
      for (int j=0;j<size;j++){
        if (b[i][j]==0){
          x = i;
          y = j;
          break;
        }
      }
    }
    int x1 = x + dx[d];
    int y1 = y + dy[d];
    if (x1>=0 && x1<size && y1>=0 && y1<size){
      int[][] b1 = new int[size][size];
      for (int i=0;i<size;i++)
        for (int j=0;j<size;j++)
          b1[i][j] = b[i][j];
      b1[x][y] = b1[x1][y1];
      b1[x1][y1] = 0;
      return b1;
    } else {
      return null;
    }
  }

  // ゴールからn手動かしたテストケース盤面を作成する
  public static int[][] makeTestCase(int n){
    int[][] ret = goal;
    int prev = -1;
    Random ran = new Random();

    for (int i=0;i<n;i++){
      while (true){
        int rnd = ran.nextInt(4);
        if (rnd == prev) continue;
        int[][] n1 = move(ret,rnd);
        if (n1 == null) continue;
        ret = n1;
        prev = rnd;
        break;
      }
    }

    return ret;
  }

  int h(){
    return h(board,hnum);
  }

  // ヒューリスティック関数
  public static int h(int[][] board, int hnum){
    int ans = 0;
    switch(hnum){
    case 0:
      break;
    case 1:
      for (int i=0;i<size;i++)
        for (int j=0;j<size;j++)
          if (goal[i][j]!=0 && board[i][j]!=goal[i][j]) ans++;
      break;
    case 2:
      for (int i=0;i<size;i++){
        for (int j=0;j<size;j++){
          int b = board[i][j];
          if (b==0) continue;
          outside:
          for (int x=0;x<size;x++){
            for (int y=0;y<size;y++){
              if (goal[x][y]==b){
                ans += Math.abs(i-x)+Math.abs(j-y);
                break outside;
              }
            }
          }
        }
      }
    }
    return ans;
  }
}