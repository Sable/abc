//5, 27, 5, 40
package p;

@interface A {
	String name() default DEFAULT_NAME;
	public static final String DEFAULT_NAME= "Jean-Pierre";
}