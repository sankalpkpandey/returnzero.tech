package tech.returnzero.micronotifyinterface;

import tech.returnzero.microexception.MicroException;
import tech.returnzero.micronotifyinterface.data.Message;
import tech.returnzero.micronotifyinterface.data.Post;

public interface MicroNotifyInterface {

    public Post notify(Message message) throws MicroException;

}