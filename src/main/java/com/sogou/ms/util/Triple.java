package com.sogou.ms.util;

/** 三元组(与Pair的想法一致，懒得定义接口类时使用)。commons-lang 3.2才有Triple类，等不及了，用自己的吧 */
public class Triple<A, B, C> {

	public final A a;
	public final B b;
	public final C c;
	Triple(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	public String toString() {
		return "(" + a + ", " + b + ", " + c + ")";
	}

	public static <A, B, C> Triple<A, B, C> of(A a, B b, C c) {
		return new Triple<A, B, C>(a, b, c);
	}

}
