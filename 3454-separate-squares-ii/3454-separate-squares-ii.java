import java.util.*;
import java.util.Arrays;

class Solution {
    public double separateSquares(int[][] squares) {
        int N = squares.length;
        int m = 2 * N;  // each square produces two events.
        Event[] events = new Event[m];
        double[] xsRaw = new double[m];
        int eIdx = 0, xIdx = 0;
        for (int[] sq : squares) {
            double x = sq[0], y = sq[1], l = sq[2];
            double x2 = x + l, y2 = y + l;
            events[eIdx++] = new Event(y, x, x2, 1);
            events[eIdx++] = new Event(y2, x, x2, -1);
            xsRaw[xIdx++] = x;
            xsRaw[xIdx++] = x2;
        }
        // Sort events by y.
        Arrays.sort(events, Comparator.comparingDouble(ev -> ev.y));
        // Compress x–coordinates.
        double[] xs = compress(xsRaw);
        // Build segment tree over compressed x.
        SegmentTree st = new SegmentTree(xs);
 
        // Sweep once and build piecewise linear segments:
        // Each segment covers an interval [yStart, yEnd] where the union x–length is constant.
        List<SweepSegment> segs = new ArrayList<>();
        double cumArea = 0.0;
        double prevY = events[0].y;
        int i = 0;
        while (i < m) {
            double curY = events[i].y;
            if (curY > prevY) {
                double unionX = st.query();
                // Record segment from prevY to curY with union length unionX
                segs.add(new SweepSegment(prevY, curY, cumArea, unionX));
                cumArea += unionX * (curY - prevY);
                prevY = curY;
            }
            // Process all events at curY.
            while (i < m && events[i].y == curY) {
                int lIdx = Arrays.binarySearch(xs, events[i].x1);
                if (lIdx < 0) lIdx = -lIdx - 1;
                int rIdx = Arrays.binarySearch(xs, events[i].x2);
                if (rIdx < 0) rIdx = -rIdx - 1;
                st.update(1, 0, xs.length - 1, lIdx, rIdx, events[i].type);
                i++;
            }
        }
 
        double totalArea = cumArea;
        double target = totalArea / 2.0;
 
        // Now, the union–area function F(y) is piecewise linear.
        // Find the first segment where the cumulative area crosses target.
        for (SweepSegment seg : segs) {
            double segArea = seg.unionX * (seg.yEnd - seg.yStart);
            if (target <= seg.startCum + segArea + 1e-10) {
                double needed = target - seg.startCum;
                // If unionX is 0, then the area does not increase; skip this segment.
                if (seg.unionX < 1e-10) continue;
                double dy = needed / seg.unionX;
                return seg.yStart + dy;
            }
        }
        return prevY;  // fallback (should not happen)
    }
    
    // Compress an array of doubles to a sorted array of unique values.
    private double[] compress(double[] arr) {
        Arrays.sort(arr);
        int cnt = 1;
        for (int i = 1; i < arr.length; i++) {
            if (Double.compare(arr[i], arr[i - 1]) != 0) cnt++;
        }
        double[] res = new double[cnt];
        res[0] = arr[0];
        int j = 1;
        for (int i = 1; i < arr.length; i++) {
            if (Double.compare(arr[i], arr[i - 1]) != 0) {
                res[j++] = arr[i];
            }
        }
        return res;
    }
    
    // Event for sweep-line (vertical boundaries).
    class Event {
        double y, x1, x2;
        int type;  // +1 for adding, -1 for removing an interval.
        Event(double y, double x1, double x2, int type) {
            this.y = y;
            this.x1 = x1;
            this.x2 = x2;
            this.type = type;
        }
    }
    
    // Segment representing a portion of the union–area function.
    class SweepSegment {
        double yStart, yEnd, startCum; // startCum: cumulative area at yStart.
        double unionX; // union length in x over this segment.
        SweepSegment(double yStart, double yEnd, double startCum, double unionX) {
            this.yStart = yStart;
            this.yEnd = yEnd;
            this.startCum = startCum;
            this.unionX = unionX;
        }
    }
    
    // Segment Tree for union–length of x–intervals.
    class SegmentTree {
        int n;
        double[] tree; // tree[idx]: total length covered in the node's interval.
        int[] count;   // count[idx]: coverage count.
        double[] xs;   // compressed x–coordinates.
 
        SegmentTree(double[] xs) {
            this.xs = xs;
            this.n = xs.length;
            tree = new double[4 * n];
            count = new int[4 * n];
        }
 
        // Update the range [ql, qr) with delta.
        void update(int idx, int l, int r, int ql, int qr, int delta) {
            if (qr <= l || ql >= r) return;
            if (ql <= l && r <= qr) {
                count[idx] += delta;
            } else {
                int mid = (l + r) >> 1;
                update(idx * 2, l, mid, ql, qr, delta);
                update(idx * 2 + 1, mid, r, ql, qr, delta);
            }
            if (count[idx] > 0) {
                tree[idx] = xs[r] - xs[l];
            } else if (r - l == 1) {
                tree[idx] = 0;
            } else {
                tree[idx] = tree[idx * 2] + tree[idx * 2 + 1];
            }
        }
 
        double query() {
            return tree[1];
        }
    }
}