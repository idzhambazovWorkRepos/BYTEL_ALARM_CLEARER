/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comptel.database;

import com.comptel.cc.event.client.vo.AlarmEvent;

/**
 *
 * @author cpt2vot
 */
public class ELEvent extends AlarmEvent {
    String ackuser;

    public ELEvent() {
        ackuser = "";
    }
    
    public String getAckuser() {
        return ackuser;
    }

    public void setAckuser(String ackuser) {
        this.ackuser = ackuser;
    }
    
}
