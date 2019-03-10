package edu.buffalo.cse.cse486586.groupmessenger2;

public class Message {

    //proposed seq will become agreed seq once msg is delivered.
    private Integer proposed_seq;
    private String message;
    private boolean isDelivered;
    private int avd_id;

    protected Message(Integer proposed_seq, String message, boolean isDelivered, int avd_id) {
        this.proposed_seq = proposed_seq;
        this.message = message;
        this.isDelivered = isDelivered;
        this.avd_id = avd_id;
    }

    public int getAvd_id() {
        return avd_id;
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
        return message.getMessage()+","+message.getProposed_seq()+","+message.getAvd_id()+","+delivered(message.isDelivered());
    }

    protected static Message decodeMessage(String message){
        String[] array=message.split(",");
        int avdid=Integer.parseInt(array[2]);
        boolean isDelivered=array[3].equals("Yes")?true:false;
        String msg=array[0];
        Integer seq=Integer.parseInt(array[1]);
        return new Message(seq ,msg,isDelivered,avdid);
    }

    private static String delivered(boolean status){
        return status?"Yes":"No";
    }

}