
public class Foo {
	
	@Log
	private int x;
	
	public Foo() {
		// Konstruktor
	}
	
	@Log
	public int pub( int i) {
		priv();
		x=i;
		return i;
	}
	
	@Log
	private String priv() {
		System.out.println("Hi");
		return "true";
	}
	
	public static void main(String[] args) {
		Foo f = new Foo();
		
		f.pub(10);
		f.setName(new String[] {"Tarek", "Auel" } );
	}
	
	public int pub() {
		return 0;
	}
	
	public void setName(String[] t) {
		
	}

}
