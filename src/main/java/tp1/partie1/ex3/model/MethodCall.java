package tp1.partie1.ex3.model;
public class MethodCall {
    private final String calleeName;
    private final String receiverType; 
    
    public MethodCall(String calleeName, String receiverType) {
        this.calleeName = calleeName;
        this.receiverType = receiverType;
    }

    public String getCalleeName() { return calleeName; }
    public String getReceiverType() { return receiverType; }
}
