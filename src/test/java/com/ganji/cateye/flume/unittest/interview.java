package com.ganji.cateye.flume.unittest;

public class interview {
	
	
	/*
	 * 题目分析：解决这个问题通常有两种做法有从源中删除子串和将不是子串的合并
	 * 实现的程度：
	 * 	考虑回溯：一次删除子串后，原来位置左右两侧的串又命中子串，如从xxyy中删除xy， 中间的xy删除后，又构造了xy出来，也删除
	 *  不考虑回溯：直接匹配
	 * 考察的点
	 * 1. 考虑回溯的实现最差的时间复杂度不应高于O(m*n)， 其中m为子串长度, n为母串长度
	 * 2. 不考虑回溯的时间复杂度不应高于O(n)
	 * 3. 空间考虑，尽量减少内存分配
	 * 4. 回溯不使用递归，递归会导致至少一次扫描 
	 */
	// 考虑回溯，且不拷贝的实现
	public static String removeSub(String s, String sub) {
		if(s == null || sub == null || s.length() < sub.length()) {
			return s;
		}
		char[] chars = s.toCharArray();
		char[] subs = sub.toCharArray();
		int jumpFrom = -1;
		int jumpTo = -1;
		
		for(int i=0; i<= chars.length - subs.length; ) {
			int j=0;
			int compare = i;
			for(;j<subs.length; j++)
			{
				if(jumpFrom > -1 && compare + j >=jumpFrom) {
					compare = i - jumpFrom + jumpTo;
				}
				if(chars[compare+j] != subs[j] ){
					break;
				}
			}
			// 找到
			if(j == subs.length) {
				jumpTo = (jumpFrom > -1 ? compare : i )+j;
				// 贪婪回溯
				if(jumpTo < chars.length) {
					int back = lastIndexOf(subs, chars[jumpTo]);
					// 注意，是大于零
					if(back > 0) {
						jumpFrom = i;
						i -= back;
					} else {
						while(i<jumpTo) 
							chars[i++] = 0;
					}
				} else {
					while(i<jumpTo)
						chars[i++] = 0;
				}
			} else { 
				// 将找到的部分清空
				if(jumpFrom > -1) {
					while(jumpFrom < jumpTo) {
						chars[jumpFrom++] = 0;
					}
					i = jumpTo;
					jumpFrom = -1;
					jumpTo = -1;
				} else 
					i++;
			}
		}
		
		// 不拷贝集合； 进行移动合并
		int index = 0;
		for (int i = 0; i < chars.length;) {
			if (chars[i] > 0) {
				if(index < i)
					chars[index++] = chars[i++];
				else { 
					i++;
					index++;
				}
			} else {
				while (i < chars.length && chars[i] == 0)
					i++;
			}
		}
		
		return new String(chars, 0, index);
	}
	
	private static int lastIndexOf(char[] subs, char c) {
		for(int i=subs.length -1; i>-1; i--) {
			if(subs[i] == c)
				return i;
		}
		return -1;
	}
	
	public static void main(String[] args) {
		System.out.println("case1");
//		System.out.println("=" +interview.removeSub("ab", "ab") + "=");
//		System.out.println("=" +interview.removeSub("aabb", "ab") + "=");
//		System.out.println("=" +interview.removeSub("aaabbb", "ab") + "=");
//		System.out.println("=" +interview.removeSub("aaaabbbb", "ab") + "=");
//		System.out.println("=" +interview.removeSub("aaaaabbbbb", "ab") + "=");
//		System.out.println("=" +interview.removeSub("aaaaaabbbbbb", "ab") + "=");
//		System.out.println("=" +interview.removeSub("aaaaaaabbbbbbb", "ab") + "=");
//		System.out.println("=" +interview.removeSub("aaaaaaaabbbbbbbb", "ab") + "=");
//		System.out.println("=" +interview.removeSub("=ab=", "ab") + "=");
//		System.out.println("=" +interview.removeSub("=aabb=", "ab") + "=");
//		System.out.println("=" +interview.removeSub("=aaabbb=", "ab") + "=");
//		System.out.println("=" +interview.removeSub("=aaaabbbb=", "ab") + "=");
//		System.out.println("=" +interview.removeSub("=aaaaabbbbb=", "ab") + "=");
//		System.out.println("=" +interview.removeSub("=aaaaaabbbbbb=", "ab") + "=");
//		System.out.println("=" +interview.removeSub("=aaaaaaabbbbbbb=", "ab") + "=");
//		System.out.println("=" +interview.removeSub("=aaaaaaaabbbbbbbb=", "ab") + "=");
//		System.out.println("case2");
//		System.out.println("=" +interview.removeSub("ab", "ab") + "=");
//		System.out.println("=" +interview.removeSub("abab", "ab") + "=");
//		System.out.println("=" +interview.removeSub("ababab", "ab") + "=");
//		System.out.println("=" +interview.removeSub("abababab", "ab") + "=");
//		System.out.println("case3");
//		System.out.println("=" +interview.removeSub("abcd", "abcd") + "=");
//		System.out.println("=" +interview.removeSub("abcdabcd", "abcd") + "=");
//		System.out.println("=" +interview.removeSub("abcabcdd", "abcd") + "=");
//		System.out.println("=" +interview.removeSub("ababcdcd", "abcd") + "=");
//		System.out.println("=" +interview.removeSub("aabcdbcd", "abcd") + "=");
		System.out.println("case4");
		System.out.println("=" +interview.removeSub("ababcdcabcdd", "abcd") + "=");
		
	}
}
