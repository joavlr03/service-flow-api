package br.com.serviceflow.api.exception;

public class SetupAlreadyCompletedException extends RuntimeException {
    public SetupAlreadyCompletedException() {
        super("A instalação já possui um proprietário cadastrado");
    }
}
