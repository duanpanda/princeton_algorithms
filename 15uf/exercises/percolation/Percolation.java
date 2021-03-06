import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class Percolation {
    private final int n;
    // n by n grid. blocked site: 0, open site: 1, full site: 2
    private byte[][] grid;
    // union find algorithm object with Top and Bottom virtual site
    private final WeightedQuickUnionUF ufTopBottom;
    /* union find algorithm object with only Top virtual site
       It is used to solve the backwash problem in isFull(). */
    private final WeightedQuickUnionUF ufTop;
    private int numberOfOpenSites;
    private final int top;            // index of the virtual site 'top' in WQUF
    private final int bottom;         // index of the virtual site 'bottom' in WQUF


    /**
     * create n-by-n grid, with all sites blocked
     */
    public Percolation(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("invalid n, n must be positive");
        }
        this.n = n;
        grid = new byte[n][n];

        /* With two virtual nodes: top and bottom,
           to reduce the number of percolates() query.

           indice 0 to n-1 in WQUF   <==> grid[0][0] to grid[n-1][n-1]
           index (i*n+j) in WQUF     <==> grid[i][j]
           index (n*n+2-2) in WQUF   <==> virtual site 'top'
           index (n*n+2-1) in WQUF   <==> virtual site 'bottom' */
        ufTopBottom = new WeightedQuickUnionUF(n * n + 2);

        /* With two virtual nodes: top and bottom,
           to reduce the number of percolates() query.

           indice 0 to n-1 in WQUF   <==> grid[0][0] to grid[n-1][n-1]
           index (i*n+j) in WQUF     <==> grid[i][j]
           index (n*n+1-1) in WQUF   <==> virtual site 'top' */
        ufTop = new WeightedQuickUnionUF(n * n + 1);
        top = n * n;
        bottom = top + 1;

        numberOfOpenSites = 0;
    }

    /**
     * open site (row, col) if it is not open already
     */
    public void open(int row, int col) {
        if (!siteExists(row, col)) {
            throw new IllegalArgumentException("illegal index");
        }
        if (grid[row-1][col-1] == 0) {
            grid[row-1][col-1] = 1;
            numberOfOpenSites++;
            connectNeighbors(row, col);
        }
    }

    /**
     * is site (row, col) open?
     */
    public boolean isOpen(int row, int col) {
        if (!siteExists(row, col)) {
            throw new IllegalArgumentException("illegal index");
        }
        return uncheckedIsOpen(row, col);
    }

    private boolean uncheckedIsOpen(int row, int col) {
        return (grid[row-1][col-1] == 1) || grid[row-1][col-1] == 2;
    }

    /**
     * is site (row, col) full?
     */
    public boolean isFull(int row, int col) {
        if (!siteExists(row, col)) {
            throw new IllegalArgumentException("illegal index");
        }
        return uncheckedIsFull(row, col);
    }

    /* Caution: if site[row][col] is full, this function changes its
       internal state! */
    private boolean uncheckedIsFull(int row, int col) {
        int currentSite = getUFIndex(row, col);
        if (grid[row-1][col-1] == 2) {
            return true;
        }
        else {
            /* check ufTop instead of ufTopBottom to avoid
               the backwash problem */
            if (uncheckedIsOpen(row, col)
                    && ufTop.connected(top, currentSite)) {
                grid[row-1][col-1] = 2;
                return true;
            }
            else {
                return false;
            }
        }
    }

    /**
     * number of open sites
     */
    public int numberOfOpenSites() {
        return numberOfOpenSites;
    }

    /**
     * does the system percolate?
     */
    public boolean percolates() {
        return ufTopBottom.connected(top, bottom);
    }

    // @pre: the current site (row,col) must be open
    private void connectNeighbors(int row, int col) {
        assert isOpen(row, col);
        int ufIndexCurrentSite = getUFIndex(row, col);
        if (row == 1) {
            ufTopBottom.union(ufIndexCurrentSite, top);
            ufTop.union(ufIndexCurrentSite, top);
        }
        if (row == n) {
            ufTopBottom.union(ufIndexCurrentSite, bottom);
        }

        // up, down, let, right
        int[] neighborRows = { row-1, row+1, row, row };
        int[] neighborCols = { col, col, col-1, col+1 };

        for (int i = 0; i < neighborRows.length; i++) {
            if (siteExists(neighborRows[i], neighborCols[i])
                    && uncheckedIsOpen(neighborRows[i], neighborCols[i])) {
                connectSites(row, col, neighborRows[i], neighborCols[i]);
            }
        }
    }

    private boolean siteExists(int r, int c) {
        return (r > 0) && (r <= n) && (c > 0) && (c <= n);
    }

    private void connectSites(int r1, int c1, int r2, int c2) {
        int si = getUFIndex(r1, c1);
        int ti = getUFIndex(r2, c2);
        ufTopBottom.union(si, ti);
        ufTop.union(si, ti);
    }

    private int getUFIndex(int row, int col) {
        return (row - 1) * n + (col - 1);
    }

    // blocked site: '*'
    // open site:    'o'
    // full site:    '+'
    private void print() {
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                if (uncheckedIsFull(i, j)) {
                    StdOut.print("+ ");
                }
                else if (uncheckedIsOpen(i, j)) {
                    StdOut.print("o ");
                }
                else {
                    StdOut.print("* ");
                }
            }
            StdOut.print("\n");
        }
    }

    /**
     * test client
     */
    public static void main(String[] args) {
        int n = 5;
        StdOut.println("Grid One:");
        Percolation percolation = new Percolation(n);
        for (int i = 1; i <= n; i++) {
            percolation.open(i, i);
        }
        percolation.print();
        StdOut.println(percolation.percolates());
        StdOut.println("numberOfOpenSites=" + percolation.numberOfOpenSites());

        StdOut.println();
        StdOut.println("Grid Two:");
        percolation = new Percolation(n);
        for (int i = 1; i < n; i++) {
            percolation.open(i, 2);
        }
        percolation.open(1, 1);
        percolation.open(1, 1);
        percolation.open(5, 5);
        percolation.print();
        StdOut.println(percolation.percolates());
        StdOut.println("numberOfOpenSites=" + percolation.numberOfOpenSites());
    }
}
