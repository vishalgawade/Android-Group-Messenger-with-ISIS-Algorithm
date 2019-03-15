package edu.buffalo.cse.cse486586.groupmessenger2;

public class Message {

    //proposed seq will become agreed seq once msg is delivered.
    private Integer proposed_seq;
    private String message;
    private boolean isDelivered;
    private int avd_id;


    private int failedPort;

    protected Message(Integer proposed_seq, String message, boolean isDelivered, int avd_id, int failedPort) {
        this.proposed_seq = proposed_seq;
        this.message = message;
        this.isDelivered = isDelivered;
        this.avd_id = avd_id;
        this.failedPort = failedPort;
    }

    public Integer getFailedPort() {
        return failedPort;
    }

    public void setFailedPort(Integer failedPort) {
        this.failedPort = failedPort;
    }

    public int getAvd_id() {
        return avd_id;
    }

    @Override
    public String toString() {
        return "Message{" +
                "proposed_seq=" + proposed_seq +
                ", message='" + message + '\'' +
                ", isDelivered=" + isDelivered +
                ", avd_id=" + avd_id +
                ", failedPort=" + failedPort +
                '}';
    }

    public void setAvd_id(int avd_id) {
        this.avd_id = avd_id;
    }

    public Integer getProposed_seq() {
        return proposed_seq;
    }

    public void setProposed_seq(Integer proposed_seq) {
        this.proposed_seq = proposed_seq;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    protected static String encodeMessage(Message message){
        return message.getMessage()+","+message.getProposed_seq()+","+message.getAvd_id()+","+delivered(message.isDelivered())+","+message.getFailedPort();
    }

    protected static Message decodeMessage(String message){
        String[] array=message.split(",");
        int avdid=Integer.parseInt(array[2]);
        boolean isDelivered=array[3].equals("Yes")?true:false;
        String msg=array[0];
        Integer seq=Integer.parseInt(array[1]);
        int failedPort = Integer.parseInt(array[4]);
        return new Message(seq ,msg,isDelivered,avdid,failedPort);
    }

    private static String delivered(boolean status){
        return status?"Yes":"No";
    }

}