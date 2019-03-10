package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.Comparator;

public class MessageComparator implements Comparator<Message> {

    @Override
    public int compare(Message lhs, Message rhs) {
       if(lhs.getProposed_seq()>rhs.getProposed_seq())
           return 1;
       else if(lhs.getProposed_seq()<rhs.getProposed_seq())
           return -1;
       else {
           return Integer.compare(lhs.getAvd_id(), rhs.getAvd_id());
       }
    }
}
