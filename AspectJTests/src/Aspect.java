
public aspect Aspect {
	
	pointcut logAllMethod():
		execution( * * (..));
	
	pointcut logSpecific():
		execution(@Log * * (..) );
	
	pointcut logNewObj():
		initialization( *.new(..)) && !within(Aspect);
	
	pointcut logVar() :
		set(@Log * *.*);
	
	before() : logAllMethod() {
		System.out.println("Methode aufgerufen: " + thisJoinPoint.getSignature());
		System.out.print("Parameter: ");
		for(Object o:thisJoinPoint.getArgs()) {
			System.out.print( o+ ", "); 
		}
		System.out.println();
	}
	
	before() : logSpecific() {
		System.err.println("Methode aufgerufen: " + thisJoinPoint.getSignature());
		System.err.print("Parameter: ");
		for(Object o:thisJoinPoint.getArgs()) {
			System.err.print( o+ ", "); 
		}
		System.err.println();
	}
	
	before() : logNewObj() {
		System.out.println("Methode aufgerufen: " + thisJoinPoint.getSignature());
		System.out.print("Parameter: ");
		for(Object o:thisJoinPoint.getArgs()) {
			System.out.print( o+ ", "); 
		}
		System.out.println();
	}
	
	before() : logVar() {
		System.out.println("Varible: " + thisJoinPoint.getSignature());
		System.out.print("Parameter: ");
		for(Object o:thisJoinPoint.getArgs()) {
			System.out.print( o+ ", "); 
		}
		System.out.println();
	}	
}
