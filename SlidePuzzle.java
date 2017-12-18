import java.util.*;

public class SlidePuzzle{

	public static void main(String[] args){
    Scanner sc = new Scanner(System.in);
    int[][] arr = new int[4][4];

    for (int i=0;i<4;i++){
      for (int j=0;j<4;j++){
        arr[i][j] = sc.nextInt();
      }
    }
    
		AstarSearch as = new AstarSearch(new Node(arr),2);
    System.out.println(as.launch());
	}
}

class AstarSearch{

  private static final int[] dx = {0,-1,1,0};
  private static final int[] dy = {-1,0,0,1};
  PriorityQueue<Tuple> open;
  ArrayList<Tuple> closed;
  int hnum;

  AstarSearch(Node n, int hnum){
    open = new PriorityQueue<Tuple>(Comparator.comparing(Tuple::getCost));
    closed = new ArrayList<Tuple>();
    this.hnum = hnum;
    // Step1
    open.add(new Tuple(n,null,n.h(hnum)));
  }

  int launch(){
    while(true){
      // Step2
      Tuple e = open.poll();
      if (e == null) return -1; // failure
      Node n = e.getNode();
      if (n.isGoal()) {
        trace(e);
        return e.getG();
      } // success
      // Step3
      closed.add(e);
      for (int i=0; i<4; i++){
        Node n1 = n.move(dx[i],dy[i]);
        if (n1 == null) continue;
        int g = e.getG();
        Tuple e1 = new Tuple(n1, e, n1.h(hnum)+g+1, g+1);
        int prev = closed.indexOf(e1);
        // n' is not in closed
        if (prev == -1){
          // n' is in open
          if (open.contains(e1)){
            Tuple e_prev = null;
            for (Tuple t: open){
              if (t.equals(e1)){
                e_prev = t;
                break;
              }
            }
            if (e_prev.getCost()>e1.getCost()){
              open.remove(e_prev);
              open.add(e1);
            }
          } else {
            open.add(e1);
          }
        // n' is in closed
        } else {
          Tuple e_prev = closed.get(prev);
          if (e_prev.getCost()>e1.getCost()){
            closed.remove(prev);
            open.add(e1);
          }
        }
      }
    }
  }

  void trace(Tuple t){
    Stack<Node> st = new Stack<Node>();
    while (t!=null){
      st.push(t.getNode());
      t = t.getParent();
    }
  }

  class Tuple{
    private Node n;
    private Tuple p;
    private int c;
    private int g;

    Tuple(Node n, Tuple p, int c){
      this(n,p,c,0);
    }

    Tuple(Node n, Tuple p, int c, int g){
      this.n = n;
      this.p = p;
      this.c = c;
      this.g = g;
      System.err.println(this);
    }

    Integer getCost(){ return c; }
    Node getNode(){ return n; }
    Tuple getParent(){ return p; }
    Integer getG(){ return g; }

    @Override
    public boolean equals(Object obj){
      if (this == obj) return true;
      if (obj == null) return false;
      if (!(obj instanceof Tuple)) return false;
      Tuple other = (Tuple) obj;
      // System.err.println(this.n+"vs "+other.n);
      return n.equals(other.n);
    }

    @Override
    public String toString(){
      return "Tuple:(\n"+n+"Parent: "+p+"Cost: "+c+" / g: "+g+")\n";
    }
  }
}

class Node{
	private int board[][];
  private int x;
  private int y;
	private static final int size = 4;
	private static final int[][] goal = {{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,0}};
  private static final Node goalNode = new Node(goal);

	Node(int[][] board){
		this.board = board;
    for (int i=0;i<size;i++){
      for (int j=0;j<size;j++){
        if (board[i][j]==0){
          x = i;
          y = j;
        }
      }
    }
	}

  @Override
  public boolean equals(Object obj){
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof Node)) return false;
    Node other = (Node) obj;
    if (size!=other.size) return false;
    for (int i=0;i<size;i++){
      for (int j=0;j<size;j++){
        if (board[i][j]!=other.board[i][j]) return false;
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
    return ret;
  }

  boolean isGoal(){
    return this.equals(goalNode);
  }

  Node move(int dx, int dy){
    int x1 = x + dx;
    int y1 = y + dy;
    if (x1>=0 && x1<size && y1>=0 && y1<size){
      int[][] board1 = new int[size][size];
      for (int i=0;i<size;i++)
        for (int j=0;j<size;j++)
          board1[i][j] = board[i][j];
      board1[x][y] = board1[x1][y1];
      board1[x1][y1] = 0;
      return new Node(board1);
    } else {
      return null;
    }
  }

	int h(int n){
		int ans = 0;
		switch(n){
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