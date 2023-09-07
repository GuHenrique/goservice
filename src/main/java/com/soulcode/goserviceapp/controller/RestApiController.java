package com.soulcode.goserviceapp.controller;

import com.soulcode.goserviceapp.domain.Servico;
import com.soulcode.goserviceapp.domain.Usuario;
import com.soulcode.goserviceapp.service.ServicoService;
import com.soulcode.goserviceapp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api")
public class RestApiController {

    @Autowired
    private ServicoService servicoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping(value = "/servicos")
    public List<Servico> servicos() {
        return servicoService.findAll();
    }

    @GetMapping(value = "/usuarios")
    public List<Usuario> usuarios() {
        return usuarioService.findAll();
    }

//    @PostMapping
//    @PutMapping
//    @DeleteMapping
}
