package net.stardust.base.database;

public class NotFoundException extends RuntimeException {
	
    private Object inputObject;
    
    public NotFoundException(Object inputObject) {
        super();
        this.inputObject = inputObject;
    }

    public NotFoundException(String message, Object inputObject) {
        super(message);
        this.inputObject = inputObject;
    }

    public NotFoundException(Throwable cause, Object inputObject) {
        super(cause);
        this.inputObject = inputObject;
    }

    public NotFoundException(String message, Throwable cause, Object inputObject) {
        super(message, cause);
        this.inputObject = inputObject;
    }

    public Object getInputObject() {
        return inputObject;
    }

}
