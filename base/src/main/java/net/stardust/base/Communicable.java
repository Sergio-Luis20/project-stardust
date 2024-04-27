package net.stardust.base;

public interface Communicable {
	
	default String getId() {
		return getClass().getSimpleName();
	}

}
