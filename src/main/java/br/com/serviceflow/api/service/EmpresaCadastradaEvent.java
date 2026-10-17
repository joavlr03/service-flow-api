package br.com.serviceflow.api.service;

/** Publicado pelo cadastro de empresa; o e-mail de boas-vindas só sai depois do commit. */
public record EmpresaCadastradaEvent(String empresa, String dono, String email) {}
