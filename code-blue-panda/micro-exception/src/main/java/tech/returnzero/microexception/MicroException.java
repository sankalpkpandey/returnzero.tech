package tech.returnzero.microexception;


public class MicroException extends Exception {

    public MicroException(String message){
        super(message);
    }


    public MicroException(String message, String[] arguments){
        super(message);
    }

}
