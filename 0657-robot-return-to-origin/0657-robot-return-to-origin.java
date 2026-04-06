class Solution {
    public boolean judgeCircle(String moves) {
        int x = 0, y = 0;

        for (char move : moves.toCharArray()) {
            if (move == 'U') {
                y++;
            } else if (move == 'D') {
                y--;
            } else if (move == 'L') {
                x--;
            } else { // 'R'
                x++;
            }
        }

        if(x == 0 && y == 0) { 
            return true; 
        } 
        return false;
    }
}