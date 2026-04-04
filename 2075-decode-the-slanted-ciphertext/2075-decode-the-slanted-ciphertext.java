class Solution {
    public String decodeCiphertext(String encodedText, int rows) {
        if(encodedText.length() == 0) return "";
        int col = encodedText.length()/rows;
        StringBuilder ans = new StringBuilder();
        for(int i=0;i<col;i++) {
            int j = i;
            while(j < encodedText.length()) {
                ans.append(encodedText.charAt(j));
                j += (col+1);
            }
        }
        int end = ans.length()-1;
        while(ans.charAt(end) == ' ') end--;
        return ans.substring(0,end+1);
    }
}