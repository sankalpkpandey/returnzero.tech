package tech.returnzero.micronotifyinterface;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import tech.returnzero.microexception.MicroException;
import tech.returnzero.micronotifyinterface.data.Message;
import tech.returnzero.micronotifyinterface.data.Post;

public interface MicroNotifyControllerInterface {

    default ResponseEntity<Object> send(MicroNotifyInterface notifier, Message message) throws MicroException {
        Post post = notifier.notify(message);
        return new ResponseEntity<Object>(post.getResponse(), HttpStatus.OK);
    }

}
