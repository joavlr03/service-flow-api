package br.com.serviceflow.api.exception;

public class OnboardingAlreadyCompletedException extends RuntimeException {
    public OnboardingAlreadyCompletedException() {
        super("O primeiro acesso desta empresa já foi concluído");
    }
}
