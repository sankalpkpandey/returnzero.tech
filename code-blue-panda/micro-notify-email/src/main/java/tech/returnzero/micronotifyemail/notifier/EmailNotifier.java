package tech.returnzero.micronotifyemail.notifier;

import tech.returnzero.microexception.MicroException;
import tech.returnzero.micronotifyinterface.MicroNotifyInterface;
import tech.returnzero.micronotifyinterface.data.Message;
import tech.returnzero.micronotifyinterface.data.Post;

public class EmailNotifier implements MicroNotifyInterface {

    @Override
    public Post notify(Message message) throws MicroException {
       
        return null;
    }

}
